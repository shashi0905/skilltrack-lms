package com.skilltrack.api.controller;

import com.skilltrack.api.dto.request.ModuleCreateRequest;
import com.skilltrack.api.dto.response.MessageResponse;
import com.skilltrack.api.dto.response.ModuleResponse;
import com.skilltrack.api.service.ModuleService;
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
@RequestMapping("/api/courses/{courseId}/modules")
@Tag(name = "Module Management", description = "APIs for managing course modules")
public class ModuleController {

    private final ModuleService moduleService;

    @Autowired
    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @PostMapping
    @Operation(summary = "Create a new module")
    public ResponseEntity<ModuleResponse> createModule(
            @PathVariable String courseId,
            @Valid @RequestBody ModuleCreateRequest request,
            Authentication authentication) {
        ModuleResponse response = moduleService.createModule(courseId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{moduleId}")
    @Operation(summary = "Update a module")
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable String courseId,
            @PathVariable String moduleId,
            @Valid @RequestBody ModuleCreateRequest request,
            Authentication authentication) {
        ModuleResponse response = moduleService.updateModule(courseId, moduleId, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{moduleId}")
    @Operation(summary = "Delete a module")
    public ResponseEntity<MessageResponse> deleteModule(
            @PathVariable String courseId,
            @PathVariable String moduleId,
            Authentication authentication) {
        moduleService.deleteModule(courseId, moduleId, authentication.getName());
        return ResponseEntity.ok(new MessageResponse("Module deleted successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all modules for a course")
    public ResponseEntity<List<ModuleResponse>> getCourseModules(
            @PathVariable String courseId,
            Authentication authentication) {
        List<ModuleResponse> modules = moduleService.getCourseModules(courseId, authentication.getName());
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/{moduleId}")
    @Operation(summary = "Get module by ID")
    public ResponseEntity<ModuleResponse> getModuleById(
            @PathVariable String courseId,
            @PathVariable String moduleId,
            Authentication authentication) {
        ModuleResponse response = moduleService.getModuleById(courseId, moduleId, authentication.getName());
        return ResponseEntity.ok(response);
    }
}