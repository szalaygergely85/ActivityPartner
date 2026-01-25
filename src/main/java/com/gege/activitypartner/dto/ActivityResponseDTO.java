package com.gege.activitypartner.dto;

import com.gege.activitypartner.entity.ActivityStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponseDTO {

  private Long id;
  private String title;
  private String description;
  private LocalDateTime activityDate;
  private String location;
  private String placeId; // Google Places ID for the location
  private BigDecimal latitude; // Geographic latitude
  private BigDecimal longitude; // Geographic longitude
  private Double distance; // Distance from user's location in kilometers (calculated dynamically)
  private String category;

  // Spots Management
  private Integer totalSpots;
  private Integer availableSpots;
  private Integer reservedForFriendsSpots;
  private Integer minParticipants;

  // Status & Metadata
  private ActivityStatus status;
  private Boolean trending;

  // Additional Info
  private String difficulty;
  private Double cost;
  private Integer minAge;

  // Activity Interests/Tags
  private List<String> interests;

  // Cover Image
  private String coverImageUrl; // User-selected cover image URL (null uses category default)

  // Creator Info
  private UserSimpleResponse creator;

  // Participants count
  private Integer participantsCount;

  // Timestamps
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
