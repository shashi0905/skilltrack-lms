package com.skilltrack.api.service;

import com.skilltrack.common.entity.MediaAsset;
import com.skilltrack.common.enums.ProcessingStatus;
import com.skilltrack.common.repository.MediaAssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class VideoProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(VideoProcessingService.class);

    private final MediaAssetRepository mediaAssetRepository;

    @Value("${app.upload.directory:uploads}")
    private String uploadDirectory;

    @Value("${app.video.hls-directory:hls}")
    private String hlsDirectory;

    @Value("${app.video.ffmpeg-path:ffmpeg}")
    private String ffmpegPath;

    @Autowired
    public VideoProcessingService(MediaAssetRepository mediaAssetRepository) {
        this.mediaAssetRepository = mediaAssetRepository;
    }

    @Async
    public CompletableFuture<Void> processVideoAsync(MediaAsset mediaAsset) {
        try {
            logger.info("Starting video processing for: {}", mediaAsset.getOriginalFilename());
            
            // Create HLS directory
            Path hlsPath = Paths.get(hlsDirectory, mediaAsset.getId());
            if (!Files.exists(hlsPath)) {
                Files.createDirectories(hlsPath);
            }

            // Process video with FFmpeg
            String inputPath = mediaAsset.getStoragePath();
            String outputPath = hlsPath.resolve("playlist.m3u8").toString();
            
            boolean success = processVideoWithFFmpeg(inputPath, outputPath, mediaAsset.getWatermarkText());
            
            if (success) {
                // Update media asset with HLS URL and duration
                mediaAsset.setHlsManifestUrl("/api/video/stream/" + mediaAsset.getId() + "/playlist.m3u8");
                mediaAsset.setVideoDurationSeconds(getVideoDuration(inputPath));
                
                // Update lesson processing status
                if (mediaAsset.getLesson() != null) {
                    mediaAsset.getLesson().setProcessingStatus(ProcessingStatus.READY);
                    mediaAsset.getLesson().setVideoDurationSeconds(mediaAsset.getVideoDurationSeconds());
                    mediaAsset.getLesson().setHlsManifestUrl(mediaAsset.getHlsManifestUrl());
                }
                
                mediaAssetRepository.save(mediaAsset);
                logger.info("Video processing completed successfully for: {}", mediaAsset.getOriginalFilename());
            } else {
                // Mark as failed
                if (mediaAsset.getLesson() != null) {
                    mediaAsset.getLesson().setProcessingStatus(ProcessingStatus.FAILED);
                }
                mediaAssetRepository.save(mediaAsset);
                logger.error("Video processing failed for: {}", mediaAsset.getOriginalFilename());
            }
            
        } catch (Exception e) {
            logger.error("Error processing video: {}", mediaAsset.getOriginalFilename(), e);
            // Mark as failed
            if (mediaAsset.getLesson() != null) {
                mediaAsset.getLesson().setProcessingStatus(ProcessingStatus.FAILED);
            }
            mediaAssetRepository.save(mediaAsset);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    private boolean processVideoWithFFmpeg(String inputPath, String outputPath, String watermarkText) {
        try {
            // FFmpeg command for HLS with watermark
            ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegPath,
                "-i", inputPath,
                "-vf", String.format("drawtext=text='%s':fontcolor=white:fontsize=24:x=10:y=10:alpha=0.7", watermarkText),
                "-c:v", "libx264",
                "-c:a", "aac",
                "-hls_time", "10",
                "-hls_playlist_type", "vod",
                "-hls_segment_filename", outputPath.replace("playlist.m3u8", "segment_%03d.ts"),
                "-f", "hls",
                outputPath
            );

            Process process = processBuilder.start();
            
            // Read output for debugging
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.debug("FFmpeg: {}", line);
                }
            }

            int exitCode = process.waitFor();
            return exitCode == 0;
            
        } catch (IOException | InterruptedException e) {
            logger.error("Error running FFmpeg", e);
            return false;
        }
    }

    private Integer getVideoDuration(String inputPath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegPath,
                "-i", inputPath,
                "-show_entries", "format=duration",
                "-v", "quiet",
                "-of", "csv=p=0"
            );

            Process process = processBuilder.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String durationStr = reader.readLine();
                if (durationStr != null && !durationStr.trim().isEmpty()) {
                    return (int) Double.parseDouble(durationStr.trim());
                }
            }
            
        } catch (Exception e) {
            logger.warn("Could not determine video duration for: {}", inputPath, e);
        }
        
        return null;
    }

    public void deleteHlsFiles(MediaAsset mediaAsset) {
        try {
            Path hlsPath = Paths.get(hlsDirectory, mediaAsset.getId());
            if (Files.exists(hlsPath)) {
                Files.walk(hlsPath)
                     .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                     .forEach(path -> {
                         try {
                             Files.delete(path);
                         } catch (IOException e) {
                             logger.warn("Could not delete HLS file: {}", path, e);
                         }
                     });
            }
        } catch (IOException e) {
            logger.error("Error deleting HLS files for media asset: {}", mediaAsset.getId(), e);
        }
    }
}