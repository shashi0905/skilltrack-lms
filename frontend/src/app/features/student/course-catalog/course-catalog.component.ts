import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CourseService } from '@core/services/course.service';
import { Course } from '@core/models/course.model';

@Component({
  selector: 'app-course-catalog',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './course-catalog.component.html',
  styleUrl: './course-catalog.component.scss'
})
export class CourseCatalogComponent implements OnInit {
  private readonly courseService = inject(CourseService);

  protected readonly courses = signal<Course[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly searchQuery = signal('');
  protected readonly currentPage = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly totalElements = signal(0);

  private readonly pageSize = 12;

  ngOnInit(): void {
    this.loadCourses();
  }

  protected onSearch(): void {
    this.currentPage.set(0);
    this.loadCourses();
  }

  protected onPageChange(page: number): void {
    this.currentPage.set(page);
    this.loadCourses();
  }

  protected loadCourses(): void {
    this.loading.set(true);
    this.error.set(null);

    const operation = this.searchQuery() 
      ? this.courseService.searchPublishedCourses(this.searchQuery(), this.currentPage(), this.pageSize)
      : this.courseService.getPublishedCourses(this.currentPage(), this.pageSize);

    operation.subscribe({
      next: (response) => {
        this.courses.set(response.content);
        this.totalPages.set(response.totalPages);
        this.totalElements.set(response.totalElements);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Failed to load courses');
        this.loading.set(false);
        console.error('Error loading courses:', err);
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
}