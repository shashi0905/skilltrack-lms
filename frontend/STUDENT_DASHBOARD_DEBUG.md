## Student Dashboard Troubleshooting Guide

### Issue: Students still see "coming soon" message instead of course catalog

### Quick Fixes to Try:

#### 1. Clear Browser Cache
- **Chrome/Edge**: Ctrl+Shift+R (or Cmd+Shift+R on Mac)
- **Firefox**: Ctrl+F5 (or Cmd+F5 on Mac)
- Or open Developer Tools → Network tab → check "Disable cache"

#### 2. Hard Refresh the Application
```bash
# Stop the frontend server (Ctrl+C)
cd frontend
npm start
```

#### 3. Check User Role in Browser Console
1. Open browser Developer Tools (F12)
2. Go to Console tab
3. Type: `localStorage.getItem('currentUser')`
4. Verify the user has `ROLE_STUDENT` in the roles array

#### 4. Manual Navigation Test
In the browser address bar, try navigating directly to:
```
http://localhost:4200/student/courses
```

### Expected Behavior:
- **Students**: Should auto-redirect to `/student/courses` (course catalog)
- **Instructors**: Should auto-redirect to `/instructor/dashboard`
- **Admins**: Should stay on dashboard with admin panel option

### Debug Steps:

#### Step 1: Verify User Roles
```javascript
// In browser console:
const user = JSON.parse(localStorage.getItem('currentUser') || '{}');
console.log('User roles:', user.roles);
console.log('Is student:', user.roles?.includes('ROLE_STUDENT'));
```

#### Step 2: Check Route Protection
If you get "Forbidden" error when accessing `/student/courses`, the issue is with role-based routing.

#### Step 3: Verify Component Loading
Check browser Network tab for any failed component loads.

### If Still Not Working:

#### Option 1: Temporary Manual Navigation
Update the dashboard template to add a direct link:
```html
@if (isStudent()) {
  <div class="action-section">
    <h3>Student Dashboard</h3>
    <p>Browse courses, track your learning progress, and manage enrollments.</p>
    <button (click)="goToCourseCatalog()" class="btn btn-primary">
      Browse Course Catalog
    </button>
    <!-- Temporary direct link for testing -->
    <a routerLink="/student/courses" class="btn btn-secondary" style="margin-left: 10px;">
      Direct Link to Catalog
    </a>
  </div>
}
```

#### Option 2: Check Authentication State
```javascript
// In browser console:
console.log('Auth token:', localStorage.getItem('accessToken'));
console.log('Current user:', localStorage.getItem('currentUser'));
```

### Most Likely Causes:
1. **Browser cache** - Old version of dashboard component
2. **Role not properly set** - User doesn't have ROLE_STUDENT
3. **Route guard issue** - RoleGuard blocking access
4. **Component not compiled** - Build issue with new components

### Test User Creation:
If you need to create a test student user:
1. Register a new user
2. Select "Student" role during registration
3. Verify email if required
4. Login and check dashboard