package com.skilltrack.api.controller;

import com.skilltrack.common.entity.MediaAsset;
import com.skilltrack.common.exception.ResourceNotFoundException;
import com.skilltrack.common.repository.MediaAssetRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/media")
@Tag(name = "Media", description = "APIs for serving media files")
public class MediaController {

    private final MediaAssetRepository mediaAssetRepository;

    @Autowired
    public MediaController(MediaAssetRepository mediaAssetRepository) {
        this.mediaAssetRepository = mediaAssetRepository;
    }

    @GetMapping("/{mediaAssetId}")
    @Operation(summary = "Get media file by ID")
    public ResponseEntity<Resource> getMedia(@PathVariable String mediaAssetId) {
        MediaAsset asset = mediaAssetRepository.findById(mediaAssetId)
                .orElseThrow(() -> new ResourceNotFoundException("Media asset not found"));

        Path filePath = Paths.get(asset.getStoragePath());
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            throw new ResourceNotFoundException("File not found on disk");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(asset.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + asset.getOriginalFilename() + "\"")
                .body(resource);
    }
}
