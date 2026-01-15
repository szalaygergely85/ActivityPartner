package com.gege.activitypartner.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

  private Long id;
  private Integer rating;
  private String comment;
  private Long activityId;
  private UserSimpleResponse reviewer;
  private UserSimpleResponse reviewedUser;
  private LocalDateTime createdAt;
}
