import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject, filter, take, switchMap, catchError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

/**
 * Error Interceptor
 * 
 * Centralized HTTP error handling for the application.
 * 
 * Features:
 * - Automatic token refresh on 401 Unauthorized
 * - Redirect to login on authentication failure
 * - Handle network errors
 * - Extract and format API error messages
 * 
 * Token Refresh Flow:
 * 1. Request fails with 401
 * 2. Attempt to refresh token
 * 3. Retry original request with new token
 * 4. If refresh fails, logout and redirect to login
 */
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && !request.url.includes('/auth/login')) {
          // Attempt token refresh
          return this.handle401Error(request, next);
        }

        if (error.status === 403) {
          // Forbidden - user lacks permission
          console.error('Access forbidden:', error.error?.message || 'Insufficient permissions');
        }

        if (error.status === 0) {
          // Network error
          console.error('Network error - check if backend is running');
        }

        // Return formatted error
        return throwError(() => this.formatError(error));
      })
    );
  }

  /**
   * Handle 401 Unauthorized by refreshing token.
   */
  private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      const refreshToken = this.authService.getRefreshToken();

      if (refreshToken) {
        return this.authService.refreshToken().pipe(
          switchMap((response) => {
            this.isRefreshing = false;
            this.refreshTokenSubject.next(response.accessToken);
            
            // Retry original request with new token
            return next.handle(this.addTokenHeader(request, response.accessToken));
          }),
          catchError((err) => {
            this.isRefreshing = false;
            
            // Refresh failed - logout and redirect to login
            this.authService.logoutLocal();
            this.router.navigate(['/auth/login'], {
              queryParams: { returnUrl: this.router.url, sessionExpired: true }
            });
            
            return throwError(() => err);
          })
        );
      } else {
        // No refresh token - logout
        this.isRefreshing = false;
        this.authService.logoutLocal();
        this.router.navigate(['/auth/login']);
        return throwError(() => new Error('No refresh token available'));
      }
    } else {
      // Refresh in progress - wait for new token
      return this.refreshTokenSubject.pipe(
        filter(token => token !== null),
        take(1),
        switchMap((token) => {
          return next.handle(this.addTokenHeader(request, token!));
        })
      );
    }
  }

  /**
   * Add authentication token to request.
   */
  private addTokenHeader(request: HttpRequest<any>, token: string): HttpRequest<any> {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  /**
   * Format error response for consistent handling.
   */
  private formatError(error: HttpErrorResponse): any {
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      return {
        message: error.error.message,
        timestamp: new Date().toISOString()
      };
    } else {
      // Server-side error
      return {
        status: error.status,
        message: error.error?.message || error.message || 'An error occurred',
        fieldErrors: error.error?.fieldErrors || [],
        timestamp: error.error?.timestamp || new Date().toISOString()
      };
    }
  }
}
