package com.gege.activitypartner.controller;

import com.gege.activitypartner.config.SecurityContextUtil;
import com.gege.activitypartner.dto.ActivityGalleryAccessResponse;
import com.gege.activitypartner.dto.ActivityPhotoResponse;
import com.gege.activitypartner.service.ActivityPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/activities/{activityId}/gallery")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ActivityPhotoController {

    private final ActivityPhotoService activityPhotoService;
    private final SecurityContextUtil securityContextUtil;

    /**
     * Check if current user has access to the activity gallery
     * GET /api/activities/{activityId}/gallery/access
     */
    @GetMapping("/access")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivityGalleryAccessResponse> checkGalleryAccess(@PathVariable Long activityId) {
        Long userId = securityContextUtil.getCurrentUserId();
        ActivityGalleryAccessResponse response = activityPhotoService.checkGalleryAccess(activityId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload photos to activity gallery
     * POST /api/activities/{activityId}/gallery/upload
     * Supports multiple file upload
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActivityPhotoResponse>> uploadPhotos(
            @PathVariable Long activityId,
            @RequestParam("files") List<MultipartFile> files) {
        Long userId = securityContextUtil.getCurrentUserId();
        List<ActivityPhotoResponse> uploadedPhotos = activityPhotoService.uploadActivityPhotos(activityId, userId, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedPhotos);
    }

    /**
     * Get all photos for an activity
     * GET /api/activities/{activityId}/gallery
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActivityPhotoResponse>> getActivityPhotos(@PathVariable Long activityId) {
        Long userId = securityContextUtil.getCurrentUserId();
        List<ActivityPhotoResponse> photos = activityPhotoService.getActivityPhotos(activityId, userId);
        return ResponseEntity.ok(photos);
    }

    /**
     * Delete a photo from the gallery
     * DELETE /api/activities/{activityId}/gallery/{photoId}
     */
    @DeleteMapping("/{photoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deletePhoto(
            @PathVariable Long activityId,
            @PathVariable Long photoId) {
        Long userId = securityContextUtil.getCurrentUserId();
        activityPhotoService.deleteActivityPhoto(activityId, photoId, userId);
        return ResponseEntity.ok("Photo deleted successfully");
    }

    /**
     * Get photo count for an activity
     * GET /api/activities/{activityId}/gallery/count
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getPhotoCount(@PathVariable Long activityId) {
        Long count = activityPhotoService.getPhotoCount(activityId);
        return ResponseEntity.ok(count);
    }
}
