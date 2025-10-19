package com.gege.activitypartner.controller;

import com.gege.activitypartner.config.SecurityContextUtil;
import com.gege.activitypartner.dto.ParticipantActivityResponse;
import com.gege.activitypartner.dto.ParticipantResponse;
import com.gege.activitypartner.dto.UpdateParticipantStatusRequest;
import com.gege.activitypartner.entity.ParticipantStatus;
import com.gege.activitypartner.entity.User;
import com.gege.activitypartner.repository.UserRepository;
import com.gege.activitypartner.service.ActivityParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Configure properly in production
public class ActivityParticipantController {

    private final ActivityParticipantService participantService;
    private final SecurityContextUtil securityContextUtil;
    private final UserRepository userRepository;

    // Express interest in an activity
    @PostMapping("/activities/{activityId}/interest")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ParticipantResponse> expressInterest(@PathVariable Long activityId) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ParticipantResponse response = participantService.expressInterest(activityId, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all participants for an activity
    @GetMapping("/activities/{activityId}")
    public ResponseEntity<List<ParticipantResponse>> getActivityParticipants(
            @PathVariable Long activityId) {
        List<ParticipantResponse> participants = participantService.getActivityParticipants(activityId);
        return ResponseEntity.ok(participants);
    }

    // Get interested users for an activity (creator only)
    @GetMapping("/activities/{activityId}/interested")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ParticipantResponse>> getInterestedUsers(@PathVariable Long activityId) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<ParticipantResponse> interested = participantService.getInterestedUsers(activityId, user.getId());
        return ResponseEntity.ok(interested);
    }

    // Get my participated activities
    @GetMapping("/my-participations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ParticipantActivityResponse>> getMyParticipations() {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<ParticipantActivityResponse> participations = participantService.getMyParticipations(user.getId());
        return ResponseEntity.ok(participations);
    }

    // Get my participations filtered by status
    @GetMapping("/my-participations/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ParticipantActivityResponse>> getMyParticipationsByStatus(
            @PathVariable ParticipantStatus status) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<ParticipantActivityResponse> participations = participantService.getMyParticipationsByStatus(user.getId(), status);
        return ResponseEntity.ok(participations);
    }

    // Accept or decline participant (creator only)
    @PatchMapping("/{participantId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ParticipantResponse> updateParticipantStatus(
            @PathVariable Long participantId,
            @Valid @RequestBody UpdateParticipantStatusRequest request) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ParticipantResponse response = participantService.updateParticipantStatus(
                participantId, request.getStatus(), user.getId());
        return ResponseEntity.ok(response);
    }

    // Confirm joining after acceptance
    @PostMapping("/{participantId}/confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ParticipantResponse> confirmJoining(@PathVariable Long participantId) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ParticipantResponse response = participantService.confirmJoining(participantId, user.getId());
        return ResponseEntity.ok(response);
    }

    // Leave activity
    @DeleteMapping("/activities/{activityId}/leave")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> leaveActivity(@PathVariable Long activityId) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        participantService.leaveActivity(activityId, user.getId());
        return ResponseEntity.noContent().build();
    }

    // Delete interest before acceptance
    @DeleteMapping("/activities/{activityId}/interest")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteInterest(@PathVariable Long activityId) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        participantService.deleteInterest(activityId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
