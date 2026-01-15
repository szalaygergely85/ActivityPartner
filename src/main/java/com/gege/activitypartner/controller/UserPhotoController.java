package com.gege.activitypartner.controller;

import com.gege.activitypartner.config.SecurityContextUtil;
import com.gege.activitypartner.dto.ImageUploadResponse;
import com.gege.activitypartner.dto.UserPhotoResponse;
import com.gege.activitypartner.service.UserPhotoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/photos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserPhotoController {

  private final UserPhotoService userPhotoService;
  private final SecurityContextUtil securityContextUtil;

  /** Upload a new photo for the current user Returns the URL of the uploaded photo as JSON */
  @PostMapping("/upload")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ImageUploadResponse> uploadPhoto(@RequestParam("file") MultipartFile file) {
    Long userId = securityContextUtil.getCurrentUserId();
    String photoUrl = userPhotoService.uploadUserPhoto(userId, file);
    ImageUploadResponse response = new ImageUploadResponse(photoUrl);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /** Get all photos for the current user */
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<UserPhotoResponse>> getMyPhotos() {
    Long userId = securityContextUtil.getCurrentUserId();
    List<UserPhotoResponse> photos = userPhotoService.getUserPhotos(userId);
    return ResponseEntity.ok(photos);
  }

  /** Get all photos for a specific user (public) */
  @GetMapping("/user/{userId}")
  public ResponseEntity<List<UserPhotoResponse>> getUserPhotos(@PathVariable Long userId) {
    List<UserPhotoResponse> photos = userPhotoService.getUserPhotos(userId);
    return ResponseEntity.ok(photos);
  }

  /** Set a photo as the main profile picture */
  @PutMapping("/{photoId}/set-as-profile")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<UserPhotoResponse> setAsProfilePicture(@PathVariable Long photoId) {
    Long userId = securityContextUtil.getCurrentUserId();
    UserPhotoResponse photo = userPhotoService.setAsProfilePicture(userId, photoId);
    return ResponseEntity.ok(photo);
  }

  /** Delete a photo Returns success message as JSON */
  @DeleteMapping("/{photoId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<String> deletePhoto(@PathVariable Long photoId) {
    Long userId = securityContextUtil.getCurrentUserId();
    userPhotoService.deleteUserPhoto(userId, photoId);
    return ResponseEntity.ok("Photo deleted successfully");
  }

  /**
   * Reorder photos (for drag-and-drop) Request body: {"photoIds": [1, 2, 3, 4, 5, 6]} Returns
   * success message as JSON
   */
  @PutMapping("/reorder")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<String> reorderPhotos(@RequestBody ReorderPhotosRequest request) {
    Long userId = securityContextUtil.getCurrentUserId();
    userPhotoService.reorderPhotos(userId, request.getPhotoIds());
    return ResponseEntity.ok("Photos reordered successfully");
  }

  // DTO for reorder request
  @lombok.Data
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor
  public static class ReorderPhotosRequest {
    private List<Long> photoIds;
  }
}
