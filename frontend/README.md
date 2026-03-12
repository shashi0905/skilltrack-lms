# SkillTrack Frontend

Angular 17 frontend application for SkillTrack Learning Management System.

## Prerequisites

- Node.js 18+ and npm
- Angular CLI 17+

## Installation

```bash
npm install
```

## Development Server

```bash
npm start
# or
ng serve
```

Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

## Build

```bash
npm run build
```

Build artifacts will be stored in the `dist/` directory.

## Running Tests

```bash
npm test
```

## Project Structure

```
src/
├── app/
│   ├── core/          # Singleton services, guards, interceptors
│   │   ├── guards/
│   │   ├── interceptors/
│   │   ├── models/
│   │   └── services/
│   ├── features/      # Feature modules
│   │   ├── auth/      # Authentication feature
│   │   └── ...
│   ├── shared/        # Shared components, directives, pipes
│   └── app.component.ts
├── assets/
├── environments/
└── styles.scss
```

## Authentication Features

- ✅ Email/Password Registration
- ✅ Email Verification
- ✅ Login & Logout
- ✅ Password Reset Flow
- ✅ GitHub OAuth Integration
- ✅ JWT Token Management
- ✅ Role-based Route Guards

## Backend API

Default backend URL: `http://localhost:8080`

Configure in `src/environments/environment.ts`
