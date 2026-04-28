package com.skilltrack.api.controller;

import com.skilltrack.api.service.VideoStreamingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video/stream")
@Tag(name = "Video Streaming", description = "APIs for streaming protected video content")
public class VideoStreamingController {

    private final VideoStreamingService videoStreamingService;

    @Autowired
    public VideoStreamingController(VideoStreamingService videoStreamingService) {
        this.videoStreamingService = videoStreamingService;
    }

    @GetMapping("/{mediaAssetId}/playlist.m3u8")
    @Operation(summary = "Get HLS playlist for video")
    public ResponseEntity<Resource> getPlaylist(
            @PathVariable String mediaAssetId,
            Authentication authentication) {
        
        Resource resource = videoStreamingService.getPlaylist(mediaAssetId, authentication.getName());
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(resource);
    }

    @GetMapping("/{mediaAssetId}/segment_{segmentNumber}.ts")
    @Operation(summary = "Get HLS video segment")
    public ResponseEntity<Resource> getSegment(
            @PathVariable String mediaAssetId,
            @PathVariable String segmentNumber,
            Authentication authentication) {
        
        Resource resource = videoStreamingService.getSegment(mediaAssetId, segmentNumber, authentication.getName());
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp2t"))
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // Cache segments for 1 hour
                .body(resource);
    }

    @GetMapping("/{mediaAssetId}/signed-url")
    @Operation(summary = "Get signed URL for video streaming")
    public ResponseEntity<String> getSignedUrl(
            @PathVariable String mediaAssetId,
            Authentication authentication) {
        
        String signedUrl = videoStreamingService.generateSignedUrl(mediaAssetId, authentication.getName());
        return ResponseEntity.ok(signedUrl);
    }
}