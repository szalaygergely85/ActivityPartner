package com.gege.activitypartner.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenRequest {

  @NotBlank(message = "FCM token is required")
  private String fcmToken;
}
