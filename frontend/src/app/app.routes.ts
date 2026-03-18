import { Routes } from '@angular/router';
import { AuthGuard } from '@core/guards/auth.guard';
import { RoleGuard } from '@core/guards/role.guard';
import { RoleName } from '@core/models/auth.model';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'auth',
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
      },
      {
        path: 'verify-email',
        loadComponent: () => import('./features/auth/verify-email/verify-email.component').then(m => m.VerifyEmailComponent)
      },
      {
        path: 'resend-verification',
        loadComponent: () => import('./features/auth/resend-verification/resend-verification.component').then(m => m.ResendVerificationComponent)
      },
      {
        path: 'forgot-password',
        loadComponent: () => import('./features/auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent)
      },
      {
        path: 'reset-password',
        loadComponent: () => import('./features/auth/reset-password/reset-password.component').then(m => m.ResetPasswordComponent)
      },
      {
        path: 'oauth2/redirect',
        loadComponent: () => import('./features/auth/oauth2-redirect/oauth2-redirect.component').then(m => m.OAuth2RedirectComponent)
      }
    ]
  },
  {
    path: 'oauth2/redirect',
    loadComponent: () => import('./features/auth/oauth2-redirect/oauth2-redirect.component').then(m => m.OAuth2RedirectComponent)
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'admin',
    loadComponent: () => import('./features/admin/admin.component').then(m => m.AdminComponent),
    canActivate: [RoleGuard],
    data: { roles: [RoleName.ROLE_ADMIN] }
  },
  {
    path: 'instructor',
    canActivate: [RoleGuard],
    data: { roles: [RoleName.ROLE_INSTRUCTOR] },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/instructor/instructor-dashboard/instructor-dashboard.component').then(m => m.InstructorDashboardComponent)
      },
      {
        path: 'courses/create',
        loadComponent: () => import('./features/instructor/course-form/course-form.component').then(m => m.CourseFormComponent)
      },
      {
        path: 'courses/:id',
        loadComponent: () => import('./features/instructor/course-detail/course-detail.component').then(m => m.CourseDetailComponent)
      },
      {
        path: 'courses/:id/edit',
        loadComponent: () => import('./features/instructor/course-form/course-form.component').then(m => m.CourseFormComponent)
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: 'student',
    canActivate: [RoleGuard],
    data: { roles: [RoleName.ROLE_STUDENT] },
    children: [
      {
        path: 'courses',
        loadComponent: () => import('./features/student/course-catalog/course-catalog.component').then(m => m.CourseCatalogComponent)
      },
      {
        path: 'courses/:id',
        loadComponent: () => import('./features/student/course-detail/student-course-detail.component').then(m => m.StudentCourseDetailComponent)
      },
      {
        path: '',
        redirectTo: 'courses',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: 'forbidden',
    loadComponent: () => import('./features/forbidden/forbidden.component').then(m => m.ForbiddenComponent)
  },
  {
    path: '**',
    redirectTo: '/auth/login'
  }
];
