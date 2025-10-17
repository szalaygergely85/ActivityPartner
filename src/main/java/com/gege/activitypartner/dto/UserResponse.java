package com.gege.activitypartner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<String> interests;
    private String badge;
    private String createdAt;
}
