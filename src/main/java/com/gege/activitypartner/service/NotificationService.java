package com.gege.activitypartner.service;

import com.gege.activitypartner.dto.NotificationResponse;
import com.gege.activitypartner.entity.Notification;
import com.gege.activitypartner.entity.NotificationType;
import com.gege.activitypartner.entity.User;
import com.gege.activitypartner.exception.ResourceNotFoundException;
import com.gege.activitypartner.repository.NotificationRepository;
import com.gege.activitypartner.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final FirebaseMessagingService firebaseMessagingService;
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  /** Create and send a notification to a user */
  @Transactional
  public Notification createAndSendNotification(
      User user,
      String title,
      String message,
      NotificationType type,
      Long activityId,
      Long participantId,
      Long reviewId) {
    // Create notification in database
    Notification notification = new Notification();
    notification.setUser(user);
    notification.setTitle(title);
    notification.setMessage(message);
    notification.setType(type);
    notification.setActivityId(activityId);
    notification.setParticipantId(participantId);
    notification.setReviewId(reviewId);
    notification.setIsRead(false);
    notification.setIsSent(false);

    notification = notificationRepository.save(notification);

    // Send push notification if user has the relevant preference enabled and has an FCM token
    if (user.getFcmToken() != null
        && !user.getFcmToken().isEmpty()
        && isNotificationAllowed(user, type)) {
      Map<String, String> data = buildNotificationData(type, activityId, participantId, reviewId);
      boolean sent =
          firebaseMessagingService.sendNotification(user.getFcmToken(), title, message, data);
      notification.setIsSent(sent);
      notificationRepository.save(notification);
    }

    return notification;
  }

  /** Check whether a notification type is allowed by the user's preferences */
  private boolean isNotificationAllowed(User user, NotificationType type) {
    if (!Boolean.TRUE.equals(user.getNotificationsEnabled())) {
      return false;
    }
    if (type == NotificationType.ACTIVITY_REMINDER) {
      return Boolean.TRUE.equals(user.getRemindersEnabled());
    }
    switch (type) {
      case ACTIVITY_CREATED:
      case ACTIVITY_UPDATED:
      case ACTIVITY_CANCELLED:
      case ACTIVITY_COMPLETED:
      case PARTICIPANT_INTERESTED:
      case PARTICIPANT_ACCEPTED:
      case PARTICIPANT_DECLINED:
      case PARTICIPANT_JOINED:
      case PARTICIPANT_LEFT:
      case REVIEW_RECEIVED:
      case NEW_MESSAGE:
        return Boolean.TRUE.equals(user.getActivityUpdatesEnabled());
      default:
        return true; // System notifications (badges, milestones, general) are always sent
    }
  }

  /** Build data payload for FCM notification */
  private Map<String, String> buildNotificationData(
      NotificationType type, Long activityId, Long participantId, Long reviewId) {
    Map<String, String> data = new HashMap<>();
    data.put("type", type.name());

    if (activityId != null) {
      data.put("activityId", activityId.toString());
      data.put("screen", "ActivityDetail");
    }
    if (participantId != null) {
      data.put("participantId", participantId.toString());
    }
    if (reviewId != null) {
      data.put("reviewId", reviewId.toString());
      data.put("screen", "Reviews");
    }

    return data;
  }

  /** Get all notifications for a user */
  public List<NotificationResponse> getUserNotifications(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
        .map(this::convertToResponse)
        .collect(Collectors.toList());
  }

  /** Get unread notifications for a user */
  public List<NotificationResponse> getUnreadNotifications(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user).stream()
        .map(this::convertToResponse)
        .collect(Collectors.toList());
  }

  /** Get unread notification count */
  public Long getUnreadCount(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    return notificationRepository.countByUserAndIsReadFalse(user);
  }

  /** Mark notification as read */
  @Transactional
  public void markAsRead(Long notificationId, Long userId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

    // Verify notification belongs to user
    if (!notification.getUser().getId().equals(userId)) {
      throw new RuntimeException("Unauthorized access to notification");
    }

    if (!notification.getIsRead()) {
      notificationRepository.markAsRead(notificationId, LocalDateTime.now());
    }
  }

  /** Mark all notifications as read for a user */
  @Transactional
  public void markAllAsRead(Long userId) {
    notificationRepository.markAllAsReadForUser(userId, LocalDateTime.now());
  }

  /** Delete a notification */
  @Transactional
  public void deleteNotification(Long notificationId, Long userId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

    // Verify notification belongs to user
    if (!notification.getUser().getId().equals(userId)) {
      throw new RuntimeException("Unauthorized access to notification");
    }

    notificationRepository.delete(notification);
  }

  /** Update user FCM token */
  @Transactional
  public void updateFcmToken(Long userId, String fcmToken) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    user.setFcmToken(fcmToken);
    userRepository.save(user);
    log.info("Updated FCM token for user: {}", userId);
  }

  /** Update notification preferences */
  @Transactional
  public void updateNotificationPreference(
      Long userId, Boolean activityUpdatesEnabled, Boolean remindersEnabled) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    user.setActivityUpdatesEnabled(activityUpdatesEnabled);
    user.setRemindersEnabled(remindersEnabled);
    userRepository.save(user);
    log.info(
        "Updated notification preferences for user {}: activityUpdates={}, reminders={}",
        userId,
        activityUpdatesEnabled,
        remindersEnabled);
  }

  /** Cleanup old read notifications (can be scheduled) */
  @Transactional
  public void cleanupOldNotifications(int daysOld) {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
    notificationRepository.deleteOldReadNotifications(cutoffDate);
    log.info("Cleaned up notifications older than {} days", daysOld);
  }

  /** Convert Notification entity to NotificationResponse DTO */
  private NotificationResponse convertToResponse(Notification notification) {
    NotificationResponse response = new NotificationResponse();
    response.setId(notification.getId());
    response.setTitle(notification.getTitle());
    response.setMessage(notification.getMessage());
    response.setType(notification.getType());
    response.setIsRead(notification.getIsRead());
    response.setActivityId(notification.getActivityId());
    response.setParticipantId(notification.getParticipantId());
    response.setReviewId(notification.getReviewId());
    response.setCreatedAt(notification.getCreatedAt().format(DATE_FORMATTER));
    response.setReadAt(
        notification.getReadAt() != null ? notification.getReadAt().format(DATE_FORMATTER) : null);
    return response;
  }
}
