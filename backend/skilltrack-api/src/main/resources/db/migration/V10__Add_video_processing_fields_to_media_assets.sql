-- V10__Add_video_processing_fields_to_media_assets.sql

-- Add new columns to media_assets table for video processing
ALTER TABLE media_assets 
ADD COLUMN hls_manifest_url VARCHAR(500),
ADD COLUMN video_duration_seconds INTEGER,
ADD COLUMN watermark_text VARCHAR(100);

-- Create indexes for new columns
CREATE INDEX idx_media_assets_hls_manifest ON media_assets(hls_manifest_url);
CREATE INDEX idx_media_assets_watermark ON media_assets(watermark_text);