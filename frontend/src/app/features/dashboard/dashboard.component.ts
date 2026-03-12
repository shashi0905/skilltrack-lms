import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

/**
 * Dashboard Component
 * 
 * Main landing page after successful login.
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
          <p>Your authentication is working perfectly. This is the protected dashboard page.</p>
          
          <div class="user-info">
            <h3>Your Profile</h3>
            <p><strong>Email:</strong> {{ currentUser?.email }}</p>
            <p><strong>Name:</strong> {{ currentUser?.fullName }}</p>
            <p><strong>Roles:</strong> {{ currentUser?.roles?.join(', ') }}</p>
            <p><strong>Email Status:</strong> {{ currentUser?.emailVerificationStatus }}</p>
          </div>

          <div class="actions">
            <p>This is a protected route. Only authenticated users can access this page.</p>
            <p class="mt-3">Future features will include:</p>
            <ul>
              <li>Course catalog</li>
              <li>My enrollments</li>
              <li>Progress tracking</li>
              <li>Instructor dashboard (for instructors)</li>
              <li>Admin panel (for admins)</li>
            </ul>
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

      ul {
        margin: 12px 0 0 24px;
        color: #666;
        line-height: 1.8;
      }
    }

    .user-info {
      background: #f8f9fa;
      padding: 20px;
      border-radius: 8px;
      margin-top: 24px;

      strong {
        color: #333;
      }
    }

    .actions {
      margin-top: 24px;
      padding-top: 24px;
      border-top: 1px solid #eee;
    }

    .mt-3 {
      margin-top: 1.5rem;
    }
  `]
})
export class DashboardComponent {
  currentUser = this.authService.getCurrentUser();

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  logout(): void {
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
