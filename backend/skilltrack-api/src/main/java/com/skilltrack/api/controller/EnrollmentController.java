package com.skilltrack.api.controller;

import com.skilltrack.api.dto.response.EnrollmentResponse;
import com.skilltrack.api.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@Tag(name = "Enrollment Management", description = "APIs for course enrollment")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Autowired
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/{courseId}")
    @Operation(summary = "Enroll in a course")
    public ResponseEntity<EnrollmentResponse> enroll(
            @PathVariable String courseId,
            Authentication authentication) {
        EnrollmentResponse response = enrollmentService.enroll(courseId, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get my enrollments")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(Authentication authentication) {
        List<EnrollmentResponse> enrollments = enrollmentService.getMyEnrollments(authentication.getName());
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/{courseId}/status")
    @Operation(summary = "Check enrollment status for a course")
    public ResponseEntity<Map<String, Boolean>> checkEnrollment(
            @PathVariable String courseId,
            Authentication authentication) {
        boolean enrolled = enrollmentService.isEnrolled(courseId, authentication.getName());
        return ResponseEntity.ok(Map.of("enrolled", enrolled));
    }
}
