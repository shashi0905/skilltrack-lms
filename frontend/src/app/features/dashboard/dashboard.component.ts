import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { RoleName } from '@core/models/auth.model';

/**
 * Dashboard Component
 * 
 * Main landing page after successful login.
 * Redirects users to appropriate dashboards based on their roles.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="dashboard-container">
      <nav class="navbar">
        <div class="navbar-brand">
          <h1>SkillTrack LMS</h1>
        </div>
        <div class="navbar-menu">
          <span class="user-name">Welcome, {{ currentUser?.fullName || currentUser?.email }}</span>
          <button (click)="logout()" class="btn btn-logout">Logout</button>
        </div>
      </nav>

      <div class="dashboard-content">
        <div class="dashboard-card">
          <h2>Welcome to SkillTrack!</h2>
          
          <div class="user-info">
            <h3>Your Profile</h3>
            <p><strong>Email:</strong> {{ currentUser?.email }}</p>
            <p><strong>Name:</strong> {{ currentUser?.fullName }}</p>
            <p><strong>Roles:</strong> {{ currentUser?.roles?.join(', ') }}</p>
            <p><strong>Email Status:</strong> {{ currentUser?.emailVerificationStatus }}</p>
          </div>

          <div class="role-actions">
            @if (isInstructor()) {
              <div class="action-section">
                <h3>Instructor Dashboard</h3>
                <p>Manage your courses, create new content, and track your teaching progress.</p>
                <button (click)="goToInstructorDashboard()" class="btn btn-primary">
                  Go to Course Management
                </button>
              </div>
            }

            @if (isAdmin()) {
              <div class="action-section">
                <h3>Admin Panel</h3>
                <p>Manage users, courses, and system settings.</p>
                <button (click)="goToAdmin()" class="btn btn-primary">
                  Go to Admin Panel
                </button>
              </div>
            }

            @if (isStudent()) {
              <div class="action-section">
                <h3>Student Dashboard</h3>
                <p>Browse courses, track your learning progress, and manage enrollments.</p>
                <button (click)="goToCourseCatalog()" class="btn btn-primary">
                  Browse Course Catalog
                </button>
                <!-- Debug info -->
                <div style="margin-top: 10px; font-size: 12px; color: #666;">
                  <p>Debug: User roles = {{ currentUser?.roles?.join(', ') }}</p>
                  <p>If you're not redirected automatically, click the button above.</p>
                </div>
              </div>
            }

            @if (!hasAnyRole()) {
              <div class="action-section">
                <h3>Getting Started</h3>
                <p>It looks like you don't have any roles assigned yet. Please contact an administrator.</p>
              </div>
            }
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      min-height: 100vh;
      background: #f5f5f5;
    }

    .navbar {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 16px 24px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

      h1 {
        margin: 0;
        font-size: 24px;
        font-weight: 600;
      }
    }

    .navbar-menu {
      display: flex;
      align-items: center;
      gap: 20px;

      .user-name {
        font-size: 14px;
      }
    }

    .btn-logout {
      background: rgba(255, 255, 255, 0.2);
      color: white;
      border: 1px solid rgba(255, 255, 255, 0.3);
      padding: 8px 16px;
      border-radius: 6px;
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: rgba(255, 255, 255, 0.3);
      }
    }

    .dashboard-content {
      padding: 40px 20px;
      max-width: 800px;
      margin: 0 auto;
    }

    .dashboard-card {
      background: white;
      border-radius: 12px;
      padding: 40px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

      h2 {
        margin: 0 0 16px 0;
        font-size: 28px;
        font-weight: 600;
        color: #333;
      }

      h3 {
        margin: 24px 0 12px 0;
        font-size: 20px;
        font-weight: 600;
        color: #333;
      }

      p {
        margin: 0 0 12px 0;
        color: #666;
        line-height: 1.6;
      }
    }

    .user-info {
      background: #f8f9fa;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 24px;

      strong {
        color: #333;
      }
    }

    .role-actions {
      margin-top: 24px;
    }

    .action-section {
      background: #f8f9fa;
      padding: 24px;
      border-radius: 8px;
      margin-bottom: 16px;
      border-left: 4px solid #1976d2;

      h3 {
        margin: 0 0 12px 0;
        color: #1976d2;
      }

      p {
        margin-bottom: 16px;
      }
    }

    .btn-primary {
      background: #1976d2;
      color: white;
      border: none;
      padding: 12px 24px;
      border-radius: 6px;
      font-size: 16px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: #1565c0;
        transform: translateY(-1px);
      }
    }

    .coming-soon {
      background: #fff3e0;
      padding: 12px;
      border-radius: 4px;
      border-left: 4px solid #ff9800;

      p {
        margin: 0;
        color: #e65100;
      }
    }
  `]
})
export class DashboardComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected currentUser = this.authService.getCurrentUser();

  ngOnInit(): void {
    // Debug logging
    console.log('Dashboard - Current user:', this.currentUser);
    console.log('Dashboard - Is instructor:', this.isInstructor());
    console.log('Dashboard - Is student:', this.isStudent());
    console.log('Dashboard - Is admin:', this.isAdmin());
    
    // Auto-redirect users to their appropriate dashboards
    if (this.isInstructor()) {
      console.log('Dashboard - Redirecting to instructor dashboard');
      this.router.navigate(['/instructor/dashboard']);
    } else if (this.isStudent()) {
      console.log('Dashboard - Redirecting to student course catalog');
      this.router.navigate(['/student/courses']);
    }
  }

  protected isInstructor(): boolean {
    return this.authService.hasRole(RoleName.ROLE_INSTRUCTOR);
  }

  protected isAdmin(): boolean {
    return this.authService.hasRole(RoleName.ROLE_ADMIN);
  }

  protected isStudent(): boolean {
    return this.authService.hasRole(RoleName.ROLE_STUDENT);
  }

  protected hasAnyRole(): boolean {
    const user = this.currentUser;
    return !!(user?.roles && user.roles.length > 0);
  }

  protected goToInstructorDashboard(): void {
    this.router.navigate(['/instructor/dashboard']);
  }

  protected goToAdmin(): void {
    this.router.navigate(['/admin']);
  }

  protected goToCourseCatalog(): void {
    this.router.navigate(['/student/courses']);
  }

  protected logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/auth/login']);
      },
      error: () => {
        // Even if server logout fails, do local logout
        this.authService.logoutLocal();
        this.router.navigate(['/auth/login']);
      }
    });
  }
}
