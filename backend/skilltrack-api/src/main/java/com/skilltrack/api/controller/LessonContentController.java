package com.skilltrack.api.controller;

import com.skilltrack.api.dto.response.MediaAssetResponse;
import com.skilltrack.api.dto.response.MessageResponse;
import com.skilltrack.api.service.LessonContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/courses/{courseId}/modules/{moduleId}/lessons/{lessonId}/content")
@Tag(name = "Lesson Content Management", description = "APIs for managing lesson content uploads")
public class LessonContentController {

    private final LessonContentService lessonContentService;

    @Autowired
    public LessonContentController(LessonContentService lessonContentService) {
        this.lessonContentService = lessonContentService;
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload content file to lesson")
    public ResponseEntity<MediaAssetResponse> uploadContent(
            @PathVariable String courseId,
            @PathVariable String moduleId,
            @PathVariable String lessonId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        
        MediaAssetResponse response = lessonContentService.uploadLessonContent(
                courseId, moduleId, lessonId, file, description, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/media/{mediaAssetId}")
    @Operation(summary = "Delete media asset from lesson")
    public ResponseEntity<MessageResponse> deleteMediaAsset(
            @PathVariable String courseId,
            @PathVariable String moduleId,
            @PathVariable String lessonId,
            @PathVariable String mediaAssetId,
            Authentication authentication) {
        
        lessonContentService.deleteMediaAsset(courseId, moduleId, lessonId, mediaAssetId, authentication.getName());
        return ResponseEntity.ok(new MessageResponse("Media asset deleted successfully"));
    }

    @GetMapping("/processing-status")
    @Operation(summary = "Get lesson content processing status")
    public ResponseEntity<MessageResponse> getProcessingStatus(
            @PathVariable String courseId,
            @PathVariable String moduleId,
            @PathVariable String lessonId,
            Authentication authentication) {
        
        String status = lessonContentService.getProcessingStatus(courseId, moduleId, lessonId, authentication.getName());
        return ResponseEntity.ok(new MessageResponse(status));
    }
}