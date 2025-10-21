package com.gege.activitypartner.controller;

import com.gege.activitypartner.config.SecurityContextUtil;
import com.gege.activitypartner.dto.ActivityMessageRequest;
import com.gege.activitypartner.dto.ActivityMessageResponse;
import com.gege.activitypartner.entity.User;
import com.gege.activitypartner.repository.UserRepository;
import com.gege.activitypartner.service.ActivityMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities/{activityId}/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ActivityMessageController {

    private final ActivityMessageService messageService;
    private final SecurityContextUtil securityContextUtil;
    private final UserRepository userRepository;

    /**
     * Send a message in activity chat
     * POST /api/activities/{activityId}/messages
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivityMessageResponse> sendMessage(
            @PathVariable Long activityId,
            @Valid @RequestBody ActivityMessageRequest request) {

        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ActivityMessageResponse response = messageService.sendMessage(activityId, user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all messages for an activity
     * GET /api/activities/{activityId}/messages
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActivityMessageResponse>> getMessages(@PathVariable Long activityId) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ActivityMessageResponse> messages = messageService.getActivityMessages(activityId, user.getId());
        return ResponseEntity.ok(messages);
    }

    /**
     * Get messages since a specific timestamp (for polling)
     * GET /api/activities/{activityId}/messages/since?timestamp=2024-01-01T12:00:00
     */
    @GetMapping("/since")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActivityMessageResponse>> getMessagesSince(
            @PathVariable Long activityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp) {

        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ActivityMessageResponse> messages = messageService.getMessagesSince(activityId, user.getId(), timestamp);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get message count for an activity
     * GET /api/activities/{activityId}/messages/count
     */
    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getMessageCount(@PathVariable Long activityId) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long count = messageService.getMessageCount(activityId, user.getId());
        return ResponseEntity.ok(Map.of("messageCount", count));
    }

    /**
     * Delete a message
     * DELETE /api/activities/{activityId}/messages/{messageId}
     */
    @DeleteMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> deleteMessage(
            @PathVariable Long activityId,
            @PathVariable Long messageId) {

        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        messageService.deleteMessage(messageId, user.getId());
        return ResponseEntity.ok(Map.of("message", "Message deleted successfully"));
    }
}
