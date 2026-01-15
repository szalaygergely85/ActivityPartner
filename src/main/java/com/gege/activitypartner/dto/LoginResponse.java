package com.gege.activitypartner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

  private String accessToken;
  private String refreshToken;
  private String type = "Bearer";
  private Long userId;
  private String email;
  private String fullName;
  private String profileImageUrl;
  private Double rating;
  private String badge;

  // Constructor without type (defaults to "Bearer")
  public LoginResponse(
      String accessToken,
      String refreshToken,
      Long userId,
      String email,
      String fullName,
      String profileImageUrl,
      Double rating,
      String badge) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.userId = userId;
    this.email = email;
    this.fullName = fullName;
    this.profileImageUrl = profileImageUrl;
    this.rating = rating;
    this.badge = badge;
  }
}
