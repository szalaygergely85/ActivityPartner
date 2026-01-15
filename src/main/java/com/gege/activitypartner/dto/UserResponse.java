package com.gege.activitypartner.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private Long id;
  private String fullName;
  private String email;
  private String bio;
  private String profileImageUrl;
  private Double rating;
  private Integer completedActivities;
  private String city; // User's city name
  private String placeId; // Google Places ID for the city
  private Double latitude; // User's city latitude
  private Double longitude; // User's city longitude
  private List<String> interests;
  private String badge;
  private List<UserPhotoResponse> photos = new ArrayList<>(); // User's photos (up to 6)
  private String createdAt;
}
