-- V8__Add_content_type_and_processing_to_lessons.sql

-- Add new columns to lessons table
ALTER TABLE lessons 
ADD COLUMN content_type VARCHAR(20) NOT NULL DEFAULT 'TEXT' CHECK (content_type IN ('TEXT', 'VIDEO', 'PDF', 'IMAGE', 'QUIZ')),
ADD COLUMN processing_status VARCHAR(20) NOT NULL DEFAULT 'READY' CHECK (processing_status IN ('PENDING', 'UPLOADING', 'PROCESSING', 'READY', 'FAILED')),
ADD COLUMN video_duration_seconds INTEGER,
ADD COLUMN hls_manifest_url VARCHAR(500);

-- Create indexes for new columns
CREATE INDEX idx_lessons_content_type ON lessons(content_type);
CREATE INDEX idx_lessons_processing_status ON lessons(processing_status);

-- Update existing lessons to have TEXT content type and READY status
UPDATE lessons SET content_type = 'TEXT', processing_status = 'READY' WHERE content_type IS NULL OR processing_status IS NULL;