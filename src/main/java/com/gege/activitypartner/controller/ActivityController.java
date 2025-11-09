package com.gege.activitypartner.controller;

import com.gege.activitypartner.config.SecurityContextUtil;
import com.gege.activitypartner.dto.ActivityRequestDTO;
import com.gege.activitypartner.dto.ActivityResponseDTO;
import com.gege.activitypartner.dto.ActivityUpdateDTO;
import com.gege.activitypartner.entity.ActivityStatus;
import com.gege.activitypartner.entity.User;
import com.gege.activitypartner.repository.UserRepository;
import com.gege.activitypartner.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Configure properly in production
public class ActivityController {

    private final ActivityService activityService;
    private final SecurityContextUtil securityContextUtil;
    private final UserRepository userRepository;

    // Create new activity
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivityResponseDTO> createActivity(@Valid @RequestBody ActivityRequestDTO request) {
        Long userId = securityContextUtil.getCurrentUserId();
        ActivityResponseDTO response = activityService.createActivity(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get activity by ID
    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponseDTO> getActivityById(@PathVariable Long id) {
        ActivityResponseDTO response = activityService.getActivityById(id);
        return ResponseEntity.ok(response);
    }

    // Get all activities
    @GetMapping
    public ResponseEntity<List<ActivityResponseDTO>> getAllActivities(
            @RequestParam(required = false) Double userLatitude,
            @RequestParam(required = false) Double userLongitude) {
        List<ActivityResponseDTO> activities;

        // If user coordinates are provided, calculate distances
        if (userLatitude != null && userLongitude != null) {
            activities = activityService.getAllActivitiesWithDistance(userLatitude, userLongitude);
        } else {
            activities = activityService.getAllActivities();
        }

        return ResponseEntity.ok(activities);
    }

    // Get activities by creator
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<ActivityResponseDTO>> getActivitiesByCreator(@PathVariable Long creatorId) {
        List<ActivityResponseDTO> activities = activityService.getActivitiesByCreator(creatorId);
        return ResponseEntity.ok(activities);
    }

    // Get my activities (current user) with optional status filter
    @GetMapping("/my-activities")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActivityResponseDTO>> getMyActivities(
            @RequestParam(required = false) ActivityStatus status) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<ActivityResponseDTO> activities = activityService.getMyActivities(user.getId(), status);
        return ResponseEntity.ok(activities);
    }

    // Get activities by category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ActivityResponseDTO>> getActivitiesByCategory(@PathVariable String category) {
        List<ActivityResponseDTO> activities = activityService.getActivitiesByCategory(category);
        return ResponseEntity.ok(activities);
    }

    // Get available upcoming activities
    @GetMapping("/upcoming")
    public ResponseEntity<List<ActivityResponseDTO>> getAvailableUpcomingActivities() {
        Long userId = securityContextUtil.getCurrentUserId();
        List<ActivityResponseDTO> activities = activityService.getAvailableUpcomingActivities(userId);
        return ResponseEntity.ok(activities);
    }

    // Get trending activities
    @GetMapping("/trending")
    public ResponseEntity<List<ActivityResponseDTO>> getTrendingActivities() {
        List<ActivityResponseDTO> activities = activityService.getTrendingActivities();
        return ResponseEntity.ok(activities);
    }

    // Get nearby activities within radius
    @GetMapping("/nearby")
    public ResponseEntity<List<ActivityResponseDTO>> getNearbyActivities(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radiusKm) {
        List<ActivityResponseDTO> activities = activityService.getNearbyActivities(latitude, longitude, radiusKm);
        return ResponseEntity.ok(activities);
    }

    // Get recommended activities based on user's interests
    @GetMapping("/recommended")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActivityResponseDTO>> getRecommendedActivities() {
        Long userId = securityContextUtil.getCurrentUserId();
        List<ActivityResponseDTO> activities = activityService.getRecommendedActivities(userId);
        return ResponseEntity.ok(activities);
    }

    // Update activity (partial update)
    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivityResponseDTO> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody ActivityUpdateDTO updateDTO) {
        Long userId = securityContextUtil.getCurrentUserId();
        ActivityResponseDTO response = activityService.updateActivity(id, updateDTO, userId);
        return ResponseEntity.ok(response);
    }

    // Update activity (full update)
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivityResponseDTO> updateActivityFull(
            @PathVariable Long id,
            @Valid @RequestBody ActivityUpdateDTO updateDTO) {
        Long userId = securityContextUtil.getCurrentUserId();
        ActivityResponseDTO response = activityService.updateActivity(id, updateDTO, userId);
        return ResponseEntity.ok(response);
    }

    // Cancel activity
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivityResponseDTO> cancelActivity(@PathVariable Long id) {
        Long userId = securityContextUtil.getCurrentUserId();
        ActivityResponseDTO response = activityService.cancelActivity(id, userId);
        return ResponseEntity.ok(response);
    }

    // Complete activity
    @PatchMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivityResponseDTO> completeActivity(@PathVariable Long id) {
        Long userId = securityContextUtil.getCurrentUserId();
        ActivityResponseDTO response = activityService.completeActivity(id, userId);
        return ResponseEntity.ok(response);
    }

    // Delete activity
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        Long userId = securityContextUtil.getCurrentUserId();
        activityService.deleteActivity(id, userId);
        return ResponseEntity.noContent().build();
    }
}
