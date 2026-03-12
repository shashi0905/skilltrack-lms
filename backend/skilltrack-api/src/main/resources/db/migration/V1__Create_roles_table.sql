-- V1__Create_roles_table.sql
-- Initial schema for roles

CREATE TABLE roles (
    id VARCHAR(36) PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create index for role lookup
CREATE INDEX idx_role_name ON roles(role_name);

-- Add comment
COMMENT ON TABLE roles IS 'System roles for authorization';
