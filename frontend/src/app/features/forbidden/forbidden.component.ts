import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-forbidden',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="forbidden-container">
      <div class="forbidden-card">
        <div class="icon">🔒</div>
        <h1>Access Denied</h1>
        <p>You don't have permission to access this page.</p>
        <a routerLink="/dashboard" class="btn">Go to Dashboard</a>
      </div>
    </div>
  `,
  styles: [`
    .forbidden-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 20px;
    }

    .forbidden-card {
      background: white;
      border-radius: 12px;
      padding: 60px 40px;
      text-align: center;
      max-width: 500px;
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);

      .icon {
        font-size: 80px;
        margin-bottom: 20px;
      }

      h1 {
        margin: 0 0 16px 0;
        font-size: 32px;
        font-weight: 600;
        color: #333;
      }

      p {
        margin: 0 0 32px 0;
        color: #666;
        font-size: 16px;
      }

      .btn {
        display: inline-block;
        padding: 14px 32px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        border-radius: 6px;
        font-weight: 600;
        text-decoration: none;
        transition: all 0.2s;

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
        }
      }
    }
  `]
})
export class ForbiddenComponent {}
