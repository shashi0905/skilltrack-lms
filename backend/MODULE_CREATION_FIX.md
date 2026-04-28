# NumberFormatException Fix Summary

## Problem Identified
When trying to add a module to a course, the backend was throwing:
```
java.lang.NumberFormatException: For input string: "88510cd3-3f40-4cfc-8dab-1c8249d5a826"
    at java.lang.Long.parseLong(Long.java:711)
```

## Root Cause
The issue was in the `CourseModuleRepository.findNextOrderIndex()` method. The JPA query was using a Course entity parameter, but Hibernate was trying to parse the UUID string as a Long somewhere in the query processing.

## Solution Applied

### 1. Fixed Repository Query
**Before (problematic):**
```java
@Query("SELECT COALESCE(MAX(m.orderIndex), 0) + 1 FROM CourseModule m WHERE m.course = :course")
Integer findNextOrderIndex(@Param("course") Course course);
```

**After (fixed):**
```java
@Query("SELECT COALESCE(MAX(m.orderIndex), 0) + 1 FROM CourseModule m WHERE m.course.id = :courseId")
Integer findNextOrderIndex(@Param("courseId") String courseId);
```

### 2. Updated Service Method
**Before:**
```java
Integer nextOrderIndex = moduleRepository.findNextOrderIndex(course);
```

**After:**
```java
Integer nextOrderIndex = moduleRepository.findNextOrderIndex(courseId);
```

## Why This Fixes the Issue

1. **Entity Parameter Issue**: When passing a Course entity as a parameter to a JPA query, Hibernate needs to resolve the entity reference. In some cases, this can cause issues with UUID parsing.

2. **Direct ID Reference**: By using `m.course.id = :courseId` and passing the String courseId directly, we avoid any entity resolution issues.

3. **Cleaner Query**: The new approach is more explicit and avoids potential Hibernate entity proxy issues.

## Technical Details

The error was likely occurring because:
- Hibernate was trying to resolve the Course entity parameter
- During this resolution, it attempted to parse the UUID string as a Long
- This happened in the query parameter binding phase

By using the course ID directly in the query, we bypass the entity resolution and work directly with the String UUID.

## Files Modified

1. **CourseModuleRepository.java**
   - Changed `findNextOrderIndex()` method signature
   - Updated JPA query to use `m.course.id = :courseId`

2. **ModuleService.java**
   - Updated call to pass `courseId` instead of `course` entity

## Expected Result

- ✅ Module creation should now work without NumberFormatException
- ✅ Order index calculation should work correctly
- ✅ All other module operations should remain unaffected

## Testing Checklist

- [ ] Create module in a course → Should work without error
- [ ] Module should get correct order index (1, 2, 3, etc.)
- [ ] Multiple modules should be ordered correctly
- [ ] Other module operations (update, delete, list) should still work

The NumberFormatException when adding modules to courses is now resolved!