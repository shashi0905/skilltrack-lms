# SkillTrack Frontend - Angular Implementation Guide

## Overview

Complete Angular 17 frontend implementation for SkillTrack LMS Phase 1 authentication features.

## Features Implemented

✅ **Email/Password Registration**
- Full name, email, password with validation
- Country and organization (optional)
- Role selection (Student/Instructor)
- Password strength requirements

✅ **Email Verification**
- Email verification via token link
- Resend verification email
- Success/error handling

✅ **Login & Logout**
- Email/password authentication
- JWT token management (access + refresh)
- Automatic token refresh on 401
- Session expiry handling

✅ **Password Reset**
- Forgot password flow
- Reset password with token
- Password strength validation

✅ **GitHub OAuth2**
- One-click GitHub login
- OAuth2 callback handling
- Automatic token extraction

✅ **Security Features**
- JWT interceptor for auth headers
- Error interceptor with auto-refresh
- Auth guard for protected routes
- Role guard for role-based access
- Token storage in localStorage

## Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── core/                       # Core services, guards, interceptors
│   │   │   ├── guards/
│   │   │   │   ├── auth.guard.ts       # Authentication guard
│   │   │   │   └── role.guard.ts       # Role-based guard
│   │   │   ├── interceptors/
│   │   │   │   ├── jwt.interceptor.ts  # JWT token attachment
│   │   │   │   └── error.interceptor.ts # Error handling & token refresh
│   │   │   ├── models/
│   │   │   │   ├── auth.model.ts       # Auth interfaces & enums
│   │   │   │   └── error.model.ts      # Error models
│   │   │   └── services/
│   │   │       └── auth.service.ts     # Authentication service
│   │   ├── features/
│   │   │   ├── auth/
│   │   │   │   ├── login/              # Login component
│   │   │   │   ├── register/           # Registration component
│   │   │   │   ├── verify-email/       # Email verification
│   │   │   │   ├── resend-verification/ # Resend verification
│   │   │   │   ├── forgot-password/    # Forgot password
│   │   │   │   ├── reset-password/     # Reset password
│   │   │   │   └── oauth2-redirect/    # OAuth2 callback handler
│   │   │   ├── dashboard/              # Protected dashboard
│   │   │   ├── admin/                  # Admin area (role-protected)
│   │   │   └── forbidden/              # 403 Forbidden page
│   │   ├── app.component.ts            # Root component
│   │   ├── app.routes.ts               # Route configuration
│   │   └── app.config.ts               # App configuration
│   ├── environments/
│   │   ├── environment.ts              # Development config
│   │   └── environment.prod.ts         # Production config
│   ├── index.html
│   ├── main.ts
│   └── styles.scss
├── angular.json
├── package.json
├── tsconfig.json
├── tsconfig.app.json
└── README.md
```

## Installation & Setup

### Prerequisites
- Node.js 18+ and npm
- Angular CLI 17+

### Install Dependencies

```bash
cd frontend
npm install
```

### Install Angular CLI (if not installed)

```bash
npm install -g @angular/cli@17
```

### Configuration

Update `src/environments/environment.ts` if needed:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',  // Backend API URL
  oauth2RedirectUri: 'http://localhost:4200/oauth2/redirect'
};
```

### Run Development Server

```bash
npm start
# or
ng serve
```

Navigate to `http://localhost:4200/`

### Build for Production

```bash
ng build --configuration production
```

Build artifacts will be in `dist/skilltrack-frontend/`

## Component Details

### 1. Core Services

#### AuthService (`auth.service.ts`)
- **Functions**: All authentication API calls
- **Token Management**: Access token, refresh token storage
- **User State**: BehaviorSubject for reactive current user
- **Role Helpers**: `hasRole()`, `isAdmin()`, `isInstructor()`, etc.

**Key Methods:**
```typescript
login(request: LoginRequest): Observable<LoginResponse>
register(request: RegisterRequest): Observable<User>
logout(): Observable<MessageResponse>
refreshToken(): Observable<LoginResponse>
verifyEmail(token: string): Observable<User>
forgotPassword(email: string): Observable<MessageResponse>
resetPassword(token: string, newPassword: string): Observable<MessageResponse>
loginWithGitHub(): void
```

### 2. HTTP Interceptors

