package com.gege.activitypartner.controller;

import com.gege.activitypartner.config.SecurityContextUtil;
import com.gege.activitypartner.dto.DeviceTokenRequest;
import com.gege.activitypartner.dto.NotificationPreferenceRequest;
import com.gege.activitypartner.dto.NotificationResponse;
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

    // Register/Update FCM device token
    @PostMapping("/device-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> registerDeviceToken(@Valid @RequestBody DeviceTokenRequest request) {
        Long userId = securityContextUtil.getCurrentUserId();

        notificationService.updateFcmToken(userId, request.getFcmToken());
        return ResponseEntity.ok(Map.of("message", "Device token registered successfully"));
    }

    // Update notification preferences
    @PutMapping("/preferences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> updateNotificationPreferences(
            @Valid @RequestBody NotificationPreferenceRequest request) {
        Long userId = securityContextUtil.getCurrentUserId();

        notificationService.updateNotificationPreference(userId, request.getNotificationsEnabled());
        return ResponseEntity.ok(Map.of(
                "message", "Notification preferences updated",
                "notificationsEnabled", request.getNotificationsEnabled().toString()
        ));
    }

    // Get all notifications for current user
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        Long userId = securityContextUtil.getCurrentUserId();

        List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    // Get unread notifications
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {
        Long userId = securityContextUtil.getCurrentUserId();

        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    // Get unread notification count
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        Long userId = securityContextUtil.getCurrentUserId();

        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // Mark notification as read
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        Long userId = securityContextUtil.getCurrentUserId();

        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    // Mark all notifications as read
    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        Long userId = securityContextUtil.getCurrentUserId();

        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    // Delete notification
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        Long userId = securityContextUtil.getCurrentUserId();

        notificationService.deleteNotification(id, userId);
        return ResponseEntity.noContent().build();
    }
}
