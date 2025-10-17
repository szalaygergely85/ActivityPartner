package com.gege.activitypartner.controller;

import com.gege.activitypartner.dto.ParticipantActivityResponse;
import com.gege.activitypartner.dto.ParticipantResponse;
import com.gege.activitypartner.dto.UpdateParticipantStatusRequest;
import com.gege.activitypartner.entity.ParticipantStatus;
import com.gege.activitypartner.service.ActivityParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Configure properly in production
public class ActivityParticipantController {

    private final ActivityParticipantService participantService;

    // Express interest in an activity
    @PostMapping("/activities/{activityId}/interest")
    public ResponseEntity<ParticipantResponse> expressInterest(
            @PathVariable Long activityId,
            @RequestParam Long userId) { // TODO: Replace with authenticated user from JWT
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
    public ResponseEntity<List<ParticipantResponse>> getInterestedUsers(
            @PathVariable Long activityId,
            @RequestParam Long creatorId) { // TODO: Replace with authenticated user from JWT
        List<ParticipantResponse> interested = participantService.getInterestedUsers(activityId, creatorId);
        return ResponseEntity.ok(interested);
    }

    // Get my participated activities
    @GetMapping("/my-participations")
    public ResponseEntity<List<ParticipantActivityResponse>> getMyParticipations(
            @RequestParam Long userId) { // TODO: Replace with authenticated user from JWT
        List<ParticipantActivityResponse> participations = participantService.getMyParticipations(userId);
        return ResponseEntity.ok(participations);
    }

    // Get my participations filtered by status
    @GetMapping("/my-participations/status/{status}")
    public ResponseEntity<List<ParticipantActivityResponse>> getMyParticipationsByStatus(
            @PathVariable ParticipantStatus status,
            @RequestParam Long userId) { // TODO: Replace with authenticated user from JWT
        List<ParticipantActivityResponse> participations = participantService.getMyParticipationsByStatus(userId, status);
        return ResponseEntity.ok(participations);
    }

    // Accept or decline participant (creator only)
    @PatchMapping("/{participantId}/status")
    public ResponseEntity<ParticipantResponse> updateParticipantStatus(
            @PathVariable Long participantId,
            @Valid @RequestBody UpdateParticipantStatusRequest request,
            @RequestParam Long creatorId) { // TODO: Replace with authenticated user from JWT
        ParticipantResponse response = participantService.updateParticipantStatus(
                participantId, request.getStatus(), creatorId);
        return ResponseEntity.ok(response);
    }

    // Confirm joining after acceptance
    @PostMapping("/{participantId}/confirm")
    public ResponseEntity<ParticipantResponse> confirmJoining(
            @PathVariable Long participantId,
            @RequestParam Long userId) { // TODO: Replace with authenticated user from JWT
        ParticipantResponse response = participantService.confirmJoining(participantId, userId);
        return ResponseEntity.ok(response);
    }

    // Leave activity
    @DeleteMapping("/activities/{activityId}/leave")
    public ResponseEntity<Void> leaveActivity(
            @PathVariable Long activityId,
            @RequestParam Long userId) { // TODO: Replace with authenticated user from JWT
        participantService.leaveActivity(activityId, userId);
        return ResponseEntity.noContent().build();
    }

    // Delete interest before acceptance
    @DeleteMapping("/activities/{activityId}/interest")
    public ResponseEntity<Void> deleteInterest(
            @PathVariable Long activityId,
            @RequestParam Long userId) { // TODO: Replace with authenticated user from JWT
        participantService.deleteInterest(activityId, userId);
        return ResponseEntity.noContent().build();
    }
}
