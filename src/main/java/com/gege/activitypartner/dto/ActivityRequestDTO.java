package com.gege.activitypartner.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
}