#### JwtInterceptor
- Automatically attaches `Authorization: Bearer <token>` to all requests
- Uses token from AuthService

#### ErrorInterceptor
- Catches HTTP errors globally
- Automatically refreshes token on 401 Unauthorized
- Retries failed request with new token
- Redirects to login if refresh fails
- Formats error messages consistently

### 3. Guards

#### AuthGuard
- Protects routes requiring authentication
- Redirects to login with returnUrl if not authenticated

**Usage:**
```typescript
{
  path: 'dashboard',
  component: DashboardComponent,
  canActivate: [AuthGuard]
}
```

#### RoleGuard
- Protects routes based on user roles
- Checks if user has required role(s)
- Redirects to forbidden page if insufficient permissions

**Usage:**
```typescript
{
  path: 'admin',
  component: AdminComponent,
  canActivate: [RoleGuard],
  data: { roles: [RoleName.ROLE_ADMIN] }
}
```

### 4. Authentication Components

#### LoginComponent
- Email/password login form
- GitHub OAuth button
- Links to register and password reset
- Remember returnUrl for redirect after login

#### RegisterComponent
- Registration form with all fields
- Password strength validation
- Confirm password matching
- Role selection (Student/Instructor)
- Redirect to verification message on success

#### VerifyEmailComponent
- Extracts token from URL query params
- Calls verify API
- Shows success/error message
- Auto-redirect to login on success

#### ResendVerificationComponent
- Simple form with email field
- Resends verification email
- Security: Always returns success (even if email not found)

#### ForgotPasswordComponent
- Email input for password reset
- Sends reset email
- Security: Always returns success message

#### ResetPasswordComponent
- New password form with confirmation
- Password strength validation
- Extracts token from URL
- Redirects to login on success

#### OAuth2RedirectComponent
- Extracts tokens from URL fragment
- Stores tokens using AuthService
- Retrieves returnUrl from sessionStorage
- Redirects to original destination

### 5. Protected Components

#### DashboardComponent
- Main landing page after login
- Displays user profile
- Logout button
- Protected by AuthGuard

#### AdminComponent
- Admin-only area
- Protected by RoleGuard with ROLE_ADMIN

#### ForbiddenComponent
- 403 Access Denied page
- Shows when user lacks permission

## Routing

Routes defined in `app.routes.ts`:

```typescript
/                           → Redirect to /auth/login
/auth/login                → Login page
/auth/register             → Registration page
/auth/verify-email         → Email verification (with ?token=...)
/auth/resend-verification  → Resend verification email
/auth/forgot-password      → Forgot password
/auth/reset-password       → Reset password (with ?token=...)
/oauth2/redirect           → OAuth2 callback handler
/dashboard                 → Protected dashboard (requires auth)
/admin                     → Admin area (requires ROLE_ADMIN)
/forbidden                 → 403 Forbidden page
```

## Styling

