package com.skilltrack.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller.
 * 
 * Provides simple endpoint to verify API is running.
 * Useful for:
 * - Load balancers (health probe)
 * - Monitoring systems
 * - Development testing
 * - Deployment verification
 * 
 * This is a public endpoint (no authentication required).
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    /**
     * Basic health check endpoint.
     * 
     * Returns current server time and status.
     * 
     * @return Health status response
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "SkillTrack API");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Service is running");
        
        return ResponseEntity.ok(response);
    }

    /**
     * API information endpoint.
     * 
     * Returns basic API metadata.
     * 
     * @return API info response
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "SkillTrack LMS");
        response.put("version", "1.0.0-SNAPSHOT");
        response.put("description", "Learning Management System API");
        response.put("phase", "Phase 1 - User Registration & Authentication");
        
        return ResponseEntity.ok(response);
    }
}
