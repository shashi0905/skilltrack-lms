-- V2__Create_users_table.sql
-- Users table with email verification and instructor verification

CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100),
    full_name VARCHAR(255) NOT NULL,
    country VARCHAR(100),
    organization VARCHAR(255),
    github_id VARCHAR(50) UNIQUE,
    email_verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    instructor_verification_status VARCHAR(20),
    failed_login_attempts INT NOT NULL DEFAULT 0,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT chk_email_verification_status 
        CHECK (email_verification_status IN ('PENDING', 'VERIFIED')),
    CONSTRAINT chk_instructor_verification_status 
        CHECK (instructor_verification_status IN ('UNVERIFIED', 'VERIFIED', 'REJECTED'))
);

-- Create indexes for fast lookup
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_github_id ON users(github_id);
CREATE INDEX idx_email_verification_status ON users(email_verification_status);

-- User-Role many-to-many join table
CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create indexes on foreign keys
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- Add comments
COMMENT ON TABLE users IS 'All system users (students, instructors, admins)';
COMMENT ON TABLE user_roles IS 'Many-to-many relationship between users and roles';
