import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * JWT Interceptor
 * 
 * Automatically attaches JWT access token to outgoing HTTP requests.
 * 
 * Behavior:
 * - Adds Authorization header with Bearer token to all API requests
 * - Skips token attachment for public endpoints (login, register, etc.)
 * - Token retrieved from AuthService
 * 
 * Usage: Registered in app.config.ts or app.module.ts
 */
@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Get access token
    const token = this.authService.getAccessToken();

    // Clone request and add Authorization header if token exists
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request);
  }
}
