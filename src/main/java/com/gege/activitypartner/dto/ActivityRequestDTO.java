package com.gege.activitypartner.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRequestDTO {

  @NotBlank(message = "Title is required")
  @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
  private String title;

  @Size(max = 1000, message = "Description cannot exceed 1000 characters")
  private String description;

  @NotNull(message = "Activity date is required")
  @Future(message = "Activity date must be in the future")
  private LocalDateTime activityDate;

  @NotBlank(message = "Location is required")
  private String location;

  private String placeId; // Google Places ID for the location

  @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
  @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
  private Double latitude; // Geographic latitude from Google Maps

  @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
  @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
  private Double longitude; // Geographic longitude from Google Maps

  @NotBlank(message = "Category is required")
  private String category;

  @NotNull(message = "Total spots is required")
  @Min(value = 1, message = "Total spots must be at least 1")
  @Max(value = 100, message = "Total spots cannot exceed 100")
  private Integer totalSpots;

  @Min(value = 0, message = "Reserved spots cannot be negative")
  private Integer reservedForFriendsSpots = 0;

  @Min(value = 1, message = "Minimum participants must be at least 1")
  private Integer minParticipants;

  private String difficulty; // "Easy", "Moderate", "Hard"

  @Min(value = 0, message = "Cost cannot be negative")
  private Double cost = 0.0;

  @Min(value = 0, message = "Minimum age cannot be negative")
  @Max(value = 100, message = "Minimum age cannot exceed 100")
  private Integer minAge;

  private List<String>
      interests; // Activity interests/tags (e.g., ["outdoor", "nature", "exercise"])

  private String coverImageUrl; // Optional user-selected cover image URL
}
