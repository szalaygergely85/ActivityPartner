package com.gege.activitypartner.controller;

import com.gege.activitypartner.entity.AccountDeletionRequest;
import com.gege.activitypartner.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;

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
