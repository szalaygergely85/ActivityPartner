package com.gege.activitypartner.dto;

import com.gege.activitypartner.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
  private Long id;
  private String title;
  private String message;
  private NotificationType type;
  private Boolean isRead;
  private Long activityId;
  private Long participantId;
  private Long reviewId;
  private String createdAt;
  private String readAt;
}
