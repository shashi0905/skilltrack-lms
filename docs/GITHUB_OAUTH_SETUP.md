# GitHub OAuth Setup Guide

## Step 1: Create GitHub OAuth App

1. Go to GitHub Settings: https://github.com/settings/developers
2. Click "OAuth Apps" → "New OAuth App"
3. Fill in application details:
   - **Application name**: SkillTrack LMS (or your app name)
   - **Homepage URL**: `http://localhost:4200` (for development)
   - **Authorization callback URL**: `http://localhost:8080/login/oauth2/code/github`
   - **Application description**: Learning Management System (optional)
4. Click "Register application"
5. Copy **Client ID** and **Client Secret**

## Step 2: Configure Backend

Add to `backend/skilltrack-api/src/main/resources/application.yml` or environment variables:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: YOUR_CLIENT_ID
            client-secret: YOUR_CLIENT_SECRET
```

Or set environment variables:
```bash
export GITHUB_CLIENT_ID=your_actual_client_id
export GITHUB_CLIENT_SECRET=your_actual_client_secret
```

## Step 3: Frontend Integration

### Login Button
```typescript
// Angular component
loginWithGitHub() {
  // Redirect to Spring Security OAuth2 endpoint
  window.location.href = 'http://localhost:8080/oauth2/authorize/github';
}
```

```html
<!-- Login page -->
<button (click)="loginWithGitHub()">
  <img src="assets/github-icon.svg" alt="GitHub" />
  Login with GitHub
</button>
```

### Handle OAuth2 Redirect
```typescript
// oauth2-redirect.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-oauth2-redirect',
  template: '<div>Processing login...</div>'
})
export class OAuth2RedirectComponent implements OnInit {
  
  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    // Extract tokens from URL fragment
    const fragment = window.location.hash.substring(1);
    const params = new URLSearchParams(fragment);
    
    const accessToken = params.get('access_token');
    const refreshToken = params.get('refresh_token');
    const error = params.get('error');

    if (error) {
      console.error('OAuth2 error:', error);
      this.router.navigate(['/login'], { 
        queryParams: { error: 'oauth_failed' } 
      });
      return;
    }

    if (accessToken && refreshToken) {
      // Store tokens
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      
      // Redirect to dashboard
      this.router.navigate(['/dashboard']);
    } else {
      this.router.navigate(['/login']);
    }
  }
}
```

### Route Configuration
```typescript
// app-routing.module.ts
const routes: Routes = [
  {
    path: 'oauth2/redirect',
    component: OAuth2RedirectComponent
  },
  // ... other routes
];
```

## OAuth2 Flow Diagram

```
┌─────────────┐
│   User      │
│   Browser   │
└──────┬──────┘
       │
       │ 1. Click "Login with GitHub"
       ▼
┌─────────────────────────────────────────┐
│   Frontend (Angular)                    │
│   Redirect to:                          │
│   /oauth2/authorize/github              │
└──────┬──────────────────────────────────┘
       │
       │ 2. Redirect
       ▼
┌─────────────────────────────────────────┐
│   Backend (Spring Security)             │
│   Redirect to GitHub with:              │
│   - client_id                           │
│   - redirect_uri                        │
│   - scope: user:email, read:user        │
└──────┬──────────────────────────────────┘
       │
       │ 3. Redirect to GitHub
       ▼
┌─────────────────────────────────────────┐
│   GitHub Authorization Page             │
│   User authorizes app                   │
└──────┬──────────────────────────────────┘
       │
       │ 4. Redirect back with auth code
       ▼
┌─────────────────────────────────────────┐
│   Backend (Spring Security)             │
│   - Exchange code for access token      │
│   - Fetch user profile from GitHub      │
│   - Create/link user account            │
│   - Generate JWT tokens                 │
│   - Redirect to frontend with tokens    │
└──────┬──────────────────────────────────┘
       │
       │ 5. Redirect with tokens in fragment
       ▼
┌─────────────────────────────────────────┐
│   Frontend (OAuth2RedirectComponent)    │
│   - Extract tokens from URL fragment    │
│   - Store in localStorage               │
│   - Redirect to dashboard               │
└─────────────────────────────────────────┘
```

## Security Considerations

1. **HTTPS in Production**
   - Use HTTPS for production (required by GitHub)
   - Update callback URL: `https://yourdomain.com/login/oauth2/code/github`

2. **Token Security**
   - Tokens passed via URL fragment (not query params)
   - Fragment not sent to server (client-side only)
   - Store tokens securely (consider httpOnly cookies)

3. **CORS Configuration**
   - Backend allows frontend origin
   - Frontend sends tokens in Authorization header

4. **Environment Variables**
   - Never commit client secret to version control
   - Use environment variables or secure vault

## Testing OAuth2 Login

1. Start backend: `mvn spring-boot:run`
2. Start frontend: `ng serve`
3. Navigate to: `http://localhost:4200/login`
4. Click "Login with GitHub"
5. Authorize app on GitHub
6. Should redirect back with tokens
7. Check localStorage for tokens
8. Verify user created in database

## Troubleshooting

### Error: redirect_uri_mismatch
- Check callback URL in GitHub app settings
- Must be: `http://localhost:8080/login/oauth2/code/github`
- Port must match backend server

### Error: Unauthorized (401)
- Check client ID and secret
- Verify environment variables loaded
- Check GitHub app is not suspended

### User Email is Null
- GitHub email may be private
- Request `user:email` scope
- User must make email public or use login name

## Production Deployment

1. Update GitHub OAuth app:
   - Homepage URL: `https://yourdomain.com`
   - Callback URL: `https://yourdomain.com/login/oauth2/code/github`

2. Update application.yml:
   ```yaml
   app:
     oauth2:
       redirect-uri: https://yourdomain.com/oauth2/redirect
   ```

3. Use environment variables for secrets
4. Enable HTTPS
5. Update CORS origins to production domain
