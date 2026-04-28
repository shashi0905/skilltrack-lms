package com.skilltrack.api.service;

import com.skilltrack.common.entity.MediaAsset;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.exception.BusinessException;
import com.skilltrack.common.exception.ResourceNotFoundException;
import com.skilltrack.common.repository.MediaAssetRepository;
import com.skilltrack.common.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

@Service
@Transactional(readOnly = true)
public class VideoStreamingService {

    private final MediaAssetRepository mediaAssetRepository;
    private final UserRepository userRepository;

    @Value("${app.video.hls-directory:hls}")
    private String hlsDirectory;

    @Value("${app.video.signed-url-expiry-hours:24}")
    private int signedUrlExpiryHours;

    @Autowired
    public VideoStreamingService(MediaAssetRepository mediaAssetRepository, UserRepository userRepository) {
        this.mediaAssetRepository = mediaAssetRepository;
        this.userRepository = userRepository;
    }

    public Resource getPlaylist(String mediaAssetId, String userEmail) {
        MediaAsset mediaAsset = validateVideoAccess(mediaAssetId, userEmail);
        
        Path playlistPath = Paths.get(hlsDirectory, mediaAssetId, "playlist.m3u8");
        if (!Files.exists(playlistPath)) {
            throw new ResourceNotFoundException("Video playlist not found");
        }
        
        return new FileSystemResource(playlistPath);
    }

    public Resource getSegment(String mediaAssetId, String segmentNumber, String userEmail) {
        MediaAsset mediaAsset = validateVideoAccess(mediaAssetId, userEmail);
        
        Path segmentPath = Paths.get(hlsDirectory, mediaAssetId, "segment_" + segmentNumber + ".ts");
        if (!Files.exists(segmentPath)) {
            throw new ResourceNotFoundException("Video segment not found");
        }
        
        return new FileSystemResource(segmentPath);
    }

    public String generateSignedUrl(String mediaAssetId, String userEmail) {
        MediaAsset mediaAsset = validateVideoAccess(mediaAssetId, userEmail);
        
        // Create signed URL with expiration
        long expiryTime = LocalDateTime.now().plusHours(signedUrlExpiryHours).toEpochSecond(ZoneOffset.UTC);
        String payload = mediaAssetId + ":" + userEmail + ":" + expiryTime;
        String signature = Base64.getEncoder().encodeToString(payload.getBytes());
        
        return "/api/video/stream/" + mediaAssetId + "/playlist.m3u8?signature=" + signature + "&expires=" + expiryTime;
    }

    private MediaAsset validateVideoAccess(String mediaAssetId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        MediaAsset mediaAsset = mediaAssetRepository.findById(mediaAssetId)
                .orElseThrow(() -> new ResourceNotFoundException("Media asset not found"));

        if (!mediaAsset.isVideo()) {
            throw new BusinessException("Media asset is not a video");
        }

        if (!mediaAsset.isVideoProcessed()) {
            throw new BusinessException("Video is not ready for streaming");
        }

        // Check if user has access to the lesson
        if (mediaAsset.getLesson() != null) {
            // For now, allow access if user is enrolled in the course or is the instructor
            // This would be enhanced with proper enrollment checking
            boolean hasAccess = mediaAsset.getLesson().getModule().getCourse().getInstructor().equals(user);
            
            if (!hasAccess) {
                // TODO: Check if user is enrolled in the course
                throw new BusinessException("Access denied to video content");
            }
        }

        return mediaAsset;
    }

    public boolean validateSignedUrl(String signature, String mediaAssetId, String userEmail, long expiryTime) {
        // Check if URL has expired
        if (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) > expiryTime) {
            return false;
        }

        // Validate signature
        String expectedPayload = mediaAssetId + ":" + userEmail + ":" + expiryTime;
        String expectedSignature = Base64.getEncoder().encodeToString(expectedPayload.getBytes());
        
        return signature.equals(expectedSignature);
    }
}