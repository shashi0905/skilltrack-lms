package com.skilltrack.common.enums;

/**
 * Instructor account verification status.
 * 
 * Flow:
 * 1. User registers as instructor → UNVERIFIED
 * 2. Admin reviews and approves → VERIFIED
 * 
 * Business Rules:
 * - UNVERIFIED instructors can create/publish courses
 * - Courses and profiles clearly labeled as "unverified" in UI
 * - Admin can verify instructors based on profile, content quality, etc.
 */
public enum InstructorVerificationStatus {
    /**
     * Instructor account not yet verified by admin.
     * Can create courses but labeled as unverified.
     */
    UNVERIFIED,
    
    /**
     * Instructor verified by admin.
     * Courses have higher trust/visibility.
     */
    VERIFIED,
    
    /**
     * Instructor rejected by admin.
     * May restrict course creation or visibility.
     */
    REJECTED
}
