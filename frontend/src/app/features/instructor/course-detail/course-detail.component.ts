import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CourseService } from '@core/services/course.service';
import { Course, CourseModule, Lesson } from '@core/models/course.model';

@Component({
  selector: 'app-course-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './course-detail.component.html',
  styleUrl: './course-detail.component.scss'
})
export class CourseDetailComponent implements OnInit {
  private readonly courseService = inject(CourseService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  protected readonly course = signal<Course | null>(null);
  protected readonly modules = signal<CourseModule[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  
  protected readonly showModuleForm = signal(false);
  protected readonly showLessonForm = signal(false);
  protected readonly selectedModuleId = signal<string | null>(null);
  protected readonly editingModuleId = signal<string | null>(null);
  protected readonly editingLessonId = signal<string | null>(null);

  protected readonly moduleForm: FormGroup;
  protected readonly lessonForm: FormGroup;

  constructor() {
    this.moduleForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', [Validators.required, Validators.maxLength(1000)]]
    });

    this.lessonForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', [Validators.required, Validators.maxLength(1000)]],
      content: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.loadCourseData();
  }

  private loadCourseData(): void {
    const courseId = this.route.snapshot.paramMap.get('id');
    if (!courseId) return;

    this.loading.set(true);
    this.courseService.getCourseById(courseId).subscribe({
      next: (course) => {
        this.course.set(course);
        this.loadModules(courseId);
      },
      error: (err) => {
        this.error.set('Failed to load course.');
        this.loading.set(false);
        console.error('Error loading course:', err);
      }
    });
  }

  private loadModules(courseId: string): void {
    this.courseService.getModulesByCourse(courseId).subscribe({
      next: (modules) => {
        this.modules.set(modules);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Failed to load modules.');
        this.loading.set(false);
        console.error('Error loading modules:', err);
      }
    });
  }

  protected openModuleForm(module?: CourseModule): void {
    if (module) {
      this.editingModuleId.set(module.id);
      this.moduleForm.patchValue({
        title: module.title,
        description: module.description
      });
    } else {
      this.editingModuleId.set(null);
      this.moduleForm.reset();
    }
    this.showModuleForm.set(true);
  }

  protected saveModule(): void {
    if (this.moduleForm.invalid) {
      this.moduleForm.markAllAsTouched();
      return;
    }

    const courseId = this.course()?.id;
    if (!courseId) return;

    const request = this.moduleForm.value;
    const operation = this.editingModuleId()
      ? this.courseService.updateModule(courseId, this.editingModuleId()!, request)
      : this.courseService.createModule(courseId, request);

    operation.subscribe({
      next: () => {
        this.showModuleForm.set(false);
        this.moduleForm.reset();
        this.loadModules(courseId);
      },
      error: (err) => {
        alert('Failed to save module.');
        console.error('Error saving module:', err);
      }
    });
  }

  protected deleteModule(moduleId: string): void {
    if (!confirm('Delete this module and all its lessons?')) return;

    const courseId = this.course()?.id;
    if (!courseId) return;

    this.courseService.deleteModule(courseId, moduleId).subscribe({
      next: () => {
        this.loadModules(courseId);
      },
      error: (err) => {
        alert('Failed to delete module.');
        console.error('Error deleting module:', err);
      }
    });
  }

  protected openLessonForm(moduleId: string, lesson?: Lesson): void {
    this.selectedModuleId.set(moduleId);
    if (lesson) {
      this.editingLessonId.set(lesson.id);
      this.lessonForm.patchValue({
        title: lesson.title,
        description: lesson.description,
        content: lesson.content
      });
    } else {
      this.editingLessonId.set(null);
      this.lessonForm.reset();
    }
    this.showLessonForm.set(true);
  }

  protected saveLesson(): void {
    if (this.lessonForm.invalid) {
      this.lessonForm.markAllAsTouched();
      return;
    }

    const courseId = this.course()?.id;
    const moduleId = this.selectedModuleId();
    if (!courseId || !moduleId) return;

    const request = this.lessonForm.value;
    const operation = this.editingLessonId()
      ? this.courseService.updateLesson(courseId, moduleId, this.editingLessonId()!, request)
      : this.courseService.createLesson(courseId, moduleId, request);

    operation.subscribe({
      next: () => {
        this.showLessonForm.set(false);
        this.lessonForm.reset();
        this.loadModules(courseId);
      },
      error: (err) => {
        alert('Failed to save lesson.');
        console.error('Error saving lesson:', err);
      }
    });
  }

  protected deleteLesson(moduleId: string, lessonId: string): void {
    if (!confirm('Delete this lesson?')) return;

    const courseId = this.course()?.id;
    if (!courseId) return;

    this.courseService.deleteLesson(courseId, moduleId, lessonId).subscribe({
      next: () => {
        this.loadModules(courseId);
      },
      error: (err) => {
        alert('Failed to delete lesson.');
        console.error('Error deleting lesson:', err);
      }
    });
  }

  protected publishCourse(): void {
    const courseId = this.course()?.id;
    if (!courseId) return;

    this.courseService.publishCourse(courseId).subscribe({
      next: (updatedCourse) => {
        this.course.set(updatedCourse);
      },
      error: (err) => {
        const message = err.error?.message || 'Failed to publish. Ensure at least one module with one lesson exists.';
        alert(message);
        console.error('Error publishing course:', err);
      }
    });
  }

  protected goBack(): void {
    this.router.navigate(['/instructor/dashboard']);
  }
}
