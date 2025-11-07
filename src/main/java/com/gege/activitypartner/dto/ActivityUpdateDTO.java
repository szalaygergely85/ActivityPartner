package com.gege.activitypartner.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityUpdateDTO {

    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Future(message = "Activity date must be in the future")
    private LocalDateTime activityDate;

    private String location;

    private String placeId;

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    private String category;

    @Min(value = 1, message = "Total spots must be at least 1")
    @Max(value = 100, message = "Total spots cannot exceed 100")
    private Integer totalSpots;

    @Min(value = 0, message = "Reserved spots cannot be negative")
    private Integer reservedForFriendsSpots;

    @Min(value = 1, message = "Minimum participants must be at least 1")
    private Integer minParticipants;

    private String difficulty;

    @Min(value = 0, message = "Cost cannot be negative")
    private Double cost;

    @Min(value = 0, message = "Minimum age cannot be negative")
    @Max(value = 100, message = "Minimum age cannot exceed 100")
    private Integer minAge;
}
