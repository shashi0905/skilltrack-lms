import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

/**
 * Resend Verification Component
 * 
 * Allows users to resend email verification if they didn't receive it.
 */
@Component({
  selector: 'app-resend-verification',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './resend-verification.component.html',
  styleUrls: ['./resend-verification.component.scss']
})
export class ResendVerificationComponent {
  resendForm: FormGroup;
  loading = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService
  ) {
    this.resendForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.resendForm.invalid) {
      return;
    }

    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.authService.resendVerification(this.resendForm.value.email).subscribe({
      next: (response) => {
        this.loading = false;
        this.successMessage = response.message;
        this.resendForm.reset();
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.message || 'Failed to resend verification email.';
      }
    });
  }

  get email() { return this.resendForm.get('email'); }
}
