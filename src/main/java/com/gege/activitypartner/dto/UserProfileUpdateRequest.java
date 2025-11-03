package com.gege.activitypartner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {

    private String fullName;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    private String profileImageUrl;

    private String city;

    private String placeId;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private List<String> interests;
}
