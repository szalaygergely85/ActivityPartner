package com.gege.activitypartner.controller;

import com.gege.activitypartner.config.SecurityContextUtil;
import com.gege.activitypartner.dto.ParticipantActivityResponse;
import com.gege.activitypartner.dto.ParticipantResponse;
import com.gege.activitypartner.dto.UpdateParticipantStatusRequest;
import com.gege.activitypartner.entity.ParticipantStatus;
import com.gege.activitypartner.service.ActivityParticipantService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Configure properly in production
public class ActivityParticipantController {

  private final ActivityParticipantService participantService;
  private final SecurityContextUtil securityContextUtil;

  // Express interest in an activity
  @PostMapping("/activities/{activityId}/interest")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ParticipantResponse> expressInterest(@PathVariable Long activityId) {
    Long userId = securityContextUtil.getCurrentUserId();
    ParticipantResponse response = participantService.expressInterest(activityId, userId);
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
  public ResponseEntity<List<ParticipantResponse>> getInterestedUsers(
      @PathVariable Long activityId) {
    Long userId = securityContextUtil.getCurrentUserId();
    List<ParticipantResponse> interested =
        participantService.getInterestedUsers(activityId, userId);
    return ResponseEntity.ok(interested);
  }

  // Get my participated activities
  @GetMapping("/my-participations")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<ParticipantActivityResponse>> getMyParticipations() {
    Long userId = securityContextUtil.getCurrentUserId();
    List<ParticipantActivityResponse> participations =
        participantService.getMyParticipations(userId);
    return ResponseEntity.ok(participations);
  }

  // Get my participations filtered by status
  @GetMapping("/my-participations/status/{status}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<ParticipantActivityResponse>> getMyParticipationsByStatus(
      @PathVariable ParticipantStatus status) {
    Long userId = securityContextUtil.getCurrentUserId();
    List<ParticipantActivityResponse> participations =
        participantService.getMyParticipationsByStatus(userId, status);
    return ResponseEntity.ok(participations);
  }

  // Accept or decline participant (creator only)
  @PatchMapping("/{participantId}/status")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ParticipantResponse> updateParticipantStatus(
      @PathVariable Long participantId,
      @Valid @RequestBody UpdateParticipantStatusRequest request) {
    Long userId = securityContextUtil.getCurrentUserId();
    ParticipantResponse response =
        participantService.updateParticipantStatus(participantId, request.getStatus(), userId);
    return ResponseEntity.ok(response);
  }

  // Confirm joining after acceptance
  @PostMapping("/{participantId}/confirm")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ParticipantResponse> confirmJoining(@PathVariable Long participantId) {
    Long userId = securityContextUtil.getCurrentUserId();
    ParticipantResponse response = participantService.confirmJoining(participantId, userId);
    return ResponseEntity.ok(response);
  }

  // Leave activity
  @DeleteMapping("/activities/{activityId}/leave")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> leaveActivity(@PathVariable Long activityId) {
    Long userId = securityContextUtil.getCurrentUserId();
    participantService.leaveActivity(activityId, userId);
    return ResponseEntity.noContent().build();
  }

  // Delete interest before acceptance
  @DeleteMapping("/activities/{activityId}/interest")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> deleteInterest(@PathVariable Long activityId) {
    Long userId = securityContextUtil.getCurrentUserId();
    participantService.deleteInterest(activityId, userId);
    return ResponseEntity.noContent().build();
  }
}
