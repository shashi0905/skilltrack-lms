export enum CourseStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED'
}

export enum DifficultyLevel {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED'
}

export enum MediaType {
  PDF = 'PDF',
  IMAGE = 'IMAGE',
  VIDEO = 'VIDEO',
  DOCUMENT = 'DOCUMENT'
}

export interface Course {
  id: string;
  instructorId: string;
  instructorName: string;
  title: string;
  description: string;
  difficulty: DifficultyLevel;
  tags: string[];
  estimatedDuration: number;
  status: CourseStatus;
  hasDraftChanges: boolean;
  createdAt: string;
  updatedAt: string;
  modules?: CourseModule[];
}

export interface CourseModule {
  id: string;
  courseId: string;
  title: string;
  description: string;
  orderIndex: number;
  createdAt: string;
  updatedAt: string;
  lessons?: Lesson[];
}

export interface Lesson {
  id: string;
  moduleId: string;
  title: string;
  description: string;
  content: string;
  orderIndex: number;
  createdAt: string;
  updatedAt: string;
}

export interface MediaAsset {
  id: string;
  courseId?: string;
  moduleId?: string;
  lessonId?: string;
  fileName: string;
  fileType: MediaType;
  fileSize: number;
  storagePath: string;
  uploadedBy: string;
  createdAt: string;
}

export interface CreateCourseRequest {
  title: string;
  description: string;
  difficulty: DifficultyLevel;
  tags: string[];
  estimatedDuration: number;
}

export interface UpdateCourseRequest {
  title: string;
  description: string;
  difficulty: DifficultyLevel;
  tags: string[];
  estimatedDuration: number;
}

export interface CreateModuleRequest {
  title: string;
  description: string;
}

export interface UpdateModuleRequest {
  title: string;
  description: string;
}

export interface CreateLessonRequest {
  title: string;
  description: string;
  content: string;
}

export interface UpdateLessonRequest {
  title: string;
  description: string;
  content: string;
}
