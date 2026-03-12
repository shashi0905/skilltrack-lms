import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="admin-container">
      <h1>Admin Dashboard</h1>
      <p>This is the admin-only area.</p>
      <p>Only users with ROLE_ADMIN can access this page.</p>
    </div>
  `,
  styles: [`
    .admin-container {
      padding: 40px;
      max-width: 1200px;
      margin: 0 auto;
    }
  `]
})
export class AdminComponent {}
