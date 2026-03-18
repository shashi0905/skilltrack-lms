import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@env/environment';
import {
  Course,
  CourseModule,
  Lesson,
  MediaAsset,
  CreateCourseRequest,
  UpdateCourseRequest,
  CreateModuleRequest,
  UpdateModuleRequest,
  CreateLessonRequest,
  UpdateLessonRequest
} from '../models/course.model';
import { MessageResponse } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = `${environment.apiUrl}/courses`;

  getCoursesByInstructor(): Observable<Course[]> {
    return this.http.get<any[]>(`${this.API_URL}/my-courses`).pipe(
      map(courses => courses.map(course => this.transformCourseResponse(course)))
    );
  }

  getCourseById(id: string): Observable<Course> {
    return this.http.get<any>(`${this.API_URL}/${id}`).pipe(
      map(course => this.transformCourseResponse(course))
    );
  }

  private transformCourseResponse(courseResponse: any): Course {
    return {
      id: courseResponse.id,
      instructorId: courseResponse.instructorId,
      instructorName: courseResponse.instructorName,
      title: courseResponse.title,
      description: courseResponse.description,
      difficulty: courseResponse.difficulty,
      tags: courseResponse.tags ? courseResponse.tags.split(',').map((tag: string) => tag.trim()) : [],
      estimatedDuration: courseResponse.estimatedDurationHours,
      status: courseResponse.status,
      hasDraftChanges: courseResponse.hasDraftChanges || false,
      createdAt: courseResponse.createdAt,
      updatedAt: courseResponse.updatedAt,
      modules: courseResponse.modules
    };
  }

  createCourse(request: CreateCourseRequest): Observable<Course> {
    const backendRequest = {
      ...request,
      tags: request.tags.join(', ')
    };
    return this.http.post<any>(this.API_URL, backendRequest).pipe(
      map(course => this.transformCourseResponse(course))
    );
  }

  updateCourse(id: string, request: UpdateCourseRequest): Observable<Course> {
    const backendRequest = {
      ...request,
      tags: request.tags.join(', ')
    };
    return this.http.put<any>(`${this.API_URL}/${id}`, backendRequest).pipe(
      map(course => this.transformCourseResponse(course))
    );
  }

  deleteCourse(id: string): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.API_URL}/${id}`);
  }

  publishCourse(id: string): Observable<Course> {
    return this.http.post<any>(`${this.API_URL}/${id}/publish`, {}).pipe(
      map(course => this.transformCourseResponse(course))
    );
  }

  getModulesByCourse(courseId: string): Observable<CourseModule[]> {
    return this.http.get<CourseModule[]>(`${this.API_URL}/${courseId}/modules`);
  }

  createModule(courseId: string, request: CreateModuleRequest): Observable<CourseModule> {
    return this.http.post<CourseModule>(`${this.API_URL}/${courseId}/modules`, request);
  }

  updateModule(courseId: string, moduleId: string, request: UpdateModuleRequest): Observable<CourseModule> {
    return this.http.put<CourseModule>(`${this.API_URL}/${courseId}/modules/${moduleId}`, request);
  }

  deleteModule(courseId: string, moduleId: string): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.API_URL}/${courseId}/modules/${moduleId}`);
  }

  getLessonsByModule(courseId: string, moduleId: string): Observable<Lesson[]> {
    return this.http.get<Lesson[]>(`${this.API_URL}/${courseId}/modules/${moduleId}/lessons`);
  }

  createLesson(courseId: string, moduleId: string, request: CreateLessonRequest): Observable<Lesson> {
    return this.http.post<Lesson>(`${this.API_URL}/${courseId}/modules/${moduleId}/lessons`, request);
  }

  updateLesson(courseId: string, moduleId: string, lessonId: string, request: UpdateLessonRequest): Observable<Lesson> {
    return this.http.put<Lesson>(`${this.API_URL}/${courseId}/modules/${moduleId}/lessons/${lessonId}`, request);
  }

  deleteLesson(courseId: string, moduleId: string, lessonId: string): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.API_URL}/${courseId}/modules/${moduleId}/lessons/${lessonId}`);
  }

  uploadMedia(courseId: string, file: File, moduleId?: string, lessonId?: string): Observable<MediaAsset> {
    const formData = new FormData();
    formData.append('file', file);
    if (moduleId) formData.append('moduleId', moduleId);
    if (lessonId) formData.append('lessonId', lessonId);

    return this.http.post<MediaAsset>(`${this.API_URL}/${courseId}/media`, formData);
  }

  getMediaByCourse(courseId: string): Observable<MediaAsset[]> {
    return this.http.get<MediaAsset[]>(`${this.API_URL}/${courseId}/media`);
  }

  deleteMedia(courseId: string, mediaId: string): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.API_URL}/${courseId}/media/${mediaId}`);
  }

  // Public course browsing methods
  getPublishedCourses(page: number = 0, size: number = 10): Observable<{content: Course[], totalElements: number, totalPages: number}> {
    return this.http.get<any>(`${this.API_URL}/public?page=${page}&size=${size}`).pipe(
      map(response => ({
        content: response.content.map((course: any) => this.transformCourseResponse(course)),
        totalElements: response.totalElements,
        totalPages: response.totalPages
      }))
    );
  }

  searchPublishedCourses(query: string, page: number = 0, size: number = 10): Observable<{content: Course[], totalElements: number, totalPages: number}> {
    return this.http.get<any>(`${this.API_URL}/public/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`).pipe(
      map(response => ({
        content: response.content.map((course: any) => this.transformCourseResponse(course)),
        totalElements: response.totalElements,
        totalPages: response.totalPages
      }))
    );
  }

  getPublishedCourseById(id: string): Observable<Course> {
    return this.http.get<any>(`${this.API_URL}/public/${id}`).pipe(
      map(course => this.transformCourseResponse(course))
    );
  }
}
