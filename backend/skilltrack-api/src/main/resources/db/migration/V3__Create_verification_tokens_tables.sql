-- V3__Create_verification_tokens_tables.sql
-- Email verification and password reset tokens

CREATE TABLE email_verification_tokens (
    id VARCHAR(36) PRIMARY KEY,
    token_value VARCHAR(36) NOT NULL UNIQUE,
    user_id VARCHAR(36) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for token lookup
CREATE INDEX idx_email_token_value ON email_verification_tokens(token_value);
CREATE INDEX idx_email_token_user_id ON email_verification_tokens(user_id);
CREATE INDEX idx_email_token_expires_at ON email_verification_tokens(expires_at);

CREATE TABLE password_reset_tokens (
    id VARCHAR(36) PRIMARY KEY,
    token_value VARCHAR(36) NOT NULL UNIQUE,
    user_id VARCHAR(36) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for token lookup
CREATE INDEX idx_reset_token_value ON password_reset_tokens(token_value);
CREATE INDEX idx_reset_token_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_reset_token_expires_at ON password_reset_tokens(expires_at);

-- Add comments
COMMENT ON TABLE email_verification_tokens IS 'Tokens for email address verification';
COMMENT ON TABLE password_reset_tokens IS 'Tokens for password reset flow';
