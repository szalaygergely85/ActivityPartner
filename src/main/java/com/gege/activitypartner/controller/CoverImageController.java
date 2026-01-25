package com.gege.activitypartner.controller;

import com.gege.activitypartner.dto.CoverImageDTO;
import com.gege.activitypartner.service.CoverImageService;
import com.gege.activitypartner.service.CoverImageStorageService;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/covers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CoverImageController {

  private final CoverImageService coverImageService;
  private final CoverImageStorageService coverImageStorageService;

  // Get all available cover images
  @GetMapping
  public ResponseEntity<List<CoverImageDTO>> getAllCoverImages() {
    List<CoverImageDTO> images = coverImageService.getAllAvailableCoverImages();
    return ResponseEntity.ok(images);
  }

  // Serve cover image file
  @GetMapping("/{fileName:.+}")
  public ResponseEntity<Resource> getCoverImage(@PathVariable String fileName) {
    try {
      Path filePath =
          coverImageStorageService.getFileStorageLocation().resolve(fileName).normalize();
      Resource resource = new UrlResource(filePath.toUri());

      if (resource.exists() && resource.isReadable()) {
        String contentType = determineContentType(fileName);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
            .body(resource);
      }
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }

  // Upload new cover image (admin only)
  @PostMapping("/upload")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CoverImageDTO> uploadCoverImage(
      @RequestParam("file") MultipartFile file, @RequestParam("displayName") String displayName) {
    CoverImageDTO result = coverImageService.uploadCoverImage(file, displayName);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  // Deactivate cover image (admin only)
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deactivateCoverImage(@PathVariable Long id) {
    coverImageService.deactivateCoverImage(id);
    return ResponseEntity.noContent().build();
  }

  private String determineContentType(String fileName) {
    String lowerName = fileName.toLowerCase();
    if (lowerName.endsWith(".png")) {
      return "image/png";
    } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
      return "image/jpeg";
    } else if (lowerName.endsWith(".gif")) {
      return "image/gif";
    } else if (lowerName.endsWith(".webp")) {
      return "image/webp";
    }
    return "application/octet-stream";
  }
}
