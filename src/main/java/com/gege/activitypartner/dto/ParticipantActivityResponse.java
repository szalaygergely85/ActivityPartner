package com.gege.activitypartner.dto;

import com.gege.activitypartner.entity.ParticipantStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantActivityResponse {

  private Long participantId;
  private ActivityResponseDTO activity;
  private ParticipantStatus status;
  private Boolean isFriend;
  private LocalDateTime joinedAt;
  private LocalDateTime updatedAt;
}
