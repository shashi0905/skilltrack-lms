import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CourseService } from '@core/services/course.service';
import { AuthService } from '@core/services/auth.service';
import { Course, CourseModule, Lesson, ContentType, MediaAsset } from '@core/models/course.model';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-student-course-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './student-course-detail.component.html',
  styleUrl: './student-course-detail.component.scss'
})
export class StudentCourseDetailComponent implements OnInit {
  private readonly courseService = inject(CourseService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly sanitizer = inject(DomSanitizer);

  protected readonly course = signal<Course | null>(null);
  protected readonly modules = signal<CourseModule[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly enrolled = signal(false);
  protected readonly enrolling = signal(false);
  protected readonly activeLesson = signal<Lesson | null>(null);
  protected readonly ContentType = ContentType;

  ngOnInit(): void {
    this.loadCourseData();
  }

  protected loadCourseData(): void {
    const courseId = this.route.snapshot.paramMap.get('id');
    if (!courseId) return;

    this.loading.set(true);
    this.courseService.getPublishedCourseById(courseId).subscribe({
      next: (course) => {
        this.course.set(course);
        this.modules.set(course.modules || []);
        this.loading.set(false);
        this.checkEnrollment(courseId);
      },
      error: (err) => {
        this.error.set('Failed to load course details.');
        this.loading.set(false);
      }
    });
  }

  private checkEnrollment(courseId: string): void {
    this.courseService.checkEnrollment(courseId).subscribe({
      next: (res) => this.enrolled.set(res.enrolled),
      error: () => this.enrolled.set(false)
    });
  }

  protected enroll(): void {
    const courseId = this.course()?.id;
    if (!courseId) return;

    this.enrolling.set(true);
    this.courseService.enrollInCourse(courseId).subscribe({
      next: () => {
        this.enrolled.set(true);
        this.enrolling.set(false);
      },
      error: (err) => {
        this.enrolling.set(false);
        const msg = err.error?.message || 'Failed to enroll. Please try again.';
        alert(msg);
      }
    });
  }

  protected openLesson(lesson: Lesson): void {
    if (!this.enrolled()) return;
    this.activeLesson.set(lesson);
  }

  protected closeLesson(): void {
    this.activeLesson.set(null);
  }

  protected getMediaUrl(asset: MediaAsset): string {
    return this.courseService.getMediaUrl(asset.id);
  }

  protected getSafePdfUrl(asset: MediaAsset): SafeResourceUrl {
    return this.sanitizer.bypassSecurityTrustResourceUrl(this.getMediaUrl(asset));
  }

  protected getVideoAsset(lesson: Lesson): MediaAsset | undefined {
    return lesson.mediaAssets?.find(a => a.mediaType === 'VIDEO');
  }

  protected getPdfAsset(lesson: Lesson): MediaAsset | undefined {
    return lesson.mediaAssets?.find(a => a.mediaType === 'DOCUMENT');
  }

  protected getImageAsset(lesson: Lesson): MediaAsset | undefined {
    return lesson.mediaAssets?.find(a => a.mediaType === 'IMAGE');
  }

  protected getDifficultyClass(difficulty: string): string {
    switch (difficulty?.toLowerCase()) {
      case 'beginner': return 'difficulty-beginner';
      case 'intermediate': return 'difficulty-intermediate';
      case 'advanced': return 'difficulty-advanced';
      default: return 'difficulty-beginner';
    }
  }

  protected getContentIcon(contentType: ContentType): string {
    switch (contentType) {
      case ContentType.VIDEO: return '🎬';
      case ContentType.PDF: return '📄';
      case ContentType.IMAGE: return '🖼️';
      case ContentType.QUIZ: return '❓';
      default: return '📝';
    }
  }

  protected getCurrentUser() {
    return this.authService.getCurrentUser();
  }

  protected logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/auth/login']),
      error: () => {
        this.authService.logoutLocal();
        this.router.navigate(['/auth/login']);
      }
    });
  }

  protected goBack(): void {
    this.router.navigate(['/student/courses']);
  }
}
