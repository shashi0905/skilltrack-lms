import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { CourseService } from '../../../core/services/course.service';
import { QuizQuestion, QuestionType, CreateQuizQuestionRequest, CreateQuizOptionRequest } from '../../../core/models/course.model';

@Component({
  selector: 'app-quiz-builder',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <div class="quiz-builder">
      <div class="quiz-header">
        <h4>Quiz Questions</h4>
        <button type="button" class="btn btn-primary" (click)="addNewQuestion()">
          <i class="fas fa-plus"></i> Add Question
        </button>
      </div>

      <!-- Question List -->
      <div class="questions-list" *ngIf="questions.length > 0">
        <div *ngFor="let question of questions; let i = index" class="question-card">
          <div class="question-header">
            <div class="question-info">
              <span class="question-number">Q{{ i + 1 }}</span>
              <span class="question-type-badge" [class]="getQuestionTypeClass(question.questionType)">
                {{ getQuestionTypeLabel(question.questionType) }}
              </span>
              <span class="question-points">{{ question.points }} pts</span>
            </div>
            <div class="question-actions">
              <button type="button" class="btn btn-sm btn-outline-primary" (click)="editQuestion(question, i)">
                <i class="fas fa-edit"></i>
              </button>
              <button type="button" class="btn btn-sm btn-outline-danger" (click)="deleteQuestion(question, i)">
                <i class="fas fa-trash"></i>
              </button>
            </div>
          </div>
          <div class="question-content">
            <p class="question-text">{{ question.questionText }}</p>
            <div class="question-options">
              <div *ngFor="let option of question.options" class="option-item" [class.correct]="option.isCorrect">
                <i class="fas" [class.fa-check-circle]="option.isCorrect" [class.fa-circle]="!option.isCorrect"></i>
                <span>{{ option.optionText }}</span>
              </div>
            </div>
            <p *ngIf="question.explanation" class="question-explanation">
              <strong>Explanation:</strong> {{ question.explanation }}
            </p>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div class="empty-state" *ngIf="questions.length === 0">
        <i class="fas fa-question-circle"></i>
        <h5>No Questions Yet</h5>
        <p>Add your first question to get started with this quiz.</p>
        <button type="button" class="btn btn-primary" (click)="addNewQuestion()">
          <i class="fas fa-plus"></i> Add First Question
        </button>
      </div>

      <!-- Question Form Modal -->
      <div class="modal" [class.show]="showQuestionForm" *ngIf="showQuestionForm">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">{{ editingIndex >= 0 ? 'Edit' : 'Add' }} Question</h5>
              <button type="button" class="btn-close" (click)="cancelQuestionForm()"></button>
            </div>
            <div class="modal-body">
              <form [formGroup]="questionForm">
                <div class="form-group">
                  <label for="questionText">Question Text *</label>
                  <textarea 
                    id="questionText"
                    formControlName="questionText"
                    class="form-control"
                    rows="3"
                    placeholder="Enter your question here..."></textarea>
                  <div class="invalid-feedback" *ngIf="questionForm.get('questionText')?.invalid && questionForm.get('questionText')?.touched">
                    Question text is required
                  </div>
                </div>

                <div class="row">
                  <div class="col-md-6">
                    <div class="form-group">
                      <label for="questionType">Question Type *</label>
                      <select id="questionType" formControlName="questionType" class="form-control" (change)="onQuestionTypeChange()">
                        <option value="SINGLE_CHOICE">Single Choice</option>
                        <option value="MULTIPLE_CHOICE">Multiple Choice</option>
                        <option value="TRUE_FALSE">True/False</option>
                      </select>
                    </div>
                  </div>
                  <div class="col-md-6">
                    <div class="form-group">
                      <label for="points">Points</label>
                      <input type="number" id="points" formControlName="points" class="form-control" min="1" max="10">
                    </div>
                  </div>
                </div>

                <div class="form-group">
                  <label>Answer Options *</label>
                  <div formArrayName="options" class="options-container">
                    <div *ngFor="let option of optionsArray.controls; let i = index" [formGroupName]="i" class="option-form">
                      <div class="option-input-group">
                        <div class="option-correct">
                          <input 
                            type="checkbox" 
                            [id]="'option-correct-' + i"
                            formControlName="isCorrect"
                            [disabled]="!canSelectMultipleCorrect() && hasOtherCorrectOption(i)">
                          <label [for]="'option-correct-' + i" class="correct-label">Correct</label>
                        </div>
                        <div class="option-text-input">
                          <input 
                            type="text" 
                            formControlName="optionText"
                            class="form-control"
                            [placeholder]="'Option ' + (i + 1)">
                        </div>
                        <button 
                          type="button" 
                          class="btn btn-sm btn-outline-danger"
                          (click)="removeOption(i)"
                          [disabled]="optionsArray.length <= getMinOptions()">
                          <i class="fas fa-times"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                  <button 
                    type="button" 
                    class="btn btn-sm btn-outline-primary mt-2"
                    (click)="addOption()"
                    [disabled]="optionsArray.length >= getMaxOptions()">
                    <i class="fas fa-plus"></i> Add Option
                  </button>
                </div>

                <div class="form-group">
                  <label for="explanation">Explanation (Optional)</label>
                  <textarea 
                    id="explanation"
                    formControlName="explanation"
                    class="form-control"
                    rows="2"
                    placeholder="Provide an explanation for the correct answer..."></textarea>
                </div>
              </form>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="cancelQuestionForm()">Cancel</button>
              <button type="button" class="btn btn-primary" (click)="saveQuestion()" [disabled]="questionForm.invalid || !hasCorrectAnswer()">
                {{ editingIndex >= 0 ? 'Update' : 'Add' }} Question
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop" *ngIf="showQuestionForm" (click)="cancelQuestionForm()"></div>
    </div>
  `,
  styles: [`
    .quiz-builder {
      padding: 20px;
    }

    .quiz-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 15px;
      border-bottom: 1px solid #dee2e6;
    }

    .questions-list {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .question-card {
      border: 1px solid #dee2e6;
      border-radius: 8px;
      background: white;
      overflow: hidden;
    }

    .question-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px;
      background: #f8f9fa;
      border-bottom: 1px solid #dee2e6;
    }

    .question-info {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .question-number {
      font-weight: bold;
      color: #007bff;
    }

    .question-type-badge {
      padding: 2px 8px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;
    }

    .question-type-badge.single-choice {
      background: #e3f2fd;
      color: #1976d2;
    }

    .question-type-badge.multiple-choice {
      background: #f3e5f5;
      color: #7b1fa2;
    }

    .question-type-badge.true-false {
      background: #e8f5e8;
      color: #388e3c;
    }

    .question-points {
      font-size: 12px;
      color: #6c757d;
    }

    .question-actions {
      display: flex;
      gap: 5px;
    }

    .question-content {
      padding: 15px;
    }

    .question-text {
      font-weight: 500;
      margin-bottom: 15px;
    }

    .question-options {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-bottom: 15px;
    }

    .option-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 8px;
      border-radius: 4px;
      background: #f8f9fa;
    }

    .option-item.correct {
      background: #d4edda;
      color: #155724;
    }

    .option-item i {
      color: #6c757d;
    }

    .option-item.correct i {
      color: #28a745;
    }

    .question-explanation {
      font-size: 14px;
      color: #6c757d;
      font-style: italic;
    }

    .empty-state {
      text-align: center;
      padding: 60px 20px;
      color: #6c757d;
    }

    .empty-state i {
      font-size: 4rem;
      margin-bottom: 20px;
      color: #dee2e6;
    }

    .modal {
      display: none;
      position: fixed;
      z-index: 1050;
      left: 0;
      top: 0;
      width: 100%;
      height: 100%;
      overflow: auto;
      background-color: rgba(0,0,0,0.4);
    }

    .modal.show {
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .modal-dialog {
      max-width: 800px;
      width: 90%;
      margin: 20px;
    }

    .modal-content {
      background: white;
      border-radius: 8px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      border-bottom: 1px solid #dee2e6;
    }

    .modal-body {
      padding: 20px;
      max-height: 70vh;
      overflow-y: auto;
    }

    .modal-footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      padding: 20px;
      border-top: 1px solid #dee2e6;
    }

    .btn-close {
      background: none;
      border: none;
      font-size: 1.5rem;
      cursor: pointer;
    }

    .options-container {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .option-form {
      border: 1px solid #dee2e6;
      border-radius: 6px;
      padding: 10px;
      background: #f8f9fa;
    }

    .option-input-group {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .option-correct {
      display: flex;
      align-items: center;
      gap: 5px;
      min-width: 80px;
    }

    .option-text-input {
      flex: 1;
    }

    .correct-label {
      font-size: 12px;
      color: #28a745;
      font-weight: 500;
    }

    .modal-backdrop {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(0, 0, 0, 0.4);
      z-index: 1040;
    }
  `]
})
export class QuizBuilderComponent implements OnInit {
  @Input() courseId!: string;
  @Input() moduleId!: string;
  @Input() lessonId!: string;
  @Output() questionsChanged = new EventEmitter<QuizQuestion[]>();

  questions: QuizQuestion[] = [];
  showQuestionForm = false;
  editingIndex = -1;
  questionForm: FormGroup;

  constructor(
    private courseService: CourseService,
    private fb: FormBuilder
  ) {
    this.questionForm = this.createQuestionForm();
  }

  ngOnInit() {
    this.loadQuestions();
  }

  private createQuestionForm(): FormGroup {
    return this.fb.group({
      questionText: ['', Validators.required],
      questionType: [QuestionType.SINGLE_CHOICE, Validators.required],
      explanation: [''],
      points: [1, [Validators.required, Validators.min(1), Validators.max(10)]],
      options: this.fb.array([])
    });
  }

  get optionsArray(): FormArray {
    return this.questionForm.get('options') as FormArray;
  }

  loadQuestions() {
    this.courseService.getQuizQuestions(this.courseId, this.moduleId, this.lessonId).subscribe({
      next: (questions) => {
        this.questions = questions;
        this.questionsChanged.emit(questions);
      },
      error: (error) => {
        console.error('Failed to load questions:', error);
      }
    });
  }

  addNewQuestion() {
    this.editingIndex = -1;
    this.questionForm = this.createQuestionForm();
    this.initializeOptions(QuestionType.SINGLE_CHOICE);
    this.showQuestionForm = true;
  }

  editQuestion(question: QuizQuestion, index: number) {
    this.editingIndex = index;
    this.questionForm = this.createQuestionForm();
    
    // Populate form with existing question data
    this.questionForm.patchValue({
      questionText: question.questionText,
      questionType: question.questionType,
      explanation: question.explanation,
      points: question.points
    });

    // Populate options
    const optionsArray = this.optionsArray;
    question.options.forEach(option => {
      optionsArray.push(this.fb.group({
        optionText: [option.optionText, Validators.required],
        isCorrect: [option.isCorrect]
      }));
    });

    this.showQuestionForm = true;
  }

  deleteQuestion(question: QuizQuestion, index: number) {
    if (confirm('Are you sure you want to delete this question?')) {
      this.courseService.deleteQuizQuestion(this.courseId, this.moduleId, this.lessonId, question.id).subscribe({
        next: () => {
          this.questions.splice(index, 1);
          this.questionsChanged.emit(this.questions);
        },
        error: (error) => {
          console.error('Failed to delete question:', error);
        }
      });
    }
  }

  onQuestionTypeChange() {
    const questionType = this.questionForm.get('questionType')?.value;
    this.clearOptions();
    this.initializeOptions(questionType);
  }

  private initializeOptions(questionType: QuestionType) {
    const optionsArray = this.optionsArray;
    const minOptions = this.getMinOptionsForType(questionType);
    
    for (let i = 0; i < minOptions; i++) {
      optionsArray.push(this.fb.group({
        optionText: ['', Validators.required],
        isCorrect: [false]
      }));
    }

    // For True/False, set default options
    if (questionType === QuestionType.TRUE_FALSE) {
      optionsArray.at(0)?.patchValue({ optionText: 'True', isCorrect: true });
      optionsArray.at(1)?.patchValue({ optionText: 'False', isCorrect: false });
    }
  }

  addOption() {
    if (this.optionsArray.length < this.getMaxOptions()) {
      this.optionsArray.push(this.fb.group({
        optionText: ['', Validators.required],
        isCorrect: [false]
      }));
    }
  }

  removeOption(index: number) {
    if (this.optionsArray.length > this.getMinOptions()) {
      this.optionsArray.removeAt(index);
    }
  }

  private clearOptions() {
    while (this.optionsArray.length > 0) {
      this.optionsArray.removeAt(0);
    }
  }

  saveQuestion() {
    if (this.questionForm.invalid || !this.hasCorrectAnswer()) {
      return;
    }

    const formValue = this.questionForm.value;
    const request: CreateQuizQuestionRequest = {
      questionText: formValue.questionText,
      questionType: formValue.questionType,
      explanation: formValue.explanation,
      points: formValue.points,
      options: formValue.options.map((option: any) => ({
        optionText: option.optionText,
        isCorrect: option.isCorrect
      }))
    };

    if (this.editingIndex >= 0) {
      // Update existing question
      const questionId = this.questions[this.editingIndex].id;
      this.courseService.updateQuizQuestion(this.courseId, this.moduleId, this.lessonId, questionId, request).subscribe({
        next: (updatedQuestion) => {
          this.questions[this.editingIndex] = updatedQuestion;
          this.questionsChanged.emit(this.questions);
          this.cancelQuestionForm();
        },
        error: (error) => {
          console.error('Failed to update question:', error);
        }
      });
    } else {
      // Add new question
      this.courseService.addQuizQuestion(this.courseId, this.moduleId, this.lessonId, request).subscribe({
        next: (newQuestion) => {
          this.questions.push(newQuestion);
          this.questionsChanged.emit(this.questions);
          this.cancelQuestionForm();
        },
        error: (error) => {
          console.error('Failed to add question:', error);
        }
      });
    }
  }

  cancelQuestionForm() {
    this.showQuestionForm = false;
    this.editingIndex = -1;
    this.questionForm.reset();
  }

  canSelectMultipleCorrect(): boolean {
    const questionType = this.questionForm.get('questionType')?.value;
    return questionType === QuestionType.MULTIPLE_CHOICE;
  }

  hasOtherCorrectOption(currentIndex: number): boolean {
    if (this.canSelectMultipleCorrect()) {
      return false;
    }

    return this.optionsArray.controls.some((control, index) => 
      index !== currentIndex && control.get('isCorrect')?.value === true
    );
  }

  hasCorrectAnswer(): boolean {
    return this.optionsArray.controls.some(control => 
      control.get('isCorrect')?.value === true
    );
  }

  getMinOptions(): number {
    return this.getMinOptionsForType(this.questionForm.get('questionType')?.value);
  }

  getMaxOptions(): number {
    return this.getMaxOptionsForType(this.questionForm.get('questionType')?.value);
  }

  private getMinOptionsForType(questionType: QuestionType): number {
    switch (questionType) {
      case QuestionType.TRUE_FALSE:
        return 2;
      case QuestionType.SINGLE_CHOICE:
      case QuestionType.MULTIPLE_CHOICE:
        return 2;
      default:
        return 2;
    }
  }

  private getMaxOptionsForType(questionType: QuestionType): number {
    switch (questionType) {
      case QuestionType.TRUE_FALSE:
        return 2;
      case QuestionType.SINGLE_CHOICE:
      case QuestionType.MULTIPLE_CHOICE:
        return 6;
      default:
        return 6;
    }
  }

  getQuestionTypeLabel(questionType: QuestionType): string {
    switch (questionType) {
      case QuestionType.SINGLE_CHOICE:
        return 'Single Choice';
      case QuestionType.MULTIPLE_CHOICE:
        return 'Multiple Choice';
      case QuestionType.TRUE_FALSE:
        return 'True/False';
      default:
        return 'Unknown';
    }
  }

  getQuestionTypeClass(questionType: QuestionType): string {
    switch (questionType) {
      case QuestionType.SINGLE_CHOICE:
        return 'single-choice';
      case QuestionType.MULTIPLE_CHOICE:
        return 'multiple-choice';
      case QuestionType.TRUE_FALSE:
        return 'true-false';
      default:
        return '';
    }
  }
}