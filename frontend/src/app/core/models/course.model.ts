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
  DOCUMENT = 'DOCUMENT',
  PRESENTATION = 'PRESENTATION',
  SPREADSHEET = 'SPREADSHEET',
  ARCHIVE = 'ARCHIVE',
  AUDIO = 'AUDIO',
  OTHER = 'OTHER'
}

export enum ContentType {
  TEXT = 'TEXT',
  VIDEO = 'VIDEO',
  PDF = 'PDF',
  IMAGE = 'IMAGE',
  QUIZ = 'QUIZ'
}

export enum ProcessingStatus {
  PENDING = 'PENDING',
  UPLOADING = 'UPLOADING',
  PROCESSING = 'PROCESSING',
  READY = 'READY',
  FAILED = 'FAILED'
}

export enum QuestionType {
  SINGLE_CHOICE = 'SINGLE_CHOICE',
  MULTIPLE_CHOICE = 'MULTIPLE_CHOICE',
  TRUE_FALSE = 'TRUE_FALSE'
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
  content?: string;
  orderIndex: number;
  estimatedDurationMinutes?: number;
  contentType: ContentType;
  processingStatus: ProcessingStatus;
  videoDurationSeconds?: number;
  hlsManifestUrl?: string;
  createdAt: string;
  updatedAt: string;
  mediaAssets?: MediaAsset[];
  quizQuestions?: QuizQuestion[];
}

export interface MediaAsset {
  id: string;
  originalFilename: string;
  contentType: string;
  fileSizeBytes: number;
  formattedFileSize: string;
  mediaType: MediaType;
  description?: string;
  downloadUrl?: string;
  previewUrl?: string;
  createdAt: string;
  uploadedByName: string;
}

export interface QuizQuestion {
  id: string;
  questionText: string;
  questionType: QuestionType;
  orderIndex: number;
  explanation?: string;
  points: number;
  createdAt: string;
  updatedAt: string;
  options: QuizOption[];
}

export interface QuizOption {
  id: string;
  optionText: string;
  orderIndex: number;
  isCorrect: boolean;
  createdAt: string;
  updatedAt: string;
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
  content?: string;
  estimatedDurationMinutes?: number;
  contentType: ContentType;
}

export interface UpdateLessonRequest {
  title: string;
  description: string;
  content?: string;
  estimatedDurationMinutes?: number;
  contentType: ContentType;
}

export interface CreateQuizQuestionRequest {
  questionText: string;
  questionType: QuestionType;
  explanation?: string;
  points?: number;
  options: CreateQuizOptionRequest[];
}

export interface CreateQuizOptionRequest {
  optionText: string;
  isCorrect: boolean;
}

export enum EnrollmentStatus {
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  DROPPED = 'DROPPED'
}

export interface EnrollmentResponse {
  id: string;
  courseId: string;
  courseTitle: string;
  instructorName: string;
  status: EnrollmentStatus;
  enrolledAt: string;
}
