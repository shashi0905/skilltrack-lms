import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Auth Guard
 * 
 * Protects routes that require authentication.
 * 
 * Usage in routing:
 * ```typescript
 * {
 *   path: 'dashboard',
 *   component: DashboardComponent,
 *   canActivate: [AuthGuard]
 * }
 * ```
 * 
 * Behavior:
 * - If user is authenticated: Allow access
 * - If not authenticated: Redirect to login with returnUrl
 */
@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    
    if (this.authService.isAuthenticated()) {
      return true;
    }

    // Not authenticated - redirect to login with return URL
    return this.router.createUrlTree(['/auth/login'], {
      queryParams: { returnUrl: state.url }
    });
  }
}
