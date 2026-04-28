import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CourseService } from '../../../core/services/course.service';
import { Lesson, ContentType, ProcessingStatus, MediaAsset } from '../../../core/models/course.model';
import { MessageResponse } from '../../../core/models/auth.model';

@Component({
  selector: 'app-lesson-content',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <div class="lesson-content-manager">
      <div class="content-type-selector" *ngIf="!lesson.mediaAssets?.length">
        <h4>Select Content Type</h4>
        <div class="content-type-options">
          <button 
            *ngFor="let type of contentTypes" 
            class="content-type-btn"
            [class.selected]="selectedContentType === type.value"
            (click)="selectContentType(type.value)"
            [disabled]="isProcessing">
            <i [class]="type.icon"></i>
            <span>{{ type.label }}</span>
            <small>{{ type.description }}</small>
          </button>
        </div>
      </div>

      <!-- Text Content -->
      <div *ngIf="selectedContentType === 'TEXT'" class="text-content-editor">
        <form [formGroup]="textForm" (ngSubmit)="saveTextContent()">
          <div class="form-group">
            <label for="content">Lesson Content</label>
            <textarea 
              id="content"
              formControlName="content"
              class="form-control"
              rows="10"
              placeholder="Enter your lesson content here..."></textarea>
          </div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="textForm.invalid || isProcessing">
              <i class="fas fa-save"></i> Save Content
            </button>
          </div>
        </form>
      </div>

      <!-- File Upload -->
      <div *ngIf="selectedContentType !== 'TEXT' && selectedContentType !== 'QUIZ'" class="file-upload-section">
        <div class="upload-area" 
             [class.dragover]="isDragOver"
             (dragover)="onDragOver($event)"
             (dragleave)="onDragLeave($event)"
             (drop)="onDrop($event)"
             (click)="fileInput.click()">
          <input #fileInput type="file" 
                 [accept]="getAcceptedFileTypes()"
                 (change)="onFileSelected($event)"
                 style="display: none;">
          
          <div class="upload-content" *ngIf="!selectedFile">
            <i class="fas fa-cloud-upload-alt upload-icon"></i>
            <h4>Upload {{ getContentTypeLabel() }}</h4>
            <p>Drag and drop your file here, or click to browse</p>
            <small>{{ getFileTypeHint() }}</small>
          </div>

          <div class="file-preview" *ngIf="selectedFile">
            <i [class]="getFileIcon()"></i>
            <div class="file-info">
              <h5>{{ selectedFile.name }}</h5>
              <p>{{ formatFileSize(selectedFile.size) }}</p>
            </div>
            <button type="button" class="btn btn-sm btn-outline-danger" (click)="removeSelectedFile()">
              <i class="fas fa-times"></i>
            </button>
          </div>
        </div>

        <div class="upload-options" *ngIf="selectedFile">
          <div class="form-group">
            <label for="description">Description (Optional)</label>
            <input type="text" 
                   id="description"
                   class="form-control"
                   [(ngModel)]="fileDescription"
                   placeholder="Add a description for this content...">
          </div>
          <div class="form-actions">
            <button type="button" 
                    class="btn btn-primary"
                    (click)="uploadFile()"
                    [disabled]="isUploading">
              <i class="fas fa-upload" *ngIf="!isUploading"></i>
              <i class="fas fa-spinner fa-spin" *ngIf="isUploading"></i>
              {{ isUploading ? 'Uploading...' : 'Upload Content' }}
            </button>
          </div>
        </div>

        <!-- Upload Progress -->
        <div class="upload-progress" *ngIf="uploadProgress > 0">
          <div class="progress">
            <div class="progress-bar" [style.width.%]="uploadProgress"></div>
          </div>
          <small>{{ uploadProgress }}% uploaded</small>
        </div>
      </div>

      <!-- Processing Status -->
      <div class="processing-status" *ngIf="lesson.processingStatus !== 'READY'">
        <div class="status-indicator" [class]="getStatusClass()">
          <i [class]="getStatusIcon()"></i>
          <span>{{ getStatusMessage() }}</span>
        </div>
        <div class="progress" *ngIf="lesson.processingStatus === 'PROCESSING'">
          <div class="progress-bar progress-bar-striped progress-bar-animated" style="width: 100%"></div>
        </div>
      </div>

      <!-- Existing Content -->
      <div class="existing-content" *ngIf="lesson.mediaAssets?.length">
        <h4>Current Content</h4>
        <div class="media-list">
          <div *ngFor="let asset of lesson.mediaAssets" class="media-item">
            <div class="media-info">
              <i [class]="getMediaIcon(asset.mediaType)"></i>
              <div class="media-details">
                <h5>{{ asset.originalFilename }}</h5>
                <p>{{ asset.formattedFileSize }} • {{ asset.mediaType }}</p>
                <small *ngIf="asset.description">{{ asset.description }}</small>
              </div>
            </div>
            <div class="media-actions">
              <button type="button" 
                      class="btn btn-sm btn-outline-primary"
                      *ngIf="asset.mediaType === 'VIDEO'"
                      (click)="previewVideo(asset)">
                <i class="fas fa-play"></i> Preview
              </button>
              <button type="button" 
                      class="btn btn-sm btn-outline-danger"
                      (click)="deleteMediaAsset(asset)"
                      [disabled]="isProcessing">
                <i class="fas fa-trash"></i> Delete
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .lesson-content-manager {
      padding: 20px;
    }

    .content-type-options {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 15px;
      margin-top: 15px;
    }

    .content-type-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px;
      border: 2px solid #e9ecef;
      border-radius: 8px;
      background: white;
      cursor: pointer;
      transition: all 0.3s ease;
      text-align: center;
    }

    .content-type-btn:hover {
      border-color: #007bff;
      background-color: #f8f9fa;
    }

    .content-type-btn.selected {
      border-color: #007bff;
      background-color: #e3f2fd;
    }

    .content-type-btn i {
      font-size: 2rem;
      margin-bottom: 10px;
      color: #6c757d;
    }

    .content-type-btn.selected i {
      color: #007bff;
    }

    .upload-area {
      border: 2px dashed #dee2e6;
      border-radius: 8px;
      padding: 40px;
      text-align: center;
      cursor: pointer;
      transition: all 0.3s ease;
      margin: 20px 0;
    }

    .upload-area:hover,
    .upload-area.dragover {
      border-color: #007bff;
      background-color: #f8f9fa;
    }

    .upload-icon {
      font-size: 3rem;
      color: #6c757d;
      margin-bottom: 15px;
    }

    .file-preview {
      display: flex;
      align-items: center;
      gap: 15px;
      padding: 15px;
      background: #f8f9fa;
      border-radius: 6px;
    }

    .file-preview i {
      font-size: 2rem;
      color: #007bff;
    }

    .processing-status {
      margin: 20px 0;
      padding: 15px;
      border-radius: 6px;
      background: #f8f9fa;
    }

    .status-indicator {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .status-indicator.processing {
      color: #ffc107;
    }

    .status-indicator.failed {
      color: #dc3545;
    }

    .media-list {
      display: flex;
      flex-direction: column;
      gap: 15px;
    }

    .media-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px;
      border: 1px solid #dee2e6;
      border-radius: 6px;
      background: white;
    }

    .media-info {
      display: flex;
      align-items: center;
      gap: 15px;
    }

    .media-actions {
      display: flex;
      gap: 10px;
    }

    .progress {
      height: 6px;
      background-color: #e9ecef;
      border-radius: 3px;
      overflow: hidden;
      margin-top: 10px;
    }

    .progress-bar {
      height: 100%;
      background-color: #007bff;
      transition: width 0.3s ease;
    }
  `]
})
export class LessonContentComponent implements OnInit {
  @Input() lesson!: Lesson;
  @Input() courseId!: string;
  @Input() moduleId!: string;
  @Output() contentUpdated = new EventEmitter<void>();

  selectedContentType: ContentType = ContentType.TEXT;
  selectedFile: File | null = null;
  fileDescription = '';
  isUploading = false;
  isProcessing = false;
  uploadProgress = 0;
  isDragOver = false;

  textForm: FormGroup;

  contentTypes = [
    {
      value: ContentType.TEXT,
      label: 'Text Lesson',
      description: 'Rich text content with formatting',
      icon: 'fas fa-file-text'
    },
    {
      value: ContentType.VIDEO,
      label: 'Video Lesson',
      description: 'Upload video files for streaming',
      icon: 'fas fa-video'
    },
    {
      value: ContentType.PDF,
      label: 'PDF Document',
      description: 'Upload PDF documents',
      icon: 'fas fa-file-pdf'
    },
    {
      value: ContentType.IMAGE,
      label: 'Image Content',
      description: 'Upload images and diagrams',
      icon: 'fas fa-image'
    },
    {
      value: ContentType.QUIZ,
      label: 'Quiz Assessment',
      description: 'Create interactive quizzes',
      icon: 'fas fa-question-circle'
    }
  ];

  constructor(
    private courseService: CourseService,
    private fb: FormBuilder
  ) {
    this.textForm = this.fb.group({
      content: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.selectedContentType = this.lesson.contentType || ContentType.TEXT;
    if (this.lesson.content) {
      this.textForm.patchValue({ content: this.lesson.content });
    }
  }

  selectContentType(type: ContentType) {
    this.selectedContentType = type;
  }

  getAcceptedFileTypes(): string {
    switch (this.selectedContentType) {
      case ContentType.VIDEO:
        return 'video/*';
      case ContentType.PDF:
        return '.pdf';
      case ContentType.IMAGE:
        return 'image/*';
      default:
        return '*/*';
    }
  }

  getContentTypeLabel(): string {
    return this.contentTypes.find(t => t.value === this.selectedContentType)?.label || '';
  }

  getFileTypeHint(): string {
    switch (this.selectedContentType) {
      case ContentType.VIDEO:
        return 'Supported formats: MP4, AVI, MOV, WebM (Max: 100MB)';
      case ContentType.PDF:
        return 'PDF documents only (Max: 50MB)';
      case ContentType.IMAGE:
        return 'Supported formats: JPG, PNG, GIF, WebP (Max: 10MB)';
      default:
        return '';
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = false;
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.selectedFile = files[0];
    }
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  removeSelectedFile() {
    this.selectedFile = null;
    this.fileDescription = '';
  }

  uploadFile() {
    if (!this.selectedFile) return;

    this.isUploading = true;
    this.uploadProgress = 0;

    this.courseService.uploadLessonContent(
      this.courseId,
      this.moduleId,
      this.lesson.id,
      this.selectedFile,
      this.fileDescription
    ).subscribe({
      next: (mediaAsset) => {
        this.isUploading = false;
        this.uploadProgress = 100;
        this.selectedFile = null;
        this.fileDescription = '';
        this.contentUpdated.emit();
      },
      error: (error) => {
        this.isUploading = false;
        this.uploadProgress = 0;
        console.error('Upload failed:', error);
      }
    });
  }

  saveTextContent() {
    if (this.textForm.invalid) return;

    const request = {
      title: this.lesson.title,
      description: this.lesson.description,
      content: this.textForm.value.content,
      contentType: ContentType.TEXT,
      estimatedDurationMinutes: this.lesson.estimatedDurationMinutes
    };

    this.courseService.updateLesson(
      this.courseId,
      this.moduleId,
      this.lesson.id,
      request
    ).subscribe({
      next: () => {
        this.contentUpdated.emit();
      },
      error: (error) => {
        console.error('Failed to save content:', error);
      }
    });
  }

  deleteMediaAsset(asset: MediaAsset) {
    if (confirm('Are you sure you want to delete this content?')) {
      this.courseService.deleteLessonMedia(
        this.courseId,
        this.moduleId,
        this.lesson.id,
        asset.id
      ).subscribe({
        next: () => {
          this.contentUpdated.emit();
        },
        error: (error) => {
          console.error('Failed to delete media:', error);
        }
      });
    }
  }

  previewVideo(asset: MediaAsset) {
    // TODO: Implement video preview modal
    console.log('Preview video:', asset);
  }

  getStatusClass(): string {
    switch (this.lesson.processingStatus) {
      case ProcessingStatus.PROCESSING:
        return 'processing';
      case ProcessingStatus.FAILED:
        return 'failed';
      default:
        return '';
    }
  }

  getStatusIcon(): string {
    switch (this.lesson.processingStatus) {
      case ProcessingStatus.PROCESSING:
        return 'fas fa-spinner fa-spin';
      case ProcessingStatus.FAILED:
        return 'fas fa-exclamation-triangle';
      default:
        return 'fas fa-check';
    }
  }

  getStatusMessage(): string {
    switch (this.lesson.processingStatus) {
      case ProcessingStatus.PENDING:
        return 'Waiting to process...';
      case ProcessingStatus.UPLOADING:
        return 'Uploading content...';
      case ProcessingStatus.PROCESSING:
        return 'Processing video content...';
      case ProcessingStatus.FAILED:
        return 'Processing failed. Please try again.';
      default:
        return 'Content is ready';
    }
  }

  getFileIcon(): string {
    if (!this.selectedFile) return 'fas fa-file';
    
    const type = this.selectedFile.type;
    if (type.startsWith('video/')) return 'fas fa-video';
    if (type.startsWith('image/')) return 'fas fa-image';
    if (type === 'application/pdf') return 'fas fa-file-pdf';
    return 'fas fa-file';
  }

  getMediaIcon(mediaType: string): string {
    switch (mediaType) {
      case 'VIDEO': return 'fas fa-video';
      case 'IMAGE': return 'fas fa-image';
      case 'PDF': return 'fas fa-file-pdf';
      case 'DOCUMENT': return 'fas fa-file-word';
      default: return 'fas fa-file';
    }
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
}