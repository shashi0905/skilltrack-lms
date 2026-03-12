import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

/**
 * Verify Email Component
 * 
 * Handles email verification when user clicks link in verification email.
 * Extracts token from URL query params and calls verify API.
 */
@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './verify-email.component.html',
  styleUrls: ['./verify-email.component.scss']
})
export class VerifyEmailComponent implements OnInit {
  loading = true;
  success = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Get token from query params
    const token = this.route.snapshot.queryParams['token'];

    if (!token) {
      this.loading = false;
      this.errorMessage = 'Invalid verification link. No token provided.';
      return;
    }

    // Verify email
    this.authService.verifyEmail(token).subscribe({
      next: () => {
        this.loading = false;
        this.success = true;
        
        // Redirect to login after 3 seconds
        setTimeout(() => {
          this.router.navigate(['/auth/login']);
        }, 3000);
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.message || 'Verification failed. Token may be expired or invalid.';
      }
    });
  }
}
