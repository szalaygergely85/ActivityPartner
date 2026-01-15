package com.gege.activitypartner.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPhotoResponse {

  private Long id;
  private String photoUrl;
  private Boolean isProfilePicture;
  private Integer displayOrder;
  private LocalDateTime uploadedAt;
}
