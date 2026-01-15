package com.gege.activitypartner.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityPhotoResponse {

  private Long id;
  private Long activityId;
  private Long userId;
  private String userName;
  private String userAvatar;
  private String photoUrl;
  private Integer displayOrder;
  private LocalDateTime uploadedAt;
}
