package com.gege.activitypartner.repository;

import com.gege.activitypartner.entity.Notification;
import com.gege.activitypartner.entity.NotificationType;
import com.gege.activitypartner.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  // Find all notifications for a user
  List<Notification> findByUserOrderByCreatedAtDesc(User user);

  // Find unread notifications for a user
  List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

  // Find notifications by type for a user
  List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, NotificationType type);

  // Count unread notifications for a user
  Long countByUserAndIsReadFalse(User user);

  // Mark notification as read
  @Modifying
  @Query(
      "UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.id = :notificationId")
  void markAsRead(
      @Param("notificationId") Long notificationId, @Param("readAt") LocalDateTime readAt);

  // Mark all notifications as read for a user
  @Modifying
  @Query(
      "UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.user.id = :userId AND n.isRead = false")
  void markAllAsReadForUser(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

  // Delete old read notifications (cleanup)
  @Modifying
  @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.readAt < :cutoffDate")
  void deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

  // Find notifications by activity
  List<Notification> findByActivityIdOrderByCreatedAtDesc(Long activityId);
}
