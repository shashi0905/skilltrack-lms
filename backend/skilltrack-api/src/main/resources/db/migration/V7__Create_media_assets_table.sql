-- V7__Create_media_assets_table.sql
CREATE TABLE media_assets (
    id VARCHAR(36) PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    media_type VARCHAR(20) NOT NULL CHECK (media_type IN ('IMAGE', 'VIDEO', 'AUDIO', 'DOCUMENT', 'PRESENTATION', 'SPREADSHEET', 'ARCHIVE', 'OTHER')),
    description VARCHAR(500),
    uploaded_by VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id VARCHAR(36) REFERENCES courses(id) ON DELETE CASCADE,
    module_id VARCHAR(36) REFERENCES course_modules(id) ON DELETE CASCADE,
    lesson_id VARCHAR(36) REFERENCES lessons(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    soft_deleted BOOLEAN DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_media_assets_uploaded_by ON media_assets(uploaded_by);
CREATE INDEX idx_media_assets_course_id ON media_assets(course_id);
CREATE INDEX idx_media_assets_module_id ON media_assets(module_id);
CREATE INDEX idx_media_assets_lesson_id ON media_assets(lesson_id);
CREATE INDEX idx_media_assets_media_type ON media_assets(media_type);
CREATE INDEX idx_media_assets_created_at ON media_assets(created_at);

CREATE TRIGGER update_media_assets_updated_at BEFORE UPDATE ON media_assets FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();