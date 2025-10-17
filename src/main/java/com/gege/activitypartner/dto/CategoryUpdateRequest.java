package com.gege.activitypartner.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest {

    private String name;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;

    @Size(max = 10, message = "Icon must not exceed 10 characters")
    private String icon;

    private Boolean isActive;
}
