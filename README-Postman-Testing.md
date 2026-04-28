# SkillTrack Phase 2 API Testing with Postman

## Files Included

1. **SkillTrack-Phase2-API.postman_collection.json** - Complete API collection
2. **SkillTrack-Phase2-Environment.postman_environment.json** - Environment variables
3. **README-Postman-Testing.md** - This instruction file

## Import Instructions

### Step 1: Import Collection
1. Open Postman
2. Click **Import** button
3. Select **SkillTrack-Phase2-API.postman_collection.json**
4. Click **Import**

### Step 2: Import Environment
1. Click **Import** button again
2. Select **SkillTrack-Phase2-Environment.postman_environment.json**
3. Click **Import**
4. Select the **SkillTrack Phase 2 Environment** from the environment dropdown

### Step 3: Update Environment Variables
1. Click the **Environment** tab
2. Update the following variables with actual values:
   - `baseUrl`: Your backend server URL (default: http://localhost:8080)
   - `instructorToken`: JWT token for instructor user
   - `studentToken`: JWT token for student user  
   - `adminToken`: JWT token for admin user

## Getting JWT Tokens

Before testing Phase 2 APIs, you need to obtain JWT tokens:

1. **Register/Login as Instructor:**
   ```
   POST {{baseUrl}}/api/auth/login
   {
     "email": "instructor@example.com",
     "password": "password123"
   }
   ```

2. **Copy the JWT token from response and update `instructorToken` variable**

## Collection Structure

### 1. Course Management (11 endpoints)
- Create Course
- Update Course  
- Get Course by ID
- Get My Courses
- Get My Courses (Paginated)
- Get Draft/Published Courses
- Get Courses with Draft Changes
- Publish Course
- Delete Course
- Get Course Statistics

### 2. Module Management (5 endpoints)
- Create Module
- Update Module
- Get Course Modules
- Get Module by ID
- Delete Module

### 3. Lesson Management (5 endpoints)
- Create Lesson
- Update Lesson
- Get Module Lessons
- Get Lesson by ID
- Delete Lesson

### 4. Public Course Catalog (7 endpoints)
- Get Published Courses
- Search Published Courses
- Get Courses by Difficulty
- Get Courses by Tag
- Get Published Course by ID

### 5. Test Scenarios (4 endpoints)
- Try Publish Empty Course (Should Fail)
- Create Course with Invalid Data
- Access Course Without Authentication
- Delete Published Course (Should Fail)

## Recommended Testing Flow

### Complete Course Creation Workflow:
1. **Create Course** → Should return DRAFT status
2. **Try to Publish** → Should fail (no modules/lessons)
3. **Create Module** → Should succeed
4. **Try to Publish** → Should fail (no lessons)
5. **Create Lesson** → Should succeed
6. **Publish Course** → Should succeed
7. **Verify in Public Catalog** → Should be visible

### Draft Changes Workflow:
1. **Create and Publish Course** (with module and lesson)
2. **Update Course Details** → Should mark as having draft changes
3. **Check Draft Changes Endpoint** → Should show the course
4. **Publish Updates** → Should clear draft changes flag

## Environment Variables Used

- `{{baseUrl}}` - Backend server URL
- `{{instructorToken}}` - JWT token for instructor authentication
- `{{courseId}}` - Course ID for testing (update after creating course)
- `{{moduleId}}` - Module ID for testing (update after creating module)
- `{{lessonId}}` - Lesson ID for testing (update after creating lesson)

## Expected Response Codes

- **200 OK** - Successful GET, PUT operations
- **201 Created** - Successful POST operations
- **400 Bad Request** - Validation errors, business rule violations
- **401 Unauthorized** - Missing or invalid JWT token
- **403 Forbidden** - Insufficient permissions
- **404 Not Found** - Resource not found or access denied

## Tips for Testing

1. **Update IDs**: After creating resources, update the environment variables with actual IDs
2. **Check Authentication**: Ensure JWT tokens are valid and not expired
3. **Follow Sequence**: Some endpoints depend on others (e.g., create course before creating modules)
4. **Test Validation**: Try invalid data to test validation rules
5. **Test Access Control**: Try accessing resources with different user roles

## Troubleshooting

- **401 Unauthorized**: Check if JWT token is valid and properly set
- **403 Forbidden**: Ensure user has correct role (INSTRUCTOR for course management)
- **404 Not Found**: Verify resource IDs and ownership
- **400 Bad Request**: Check request body format and required fields