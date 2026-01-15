package com.gege.activitypartner.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRequest {

  @NotNull(message = "Notification preference is required")
  private Boolean notificationsEnabled;
}
