package com.gege.activitypartner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String icon;
    private String imageResourceName;
    private Boolean isActive;
    private Integer activityCount;
    private LocalDateTime createdAt;
}
