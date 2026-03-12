import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '@env/environment';
import {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RefreshTokenRequest,
  VerifyEmailRequest,
  ResendVerificationRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  MessageResponse,
  User,
  RoleName
} from '../models/auth.model';

/**
 * Authentication Service
 * 
 * Handles all authentication-related API calls and token management.
 * 
 * Features:
 * - Email/password registration and login
 * - Email verification flow
 * - Password reset flow
 * - GitHub OAuth2 authentication
 * - JWT token management (access + refresh)
 * - User session state management
 * - Role-based access helpers
 * 
 * Token Storage:
 * - Access token: localStorage (for XHR requests)
 * - Refresh token: localStorage (for token refresh)
 * - Current user: BehaviorSubject for reactive state
 * 
 * Security Considerations:
 * - Tokens cleared on logout
 * - Automatic token refresh on 401 (via interceptor)
 * - Password never logged or stored
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}/auth`;
  private readonly TOKEN_KEY = 'accessToken';
  private readonly REFRESH_TOKEN_KEY = 'refreshToken';
  private readonly USER_KEY = 'currentUser';

  // Observable current user for reactive components
  private currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Register new user with email/password.
   * 
   * @param request Registration data
   * @returns UserResponse on success
   */
  register(request: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${this.API_URL}/register`, request);
  }

  /**
   * Login with email/password.
   * 
   * Stores tokens and user data in localStorage on success.
   * Updates currentUser$ observable.
   * 
   * @param request Login credentials
   * @returns LoginResponse with tokens and user data
   */
  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, request)
      .pipe(
        tap(response => {
          this.storeTokens(response.accessToken, response.refreshToken);
          this.storeUser(response.user);
          this.currentUserSubject.next(this.mapUserInfoToUser(response.user));
        })
      );
  }

  /**
   * Logout current user.
   * 
   * Clears all tokens and user data from localStorage.
   * Updates currentUser$ to null.
   * 
   * Note: Backend logout is optional (stateless JWT).
   * Client-side cleanup is sufficient.
   */
  logout(): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.API_URL}/logout`, {})
      .pipe(
        tap(() => {
          this.clearTokens();
          this.currentUserSubject.next(null);
        })
      );
  }

  /**
   * Logout without calling backend (offline logout).
   */
  logoutLocal(): void {
    this.clearTokens();
    this.currentUserSubject.next(null);
  }

  /**
   * Refresh access token using refresh token.
   * 
   * @returns New LoginResponse with fresh access token
   */
  refreshToken(): Observable<LoginResponse> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const request: RefreshTokenRequest = { refreshToken };
    return this.http.post<LoginResponse>(`${this.API_URL}/refresh`, request)
      .pipe(
        tap(response => {
          this.storeTokens(response.accessToken, response.refreshToken);
        })
      );
  }

  /**
   * Verify email with token from email link.
   * 
   * @param token Verification token
   * @returns Updated UserResponse
   */
  verifyEmail(token: string): Observable<User> {
    const request: VerifyEmailRequest = { token };
    return this.http.post<User>(`${this.API_URL}/verify-email`, request);
  }

  /**
   * Resend verification email.
   * 
   * @param email User email
   * @returns Success message
   */
  resendVerification(email: string): Observable<MessageResponse> {
    const request: ResendVerificationRequest = { email };
    return this.http.post<MessageResponse>(`${this.API_URL}/resend-verification`, request);
  }

  /**
   * Request password reset email.
   * 
   * @param email User email
   * @returns Success message (always 200, even if email not found - security)
   */
  forgotPassword(email: string): Observable<MessageResponse> {
    const request: ForgotPasswordRequest = { email };
    return this.http.post<MessageResponse>(`${this.API_URL}/forgot-password`, request);
  }

  /**
   * Reset password with token from email.
   * 
   * @param token Reset token
   * @param newPassword New password
   * @returns Success message
   */
  resetPassword(token: string, newPassword: string): Observable<MessageResponse> {
    const request: ResetPasswordRequest = { token, newPassword };
    return this.http.post<MessageResponse>(`${this.API_URL}/reset-password`, request);
  }

  /**
   * Initiate GitHub OAuth2 login.
   * 
   * Redirects browser to backend OAuth2 endpoint.
   * Backend will redirect to GitHub for authorization.
   * After success, GitHub redirects to backend callback.
   * Backend redirects to frontend OAuth2RedirectComponent with tokens.
   */
  loginWithGitHub(): void {
    window.location.href = `${environment.apiUrl.replace('/api', '')}/oauth2/authorize/github`;
  }

  /**
   * Handle OAuth2 redirect with tokens in URL fragment.
   * Called by OAuth2RedirectComponent.
   * 
   * @param accessToken Access token from URL
   * @param refreshToken Refresh token from URL
   */
  handleOAuth2Tokens(accessToken: string, refreshToken: string): void {
    this.storeTokens(accessToken, refreshToken);
    
    // Fetch user info (optional, or decode from JWT)
    // For now, decode roles from JWT payload
    const user = this.decodeToken(accessToken);
    if (user) {
      this.currentUserSubject.next(user);
    }
  }

  // ============================================
  // Token Management
  // ============================================

  /**
   * Get current access token.
   */
  getAccessToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Get refresh token.
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  /**
   * Check if user is authenticated (has valid token).
   */
  isAuthenticated(): boolean {
    const token = this.getAccessToken();
    if (!token) {
      return false;
    }

    // Check if token is expired
    return !this.isTokenExpired(token);
  }

  /**
   * Get current user (synchronous).
   */
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Check if current user has specific role.
   */
  hasRole(role: RoleName): boolean {
    const user = this.getCurrentUser();
    return user?.roles?.includes(role) ?? false;
  }

  /**
   * Check if current user has any of the roles.
   */
  hasAnyRole(roles: RoleName[]): boolean {
    const user = this.getCurrentUser();
    return roles.some(role => user?.roles?.includes(role));
  }

  /**
   * Check if current user is admin.
   */
  isAdmin(): boolean {
    return this.hasRole(RoleName.ROLE_ADMIN);
  }

  /**
   * Check if current user is instructor.
   */
  isInstructor(): boolean {
    return this.hasRole(RoleName.ROLE_INSTRUCTOR);
  }

  /**
   * Check if current user is student.
   */
  isStudent(): boolean {
    return this.hasRole(RoleName.ROLE_STUDENT);
  }

  // ============================================
  // Private Helpers
  // ============================================

  private storeTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(this.TOKEN_KEY, accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
  }

  private storeUser(user: any): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  private clearTokens(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  }

  private getUserFromStorage(): User | null {
    const userJson = localStorage.getItem(this.USER_KEY);
    if (userJson) {
      try {
        return JSON.parse(userJson);
      } catch {
        return null;
      }
    }
    return null;
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = this.getTokenPayload(token);
      if (!payload || !payload.exp) {
        return true;
      }

      const expirationDate = new Date(payload.exp * 1000);
      return expirationDate <= new Date();
    } catch {
      return true;
    }
  }

  private getTokenPayload(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch {
      return null;
    }
  }

  private decodeToken(token: string): User | null {
    const payload = this.getTokenPayload(token);
    if (!payload) {
      return null;
    }

    // Extract user info from JWT payload
    // Note: This is minimal. In production, fetch full user profile from API
    return {
      id: '', // Not in JWT
      email: payload.sub,
      fullName: '', // Not in JWT
      roles: payload.roles ? payload.roles.split(',') : [],
      emailVerificationStatus: 'VERIFIED' as any,
      createdAt: '',
      updatedAt: ''
    };
  }

  private mapUserInfoToUser(userInfo: any): User {
    return {
      id: userInfo.id,
      email: userInfo.email,
      fullName: userInfo.fullName,
      roles: userInfo.roles,
      emailVerificationStatus: userInfo.emailVerificationStatus,
      instructorVerificationStatus: userInfo.instructorVerificationStatus,
      createdAt: '',
      updatedAt: ''
    };
  }
}
