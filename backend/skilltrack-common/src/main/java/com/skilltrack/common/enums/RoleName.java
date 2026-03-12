package com.skilltrack.common.enums;

/**
 * User role types in the system.
 * 
 * Hierarchy (highest to lowest privilege):
 * - ADMIN: Full system access, user/instructor verification, platform governance
 * - INSTRUCTOR: Can create courses, view own course analytics (must be verified by admin)
 * - STUDENT: Can browse catalog, enroll in courses, complete assessments
 * 
 * Note: Instructors are also students (can enroll in courses)
 * Admins are created via backend processes, not self-registration
 */
public enum RoleName {
    /**
     * Administrator - full platform access
     * Created via backend only, not through public registration
     */
    ROLE_ADMIN,
    
    /**
     * Instructor - can create and manage courses
     * Starts as UNVERIFIED after registration, must be verified by admin
     */
    ROLE_INSTRUCTOR,
    
    /**
     * Student/Learner - can browse and enroll in courses
     * Default role for new registrations
     */
    ROLE_STUDENT
}
