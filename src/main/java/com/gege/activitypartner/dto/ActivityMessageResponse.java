package com.gege.activitypartner.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityMessageResponse {
  private Long id;
  private Long activityId;
  private Long userId;
  private String userName;
  private String userProfilePicture;
  private String messageText;
  private LocalDateTime createdAt;
  private Boolean isOwnMessage;
}
