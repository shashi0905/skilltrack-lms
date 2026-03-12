/**
 * Role names matching backend enum.
 */
export enum RoleName {
  ROLE_ADMIN = 'ROLE_ADMIN',
  ROLE_INSTRUCTOR = 'ROLE_INSTRUCTOR',
  ROLE_STUDENT = 'ROLE_STUDENT'
}

/**
 * Email verification status.
 */
export enum EmailVerificationStatus {
  PENDING = 'PENDING',
  VERIFIED = 'VERIFIED'
}

/**
 * Instructor verification status.
 */
export enum InstructorVerificationStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
}

/**
 * User response model matching backend UserResponse DTO.
 */
export interface User {
  id: string;
  email: string;
  fullName: string;
  country?: string;
  organization?: string;
  roles: RoleName[];
  emailVerificationStatus: EmailVerificationStatus;
  instructorVerificationStatus?: InstructorVerificationStatus;
  githubId?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Registration request model.
 */
export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  country?: string;
  organization?: string;
  roleName?: RoleName;
}

/**
 * Login request model.
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * Login response model.
 */
export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: {
    id: string;
    email: string;
    fullName: string;
    roles: RoleName[];
    emailVerificationStatus: EmailVerificationStatus;
    instructorVerificationStatus?: InstructorVerificationStatus;
  };
}

/**
 * Refresh token request.
 */
export interface RefreshTokenRequest {
  refreshToken: string;
}

/**
 * Email verification request.
 */
export interface VerifyEmailRequest {
  token: string;
}

/**
 * Resend verification email request.
 */
export interface ResendVerificationRequest {
  email: string;
}

/**
 * Forgot password request.
 */
export interface ForgotPasswordRequest {
  email: string;
}

/**
 * Reset password request.
 */
export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

/**
 * Generic message response.
 */
export interface MessageResponse {
  message: string;
  success: boolean;
  timestamp: string;
}
