import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { RoleName } from '../models/auth.model';

/**
 * Role Guard
 * 
 * Protects routes based on user roles.
 * 
 * Usage in routing:
 * ```typescript
 * {
 *   path: 'admin',
 *   component: AdminComponent,
 *   canActivate: [RoleGuard],
 *   data: { roles: [RoleName.ROLE_ADMIN] }
 * }
 * ```
 * 
 * Behavior:
 * - Check if user is authenticated
 * - Check if user has required role(s)
 * - Redirect to login if not authenticated
 * - Redirect to forbidden page if authenticated but no permission
 */
@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    
    // Check authentication
    if (!this.authService.isAuthenticated()) {
      return this.router.createUrlTree(['/auth/login'], {
        queryParams: { returnUrl: state.url }
      });
    }

    // Get required roles from route data
    const requiredRoles = route.data['roles'] as RoleName[];
    if (!requiredRoles || requiredRoles.length === 0) {
      return true; // No specific role required
    }

    // Check if user has required role(s)
    if (this.authService.hasAnyRole(requiredRoles)) {
      return true;
    }

    // User doesn't have required role - redirect to forbidden
    console.error('Access denied: Insufficient permissions');
    return this.router.createUrlTree(['/forbidden']);
  }
}
