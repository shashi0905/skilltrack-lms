import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { CourseService } from '@core/services/course.service';
import { AuthService } from '@core/services/auth.service';
import { Course, CourseStatus } from '@core/models/course.model';

@Component({
  selector: 'app-instructor-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './instructor-dashboard.component.html',
  styleUrl: './instructor-dashboard.component.scss'
})
export class InstructorDashboardComponent implements OnInit {
  private readonly courseService = inject(CourseService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly courses = signal<Course[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly CourseStatus = CourseStatus;

  ngOnInit(): void {
    this.loadCourses();
  }

  protected getCurrentUser() {
    return this.authService.getCurrentUser();
  }

  protected logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/auth/login']);
      },
      error: () => {
        this.authService.logoutLocal();
        this.router.navigate(['/auth/login']);
      }
    });
  }

  protected loadCourses(): void {
    this.loading.set(true);
    this.error.set(null);

    this.courseService.getCoursesByInstructor().subscribe({
      next: (courses) => {
        this.courses.set(courses);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Failed to load courses. Please try again.');
        this.loading.set(false);
        console.error('Error loading courses:', err);
      }
    });
  }

  protected createCourse(): void {
    this.router.navigate(['/instructor/courses/create']);
  }

  protected editCourse(courseId: string): void {
    this.router.navigate(['/instructor/courses', courseId, 'edit']);
  }

  protected viewCourse(courseId: string): void {
    this.router.navigate(['/instructor/courses', courseId]);
  }

  protected deleteCourse(course: Course): void {
    if (!confirm(`Are you sure you want to delete "${course.title}"?`)) {
      return;
    }

    this.courseService.deleteCourse(course.id).subscribe({
      next: () => {
        this.courses.update(courses => courses.filter(c => c.id !== course.id));
      },
      error: (err) => {
        alert('Failed to delete course. Please try again.');
        console.error('Error deleting course:', err);
      }
    });
  }

  protected publishCourse(course: Course): void {
    this.courseService.publishCourse(course.id).subscribe({
      next: (updatedCourse) => {
        this.courses.update(courses =>
          courses.map(c => c.id === course.id ? updatedCourse : c)
        );
      },
      error: (err) => {
        const message = err.error?.message || 'Failed to publish course. Ensure it has at least one module with one lesson.';
        alert(message);
        console.error('Error publishing course:', err);
      }
    });
  }
}
