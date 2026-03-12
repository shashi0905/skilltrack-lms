import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

/**
 * OAuth2 Redirect Component
 * 
 * Handles OAuth2 callback after GitHub authentication.
 * Extracts tokens from URL fragment and stores them.
 * 
 * URL Format: /oauth2/redirect#access_token=...&refresh_token=...&token_type=Bearer
 */
@Component({
  selector: 'app-oauth2-redirect',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="oauth2-redirect-container">
      <div class="oauth2-redirect-card">
        <div *ngIf="processing" class="loading">
          <div class="spinner"></div>
          <h2>Processing login...</h2>
          <p>Please wait while we complete your authentication.</p>
        </div>

        <div *ngIf="error" class="error">
          <div class="icon-error">✗</div>
          <h2>Authentication Failed</h2>
          <p>{{ errorMessage }}</p>
          <a routerLink="/auth/login" class="btn btn-primary">Back to Login</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .oauth2-redirect-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 20px;
    }

    .oauth2-redirect-card {
      background: white;
      border-radius: 12px;
      padding: 50px;
      max-width: 500px;
      width: 100%;
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
      text-align: center;
    }

    .loading .spinner {
      width: 50px;
      height: 50px;
      margin: 0 auto 20px;
      border: 4px solid #f3f3f3;
      border-top: 4px solid #667eea;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    .error .icon-error {
      width: 70px;
      height: 70px;
      margin: 0 auto 20px;
      background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
      color: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 40px;
      font-weight: bold;
    }

    h2 {
      margin: 20px 0 16px 0;
      font-size: 26px;
      font-weight: 600;
      color: #333;
    }

    p {
      margin: 0 0 16px 0;
      color: #666;
      line-height: 1.6;
    }

    .btn {
      display: inline-block;
      padding: 12px 28px;
      font-size: 15px;
      font-weight: 600;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      text-decoration: none;
      transition: all 0.2s;
      margin-top: 16px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }

    .btn:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }
  `]
})
export class OAuth2RedirectComponent implements OnInit {
  processing = true;
  error = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Check for error in query params
    const urlParams = new URLSearchParams(window.location.search);
    const errorParam = urlParams.get('error');

    if (errorParam) {
      this.processing = false;
      this.error = true;
      this.errorMessage = 'GitHub authentication failed. Please try again.';
      return;
    }

    // Extract tokens from URL fragment
    const fragment = window.location.hash.substring(1);
    const params = new URLSearchParams(fragment);

    const accessToken = params.get('access_token');
    const refreshToken = params.get('refresh_token');

    if (accessToken && refreshToken) {
      // Store tokens
      this.authService.handleOAuth2Tokens(accessToken, refreshToken);

      // Get return URL from sessionStorage
      const returnUrl = sessionStorage.getItem('oauth2_return_url') || '/dashboard';
      sessionStorage.removeItem('oauth2_return_url');

      // Redirect to return URL
      setTimeout(() => {
        this.router.navigateByUrl(returnUrl);
      }, 1000);
    } else {
      this.processing = false;
      this.error = true;
      this.errorMessage = 'No authentication tokens received. Please try again.';
    }
  }
}
