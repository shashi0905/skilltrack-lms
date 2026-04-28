# Phase 2 Frontend - Quick Start Guide

## For Developers

### Running the Application

```bash
cd frontend
npm install
ng serve
```

Navigate to `http://localhost:4200`

### Accessing Instructor Features

1. Login as a user with `ROLE_INSTRUCTOR`
2. Navigate to `/instructor/dashboard`
3. Create your first course

### Component Usage

#### Creating a Course
1. Click "Create New Course" button
2. Fill in course details (title, description, difficulty, duration, tags)
3. Submit - course saved as DRAFT

#### Adding Structure
1. From dashboard, click "View" on a course
2. Click "Add Module" - fill module details
3. Within a module, click "Add Lesson" - fill lesson details
4. Repeat to build course structure

#### Publishing
1. Ensure course has at least 1 module with 1 lesson
2. Click "Publish" button
3. Course becomes visible to learners

#### Editing Published Courses
1. Edit course/module/lesson as needed
2. Changes saved as draft
3. "Has unpublished changes" indicator appears
4. Click "Publish Updates" to make changes live

### API Endpoints Expected

```
GET    /api/courses/instructor/my-courses
POST   /api/courses
GET    /api/courses/{id}
PUT    /api/courses/{id}
DELETE /api/courses/{id}
POST   /api/courses/{id}/publish

POST   /api/courses/{courseId}/modules
PUT    /api/courses/{courseId}/modules/{moduleId}
DELETE /api/courses/{courseId}/modules/{moduleId}

POST   /api/courses/{courseId}/modules/{moduleId}/lessons
PUT    /api/courses/{courseId}/modules/{moduleId}/lessons/{lessonId}
DELETE /api/courses/{courseId}/modules/{moduleId}/lessons/{lessonId}

POST   /api/courses/{courseId}/media
DELETE /api/courses/{courseId}/media/{mediaId}
```

### Environment Configuration

Update `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

### Troubleshooting

**Issue**: Routes not working
- Check `RoleGuard` is properly configured
- Verify user has `ROLE_INSTRUCTOR` role
- Check browser console for errors

**Issue**: API calls failing
- Verify backend is running
- Check `environment.ts` has correct API URL
- Verify JWT token is being sent (check Network tab)

**Issue**: File upload not working
- Check file size < 100MB
- Verify file type is allowed
- Check backend media upload configuration

### Testing Checklist

```
□ Login as instructor
□ Access instructor dashboard
□ Create new course
□ Edit course details
□ Add module to course
□ Add lesson to module
□ Upload media file
□ Publish course (should fail if no modules/lessons)
□ Add required structure and publish successfully
□ Edit published course
□ Verify "has draft changes" indicator
□ Publish updates
□ Delete draft course
□ Verify non-instructors cannot access routes
```

### Code Locations

```
Models:     src/app/core/models/course.model.ts
Service:    src/app/core/services/course.service.ts
Components: src/app/features/instructor/
Routes:     src/app/app.routes.ts
```

### Key Features

- **Signals**: Reactive state management
- **Reactive Forms**: Form validation and handling
- **Lazy Loading**: Components loaded on demand
- **Role Guards**: Route protection
- **Error Handling**: User-friendly error messages
- **Loading States**: Visual feedback during operations

### Customization

**Styling**: Modify `.scss` files in each component directory

**Validation**: Update validators in form components

**API URLs**: Modify `course.service.ts` if endpoints differ

**File Upload Limits**: Update validation in `media-upload.component.ts`

### Support

For issues or questions:
1. Check browser console for errors
2. Review network requests in DevTools
3. Verify backend logs
4. Consult `PHASE2_FRONTEND_IMPLEMENTATION.md` for details
