import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

/**
 * Login Component
 * 
 * Provides email/password login and GitHub OAuth options.
 * 
 * Features:
 * - Email/password login form with validation
 * - GitHub OAuth2 login button
 * - Remember returnUrl for post-login redirect
 * - Display error messages
 * - Links to register and password reset
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  loading = false;
  errorMessage = '';
  returnUrl = '/dashboard';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Build login form
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });

    // Get return URL from query params
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';

    // Check if session expired
    if (this.route.snapshot.queryParams['sessionExpired']) {
      this.errorMessage = 'Your session has expired. Please login again.';
    }
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        this.router.navigateByUrl(this.returnUrl);
      },
      error: (error) => {
        this.errorMessage = error.message || 'Login failed. Please check your credentials.';
        this.loading = false;
      }
    });
  }

  loginWithGitHub(): void {
    // Store return URL in sessionStorage for OAuth2 redirect
    sessionStorage.setItem('oauth2_return_url', this.returnUrl);
    this.authService.loginWithGitHub();
  }

  get email() { return this.loginForm.get('email'); }
  get password() { return this.loginForm.get('password'); }
}
