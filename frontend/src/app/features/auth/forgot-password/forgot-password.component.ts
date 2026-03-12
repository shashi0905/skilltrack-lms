import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

/**
 * Forgot Password Component
 * 
 * Request password reset email.
 */
@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss']
})
export class ForgotPasswordComponent {
  forgotPasswordForm: FormGroup;
  loading = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService
  ) {
    this.forgotPasswordForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.forgotPasswordForm.invalid) {
      return;
    }

    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.authService.forgotPassword(this.forgotPasswordForm.value.email).subscribe({
      next: (response) => {
        this.loading = false;
        this.successMessage = response.message;
        this.forgotPasswordForm.reset();
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.message || 'Failed to send password reset email.';
      }
    });
  }

  get email() { return this.forgotPasswordForm.get('email'); }
}
