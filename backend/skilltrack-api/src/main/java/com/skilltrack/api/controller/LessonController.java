package com.skilltrack.api.controller;

import com.skilltrack.api.dto.request.LessonCreateRequest;
import com.skilltrack.api.dto.response.LessonResponse;
import com.skilltrack.api.dto.response.MessageResponse;
import com.skilltrack.api.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/modules/{moduleId}/lessons")
@Tag(name = "Lesson Management", description = "APIs for managing lessons")
public class LessonController {

    private final LessonService lessonService;

    @Autowired
    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping
    @Operation(summary = "Create a new lesson")
    public ResponseEntity<LessonResponse> createLesson(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @Valid @RequestBody LessonCreateRequest request,
            Authentication authentication) {
        LessonResponse response = lessonService.createLesson(courseId, moduleId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{lessonId}")
    @Operation(summary = "Update a lesson")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonCreateRequest request,
            Authentication authentication) {
        LessonResponse response = lessonService.updateLesson(courseId, moduleId, lessonId, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{lessonId}")
    @Operation(summary = "Delete a lesson")
    public ResponseEntity<MessageResponse> deleteLesson(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @PathVariable Long lessonId,
            Authentication authentication) {
        lessonService.deleteLesson(courseId, moduleId, lessonId, authentication.getName());
        return ResponseEntity.ok(new MessageResponse("Lesson deleted successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all lessons for a module")
    public ResponseEntity<List<LessonResponse>> getModuleLessons(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            Authentication authentication) {
        List<LessonResponse> lessons = lessonService.getModuleLessons(courseId, moduleId, authentication.getName());
        return ResponseEntity.ok(lessons);
    }

    @GetMapping("/{lessonId}")
    @Operation(summary = "Get lesson by ID")
    public ResponseEntity<LessonResponse> getLessonById(
            @PathVariable Long courseId,
            @PathVariable Long moduleId,
            @PathVariable Long lessonId,
            Authentication authentication) {
        LessonResponse response = lessonService.getLessonById(courseId, moduleId, lessonId, authentication.getName());
        return ResponseEntity.ok(response);
    }
}