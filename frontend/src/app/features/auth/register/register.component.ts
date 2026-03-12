import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { RoleName } from '@core/models/auth.model';

/**
 * Register Component
 * 
 * New user registration form.
 * 
 * Features:
 * - Email/password registration
 * - Full name, country, organization fields
 * - Role selection (Student/Instructor)
 * - Password strength validation
 * - Confirm password matching
 * - After registration, redirect to verification message
 */
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  loading = false;
  errorMessage = '';
  successMessage = '';

  roles = [
    { value: RoleName.ROLE_STUDENT, label: 'Student' },
    { value: RoleName.ROLE_INSTRUCTOR, label: 'Instructor' }
  ];

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.registerForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8), this.passwordValidator]],
      confirmPassword: ['', Validators.required],
      fullName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      country: [''],
      organization: [''],
      roleName: [RoleName.ROLE_STUDENT, Validators.required]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const { confirmPassword, ...registration } = this.registerForm.value;

    this.authService.register(registration).subscribe({
      next: () => {
        this.successMessage = 'Registration successful! Please check your email to verify your account.';
        this.registerForm.reset();
        this.loading = false;
        
        // Redirect to verification message page after 2 seconds
        setTimeout(() => {
          this.router.navigate(['/auth/verify-email-sent']);
        }, 2000);
      },
      error: (error) => {
        this.errorMessage = error.message || 'Registration failed. Please try again.';
        this.loading = false;
      }
    });
  }

  /**
   * Custom password validator for strength requirements.
   */
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

  /**
   * Validator to ensure password and confirmPassword match.
   */
  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');

    if (!password || !confirmPassword) {
      return null;
    }

    return password.value === confirmPassword.value ? null : { passwordMismatch: true };
  }

  get email() { return this.registerForm.get('email'); }
  get password() { return this.registerForm.get('password'); }
  get confirmPassword() { return this.registerForm.get('confirmPassword'); }
  get fullName() { return this.registerForm.get('fullName'); }
}
