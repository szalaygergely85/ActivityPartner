package com.gege.activitypartner.controller;

import com.gege.activitypartner.dto.CategoryRequest;
import com.gege.activitypartner.dto.CategoryResponse;
import com.gege.activitypartner.dto.CategoryUpdateRequest;
import com.gege.activitypartner.dto.CoverImageDTO;
import com.gege.activitypartner.entity.AccountDeletionRequest;
import com.gege.activitypartner.entity.AppLog;
import com.gege.activitypartner.entity.CrashLog;
import com.gege.activitypartner.entity.DownloadLog;
import com.gege.activitypartner.service.AdminService;
import com.gege.activitypartner.service.CategoryService;
import com.gege.activitypartner.service.CoverImageService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;
  private final CategoryService categoryService;
  private final CoverImageService coverImageService;

  // Admin login
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
    String username = request.get("username");
    String password = request.get("password");

    if (username == null || password == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "Username and password required"));
    }

    try {
      String token = adminService.login(username, password);
      return ResponseEntity.ok(Map.of("token", token));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Invalid credentials"));
    }
  }

  // Get pending deletion requests
  @GetMapping("/deletion-requests")
  public ResponseEntity<?> getDeletionRequests(HttpServletRequest request) {
    Long adminId = validateAdmin(request);
    if (adminId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    List<AccountDeletionRequest> requests = adminService.getPendingDeletionRequests();
    return ResponseEntity.ok(requests);
  }

  // Mark deletion request as processed
  @PostMapping("/deletion-requests/{id}/process")
  public ResponseEntity<?> processDeletionRequest(
      @PathVariable Long id, HttpServletRequest request) {
    Long adminId = validateAdmin(request);
    if (adminId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    adminService.markDeletionRequestProcessed(id);
    return ResponseEntity.ok(Map.of("success", true));
  }

  // Get crash logs
  @GetMapping("/crash-logs")
  public ResponseEntity<?> getCrashLogs(HttpServletRequest request) {
    Long adminId = validateAdmin(request);
    if (adminId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    List<CrashLog> logs = adminService.getRecentCrashLogs();
    return ResponseEntity.ok(logs);
  }

  // Get download logs
  @GetMapping("/download-logs")
  public ResponseEntity<?> getDownloadLogs(HttpServletRequest request) {
    Long adminId = validateAdmin(request);
    if (adminId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    List<DownloadLog> logs = adminService.getRecentDownloadLogs();
    return ResponseEntity.ok(logs);
  }

  // Get download stats
  @GetMapping("/download-stats")
  public ResponseEntity<?> getDownloadStats(HttpServletRequest request) {
    Long adminId = validateAdmin(request);
    if (adminId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    AdminService.DownloadStats stats = adminService.getDownloadStats();
    return ResponseEntity.ok(
        Map.of(
            "total", stats.total(),
            "android", stats.android(),
            "ios", stats.ios()));
  }

  // Get app logs
  @GetMapping("/app-logs")
  public ResponseEntity<?> getAppLogs(HttpServletRequest request) {
    Long adminId = validateAdmin(request);
    if (adminId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    List<AppLog> logs = adminService.getRecentAppLogs();
    return ResponseEntity.ok(logs);
  }

  // ============= CATEGORIES =============

  // Get all categories
  @GetMapping("/categories")
  public ResponseEntity<?> getCategories(HttpServletRequest request) {
    Long adminId = validateAdmin(request);
    if (adminId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    List<CategoryResponse> categories = categoryService.getAllActiveCategories();
    return ResponseEntity.ok(categories);
  }

  // Create new category
  @PostMapping("/categories")
  public ResponseEntity<?> createCategory(
      @RequestBody CategoryRequest categoryRequest, HttpServletRequest request) {
    Long adminId = validateAdmin(request);
    if (adminId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    try {
      CategoryResponse category = categoryService.createCategory(categoryRequest);
      return ResponseEntity.status(HttpStatus.CREATED).body(category);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  // Update category
  @PutMapping("/categories/{id}")
  public ResponseEntity<?> updateCategory(
      @PathVariable Long id,
      @RequestBody CategoryUpdateRequest updateRequest,
      HttpServletRequest request) {
    Long adminId = validateAdmin(request);
    if (adminId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    try {
      CategoryResponse category = categoryService.updateCategory(id, updateRequest);
      return ResponseEntity.ok(category);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  // Delete category (soft delete)
  @DeleteMapping("/categories/{id}")
  public ResponseEntity<?> deleteCategory(@PathVariable Long id, HttpServletRequest request) {
    Long adminId = validateAdmin(request);
    if (adminId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    try {
      categoryService.deactivateCategory(id);
      return ResponseEntity.ok(Map.of("success", true));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  // ============= COVER IMAGES =============

  // Get all cover images
  @GetMapping("/cover-images")
  public ResponseEntity<?> getCoverImages(HttpServletRequest request) {
    Long adminId = validateAdmin(request);
    if (adminId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    List<CoverImageDTO> images = coverImageService.getAllAvailableCoverImages();
    return ResponseEntity.ok(images);
  }

  // Upload new cover image
  @PostMapping("/cover-images")
  public ResponseEntity<?> uploadCoverImage(
      @RequestParam("file") MultipartFile file,
      @RequestParam("displayName") String displayName,
      HttpServletRequest request) {
    Long adminId = validateAdmin(request);
    if (adminId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    try {
      CoverImageDTO image = coverImageService.uploadCoverImage(file, displayName);
      return ResponseEntity.status(HttpStatus.CREATED).body(image);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  // Delete cover image (soft delete)
  @DeleteMapping("/cover-images/{id}")
  public ResponseEntity<?> deleteCoverImage(@PathVariable Long id, HttpServletRequest request) {
    Long adminId = validateAdmin(request);
    if (adminId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    try {
      coverImageService.deactivateCoverImage(id);
      return ResponseEntity.ok(Map.of("success", true));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  // Helper to validate admin token from request
  private Long validateAdmin(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return null;
    }
    String token = authHeader.substring(7);
    return adminService.validateAdminToken(token);
  }
}
