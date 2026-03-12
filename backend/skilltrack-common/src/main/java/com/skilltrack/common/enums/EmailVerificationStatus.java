package com.skilltrack.common.enums;

/**
 * Email verification status for user accounts.
 * 
 * Flow:
 * 1. User registers → PENDING
 * 2. User clicks verification link → VERIFIED
 * 
 * Behavior:
 * - PENDING: Can login with limited access (browse only, cannot enroll)
 * - VERIFIED: Full access according to role
 */
public enum EmailVerificationStatus {
    /**
     * Email verification pending.
     * User can login but has limited access.
     */
    PENDING,
    
    /**
     * Email verified successfully.
     * User has full access according to their role.
     */
    VERIFIED
}
