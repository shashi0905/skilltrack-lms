import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

/**
 * Reset Password Component
 * 
 * Reset password with token from email link.
 */
@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {
  resetPasswordForm!: FormGroup;
  loading = false;
  successMessage = '';
  errorMessage = '';
  token = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Get token from query params
    this.token = this.route.snapshot.queryParams['token'];

    if (!this.token) {
      this.errorMessage = 'Invalid reset link. No token provided.';
    }

    this.resetPasswordForm = this.formBuilder.group({
      newPassword: ['', [Validators.required, Validators.minLength(8), this.passwordValidator]],
      confirmPassword: ['', Validators.required]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  onSubmit(): void {
    if (this.resetPasswordForm.invalid || !this.token) {
      return;
    }

    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.authService.resetPassword(this.token, this.resetPasswordForm.value.newPassword).subscribe({
      next: (response) => {
        this.loading = false;
        this.successMessage = response.message;
        
        // Redirect to login after 3 seconds
        setTimeout(() => {
          this.router.navigate(['/auth/login']);
        }, 3000);
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.message || 'Password reset failed. Token may be expired.';
      }
    });
  }

  private passwordValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.value;
    if (!password) {
      return null;
    }

    const hasUpperCase = /[A-Z]/.test(password);
    const hasLowerCase = /[a-z]/.test(password);
    const hasDigit = /\d/.test(password);
    const hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password);

    const valid = hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;

    if (!valid) {
      return { passwordStrength: true };
    }

    return null;
  }

  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('newPassword');
    const confirmPassword = control.get('confirmPassword');

    if (!password || !confirmPassword) {
      return null;
    }

    return password.value === confirmPassword.value ? null : { passwordMismatch: true };
  }

  get newPassword() { return this.resetPasswordForm.get('newPassword'); }
  get confirmPassword() { return this.resetPasswordForm.get('confirmPassword'); }
}