- **Global Styles**: `src/styles.scss`
- **Component Styles**: Component-specific SCSS files
- **Theme**: Purple gradient (#667eea → #764ba2)
- **Responsive**: Mobile-friendly layouts

## Security Considerations

### Token Storage
- **Access Token**: localStorage (`accessToken`)
- **Refresh Token**: localStorage (`refreshToken`)
- **Current User**: localStorage (`currentUser`)

**Note**: In production, consider httpOnly cookies for enhanced security.

### Password Validation
Client-side validation (mirroring backend):
- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 digit
- At least 1 special character

### CORS Configuration
Backend must allow frontend origin:
```yaml
# Backend application.yml
spring:
  web:
    cors:
      allowed-origins: http://localhost:4200
```

## Testing the Application

### 1. Start Backend
```bash
cd backend
mvn spring-boot:run
```

### 2. Start Frontend
```bash
cd frontend
npm start
```

### 3. Test Flows

#### Registration Flow
1. Navigate to `http://localhost:4200/auth/register`
2. Fill in registration form
3. Submit
4. Check email for verification link
5. Click verification link
6. Login with credentials

#### Login Flow
1. Navigate to `http://localhost:4200/auth/login`
2. Enter email and password
3. Submit
4. Redirected to dashboard

#### GitHub OAuth Flow
1. Navigate to `http://localhost:4200/auth/login`
2. Click "Login with GitHub"
3. Authorize on GitHub
4. Redirected to dashboard with tokens

#### Password Reset Flow
1. Navigate to `http://localhost:4200/auth/forgot-password`
2. Enter email
3. Check email for reset link
4. Click reset link
5. Enter new password
6. Login with new password

### 4. Check Browser DevTools
- **Network Tab**: Verify API calls to `http://localhost:8080/api/auth/*`
- **Application Tab**: Check localStorage for tokens
- **Console**: Check for errors

## API Integration

### Backend Endpoints Used

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/register` | POST | Register new user |
| `/api/auth/login` | POST | Login with email/password |
| `/api/auth/logout` | POST | Logout (optional) |
| `/api/auth/refresh` | POST | Refresh access token |
| `/api/auth/verify-email` | POST | Verify email with token |
| `/api/auth/resend-verification` | POST | Resend verification email |
| `/api/auth/forgot-password` | POST | Request password reset |
| `/api/auth/reset-password` | POST | Reset password with token |
| `/oauth2/authorize/github` | GET | Start GitHub OAuth flow |
| `/login/oauth2/code/github` | GET | GitHub OAuth callback |

### Request/Response Models

Defined in `src/app/core/models/auth.model.ts`:
- `RegisterRequest`
- `LoginRequest` / `LoginResponse`
- `RefreshTokenRequest`
- `VerifyEmailRequest`
- `ResendVerificationRequest`
- `ForgotPasswordRequest`
- `ResetPasswordRequest`
- `MessageResponse`
- `User` (response)

## Environment Variables

### Development (`environment.ts`)
```typescript
{
  production: false,
  apiUrl: 'http://localhost:8080/api',
  oauth2RedirectUri: 'http://localhost:4200/oauth2/redirect'
}
```

### Production (`environment.prod.ts`)
```typescript
{
  production: true,
  apiUrl: '/api',  // Relative URL for same domain
  oauth2RedirectUri: window.location.origin + '/oauth2/redirect'
}
```

## Docker Deployment (Future)

Create `Dockerfile`:

```dockerfile
# Build stage
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration production

# Serve stage
FROM nginx:alpine
COPY --from=build /app/dist/skilltrack-frontend /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

Create `nginx.conf`:

```nginx
server {
  listen 80;
  server_name localhost;
  root /usr/share/nginx/html;
  index index.html;

  location / {
    try_files $uri $uri/ /index.html;
  }

  location /api {
    proxy_pass http://backend:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
  }
}
```

## Troubleshooting

### CORS Errors
**Problem**: Browser blocks API requests

**Solution**: Ensure backend CORS configuration allows frontend origin:
```yaml
spring.web.cors.allowed-origins: http://localhost:4200
```

### 401 Unauthorized Loop
**Problem**: Infinite redirect to login

**Solution**: Check if token refresh is working correctly. Clear localStorage and re-login.

### OAuth2 Redirect Not Working
**Problem**: OAuth2 callback fails

**Solution**: 
1. Check GitHub app callback URL: `http://localhost:8080/login/oauth2/code/github`
2. Verify backend OAuth2 client configuration
3. Check frontend OAuth2 redirect URI matches backend config

### Token Expiry Not Handled
**Problem**: User not redirected on token expiry

**Solution**: ErrorInterceptor should handle 401 and refresh token automatically. Check interceptor registration in `app.config.ts`.

## Next Steps

With frontend complete, you can now:

1. **Test End-to-End**: Full registration → verification → login → dashboard flow
2. **Add Course Features**: Implement Phase 2 (Course Management)
3. **Enhance UI**: Add loading spinners, better error messages, toast notifications
4. **Add Analytics**: Track user actions with analytics service
5. **Implement Notifications**: Real-time notifications for email verification, etc.

## Summary

**Frontend Implementation Complete!** ✅

- 🎨 Modern Angular 17 with standalone components
- 🔐 Complete authentication system
- 🛡️ JWT token management with auto-refresh
- 🎯 Role-based access control
- 🌐 GitHub OAuth2 integration
- 📱 Responsive design
- ⚡ Optimized routing with lazy loading

**Total Files Created**: 40+ components, services, guards, interceptors

Ready for production deployment or Phase 2 development!
