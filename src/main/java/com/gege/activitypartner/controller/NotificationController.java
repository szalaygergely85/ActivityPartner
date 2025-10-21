package com.gege.activitypartner.controller;

import com.gege.activitypartner.config.SecurityContextUtil;
import com.gege.activitypartner.dto.DeviceTokenRequest;
import com.gege.activitypartner.dto.NotificationPreferenceRequest;
import com.gege.activitypartner.dto.NotificationResponse;
import com.gege.activitypartner.entity.User;
import com.gege.activitypartner.repository.UserRepository;
import com.gege.activitypartner.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityContextUtil securityContextUtil;
    private final UserRepository userRepository;

    // Register/Update FCM device token
    @PostMapping("/device-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> registerDeviceToken(@Valid @RequestBody DeviceTokenRequest request) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationService.updateFcmToken(user.getId(), request.getFcmToken());
        return ResponseEntity.ok(Map.of("message", "Device token registered successfully"));
    }

    // Update notification preferences
    @PutMapping("/preferences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> updateNotificationPreferences(
            @Valid @RequestBody NotificationPreferenceRequest request) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationService.updateNotificationPreference(user.getId(), request.getNotificationsEnabled());
        return ResponseEntity.ok(Map.of(
                "message", "Notification preferences updated",
                "notificationsEnabled", request.getNotificationsEnabled().toString()
        ));
    }

    // Get all notifications for current user
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<NotificationResponse> notifications = notificationService.getUserNotifications(user.getId());
        return ResponseEntity.ok(notifications);
    }

    // Get unread notifications
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(user.getId());
        return ResponseEntity.ok(notifications);
    }

    // Get unread notification count
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // Mark notification as read
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    // Mark all notifications as read
    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    // Delete notification
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        String email = securityContextUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationService.deleteNotification(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
