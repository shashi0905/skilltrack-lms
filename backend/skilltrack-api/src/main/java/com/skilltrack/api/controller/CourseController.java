package com.skilltrack.api.controller;

import com.skilltrack.api.dto.request.CourseCreateRequest;
import com.skilltrack.api.dto.request.CourseUpdateRequest;
import com.skilltrack.api.dto.response.CourseResponse;
import com.skilltrack.api.dto.response.MessageResponse;
import com.skilltrack.api.service.CourseService;
import com.skilltrack.common.enums.CourseStatus;
import com.skilltrack.common.enums.DifficultyLevel;
import com.skilltrack.common.repository.CourseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Course Management", description = "APIs for managing courses")
public class CourseController {

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    @Operation(summary = "Create a new course")
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CourseCreateRequest request,
            Authentication authentication) {
        CourseResponse response = courseService.createCourse(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{courseId}")
    @Operation(summary = "Update a course")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable String courseId,
            @Valid @RequestBody CourseUpdateRequest request,
            Authentication authentication) {
        CourseResponse response = courseService.updateCourse(courseId, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{courseId}/publish")
    @Operation(summary = "Publish a course")
    public ResponseEntity<CourseResponse> publishCourse(
            @PathVariable String courseId,
            Authentication authentication) {
        CourseResponse response = courseService.publishCourse(courseId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{courseId}")
    @Operation(summary = "Delete a draft course")
    public ResponseEntity<MessageResponse> deleteCourse(
            @PathVariable String courseId,
            Authentication authentication) {
        courseService.deleteCourse(courseId, authentication.getName());
        return ResponseEntity.ok(new MessageResponse("Course deleted successfully"));
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "Get course by ID")
    public ResponseEntity<CourseResponse> getCourseById(
            @PathVariable String courseId,
            Authentication authentication) {
        CourseResponse response = courseService.getCourseById(courseId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-courses")
    @Operation(summary = "Get instructor's courses")
    public ResponseEntity<List<CourseResponse>> getInstructorCourses(Authentication authentication) {
        List<CourseResponse> courses = courseService.getInstructorCourses(authentication.getName());
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/my-courses/paged")
    @Operation(summary = "Get instructor's courses with pagination")
    public ResponseEntity<Page<CourseResponse>> getInstructorCoursesPaged(
            Authentication authentication, Pageable pageable) {
        Page<CourseResponse> courses = courseService.getInstructorCourses(authentication.getName(), pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/my-courses/status/{status}")
    @Operation(summary = "Get instructor's courses by status")
    public ResponseEntity<List<CourseResponse>> getInstructorCoursesByStatus(
            @PathVariable CourseStatus status,
            Authentication authentication) {
        List<CourseResponse> courses = courseService.getInstructorCoursesByStatus(authentication.getName(), status);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/my-courses/draft-changes")
    @Operation(summary = "Get courses with unpublished draft changes")
    public ResponseEntity<List<CourseResponse>> getCoursesWithDraftChanges(Authentication authentication) {
        List<CourseResponse> courses = courseService.getCoursesWithDraftChanges(authentication.getName());
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get course statistics for instructor")
    public ResponseEntity<CourseRepository.CourseStatistics> getCourseStatistics(Authentication authentication) {
        CourseRepository.CourseStatistics stats = courseService.getCourseStatistics(authentication.getName());
        return ResponseEntity.ok(stats);
    }

    // Public endpoints for course catalog
    @GetMapping("/public")
    @Operation(summary = "Get published courses for public catalog")
    public ResponseEntity<Page<CourseResponse>> getPublishedCourses(Pageable pageable) {
        Page<CourseResponse> courses = courseService.getPublishedCourses(pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/public/search")
    @Operation(summary = "Search published courses")
    public ResponseEntity<Page<CourseResponse>> searchPublishedCourses(
            @RequestParam String q, Pageable pageable) {
        Page<CourseResponse> courses = courseService.searchPublishedCourses(q, pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/public/difficulty/{difficulty}")
    @Operation(summary = "Get published courses by difficulty")
    public ResponseEntity<Page<CourseResponse>> getPublishedCoursesByDifficulty(
            @PathVariable DifficultyLevel difficulty, Pageable pageable) {
        Page<CourseResponse> courses = courseService.getPublishedCoursesByDifficulty(difficulty, pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/public/tag/{tag}")
    @Operation(summary = "Get published courses by tag")
    public ResponseEntity<Page<CourseResponse>> getPublishedCoursesByTag(
            @PathVariable String tag, Pageable pageable) {
        Page<CourseResponse> courses = courseService.getPublishedCoursesByTag(tag, pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/public/{courseId}")
    @Operation(summary = "Get published course by ID")
    public ResponseEntity<CourseResponse> getPublishedCourseById(@PathVariable String courseId) {
        CourseResponse response = courseService.getPublishedCourseById(courseId);
        return ResponseEntity.ok(response);
    }
}