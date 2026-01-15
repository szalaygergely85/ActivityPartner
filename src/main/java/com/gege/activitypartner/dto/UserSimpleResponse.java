package com.gege.activitypartner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSimpleResponse {

  private Long id;
  private String fullName;
  private String profileImageUrl;
  private Double rating;
  private String badge;
}
