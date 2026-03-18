import { Component, input, output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CourseService } from '@core/services/course.service';
import { MediaAsset } from '@core/models/course.model';

@Component({
  selector: 'app-media-upload',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './media-upload.component.html',
  styleUrl: './media-upload.component.scss'
})
export class MediaUploadComponent {
  private readonly courseService = inject(CourseService);

  readonly courseId = input.required<string>();
  readonly moduleId = input<string>();
  readonly lessonId = input<string>();
  readonly mediaUploaded = output<MediaAsset>();

  protected readonly uploading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly selectedFile = signal<File | null>(null);

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      
      if (file.size > 100 * 1024 * 1024) {
        this.error.set('File size must be less than 100MB');
        return;
      }

      this.selectedFile.set(file);
      this.error.set(null);
    }
  }

  protected uploadFile(): void {
    const file = this.selectedFile();
    if (!file) return;

    this.uploading.set(true);
    this.error.set(null);

    this.courseService.uploadMedia(
      this.courseId(),
      file,
      this.moduleId(),
      this.lessonId()
    ).subscribe({
      next: (media) => {
        this.uploading.set(false);
        this.selectedFile.set(null);
        this.mediaUploaded.emit(media);
      },
      error: (err) => {
        this.uploading.set(false);
        this.error.set('Failed to upload file. Please try again.');
        console.error('Upload error:', err);
      }
    });
  }

  protected clearSelection(): void {
    this.selectedFile.set(null);
    this.error.set(null);
  }
}
