package com.gege.activitypartner.controller;

import com.gege.activitypartner.config.SecurityContextUtil;
import com.gege.activitypartner.dto.ActivityRequestDTO;
import com.gege.activitypartner.dto.ActivityResponseDTO;
import com.gege.activitypartner.dto.ActivityUpdateDTO;
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
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ActivityResponseDTO response = activityService.createActivity(request, user.getId());
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
    public ResponseEntity<List<ActivityResponseDTO>> getAllActivities() {
        List<ActivityResponseDTO> activities = activityService.getAllActivities();
        return ResponseEntity.ok(activities);
    }

    // Get activities by creator
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<ActivityResponseDTO>> getActivitiesByCreator(@PathVariable Long creatorId) {
        List<ActivityResponseDTO> activities = activityService.getActivitiesByCreator(creatorId);
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
        List<ActivityResponseDTO> activities = activityService.getAvailableUpcomingActivities();
        return ResponseEntity.ok(activities);
    }

    // Get trending activities
    @GetMapping("/trending")
    public ResponseEntity<List<ActivityResponseDTO>> getTrendingActivities() {
        List<ActivityResponseDTO> activities = activityService.getTrendingActivities();
        return ResponseEntity.ok(activities);
    }

    // Update activity
    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivityResponseDTO> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody ActivityUpdateDTO updateDTO) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ActivityResponseDTO response = activityService.updateActivity(id, updateDTO, user.getId());
        return ResponseEntity.ok(response);
    }

    // Cancel activity
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivityResponseDTO> cancelActivity(@PathVariable Long id) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ActivityResponseDTO response = activityService.cancelActivity(id, user.getId());
        return ResponseEntity.ok(response);
    }

    // Complete activity
    @PatchMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivityResponseDTO> completeActivity(@PathVariable Long id) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ActivityResponseDTO response = activityService.completeActivity(id, user.getId());
        return ResponseEntity.ok(response);
    }

    // Delete activity
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        activityService.deleteActivity(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
