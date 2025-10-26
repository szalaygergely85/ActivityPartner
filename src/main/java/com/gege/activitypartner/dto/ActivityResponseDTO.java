package com.gege.activitypartner.dto;

import com.gege.activitypartner.entity.ActivityStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponseDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime activityDate;
    private String location;
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

    // Creator Info
    private UserSimpleResponse creator;

    // Participants count
    private Integer participantsCount;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
