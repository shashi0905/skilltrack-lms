import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CourseService } from '@core/services/course.service';
import { Course, CourseModule } from '@core/models/course.model';

@Component({
  selector: 'app-student-course-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './student-course-detail.component.html',
  styleUrl: './student-course-detail.component.scss'
})
export class StudentCourseDetailComponent implements OnInit {
  private readonly courseService = inject(CourseService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly course = signal<Course | null>(null);
  protected readonly modules = signal<CourseModule[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

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
        // Modules are now included in the course response
        this.modules.set(course.modules || []);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Failed to load course details.');
        this.loading.set(false);
        console.error('Error loading course:', err);
      }
    });
  }

  protected getDifficultyClass(difficulty: string): string {
    switch (difficulty?.toLowerCase()) {
      case 'beginner': return 'difficulty-beginner';
      case 'intermediate': return 'difficulty-intermediate';
      case 'advanced': return 'difficulty-advanced';
      default: return 'difficulty-beginner';
    }
  }

  protected goBack(): void {
    this.router.navigate(['/student/courses']);
  }
}