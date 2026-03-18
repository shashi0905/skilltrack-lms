import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CourseService } from '@core/services/course.service';
import { DifficultyLevel } from '@core/models/course.model';

@Component({
  selector: 'app-course-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './course-form.component.html',
  styleUrl: './course-form.component.scss'
})
export class CourseFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly courseService = inject(CourseService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly courseForm: FormGroup;
  protected readonly isEditMode = signal(false);
  protected readonly courseId = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly difficultyLevels = Object.values(DifficultyLevel);
  protected readonly tagInput = signal('');

  constructor() {
    this.courseForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', [Validators.required, Validators.maxLength(2000)]],
      difficulty: [DifficultyLevel.BEGINNER, Validators.required],
      tags: [[] as string[]],
      estimatedDuration: [0, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.loadCourseData();
  }

  private loadCourseData(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.courseId.set(id);
      this.loading.set(true);

      this.courseService.getCourseById(id).subscribe({
        next: (course) => {
          this.courseForm.patchValue({
            title: course.title,
            description: course.description,
            difficulty: course.difficulty,
            tags: course.tags,
            estimatedDuration: course.estimatedDuration
          });
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set('Failed to load course data.');
          this.loading.set(false);
          console.error('Error loading course:', err);
        }
      });
    }
  }

  protected addTag(): void {
    const tag = this.tagInput().trim();
    if (tag) {
      const currentTags = this.courseForm.get('tags')?.value || [];
      if (!currentTags.includes(tag)) {
        this.courseForm.patchValue({ tags: [...currentTags, tag] });
      }
      this.tagInput.set('');
    }
  }

  protected removeTag(tag: string): void {
    const currentTags = this.courseForm.get('tags')?.value || [];
    this.courseForm.patchValue({ tags: currentTags.filter((t: string) => t !== tag) });
  }

  protected onSubmit(): void {
    if (this.courseForm.invalid) {
      this.courseForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const request = this.courseForm.value;

    const operation = this.isEditMode()
      ? this.courseService.updateCourse(this.courseId()!, request)
      : this.courseService.createCourse(request);

    operation.subscribe({
      next: (course) => {
        this.router.navigate(['/instructor/courses', course.id]);
      },
      error: (err) => {
        this.error.set('Failed to save course. Please try again.');
        this.loading.set(false);
        console.error('Error saving course:', err);
      }
    });
  }

  protected cancel(): void {
    this.router.navigate(['/instructor/dashboard']);
  }
}
