package com.gege.activitypartner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityGalleryAccessResponse {

  private Boolean hasAccess;
  private String reason; // Explanation if no access
  private Boolean canUpload;
  private Integer photoCount;
  private Integer maxPhotos;
}
