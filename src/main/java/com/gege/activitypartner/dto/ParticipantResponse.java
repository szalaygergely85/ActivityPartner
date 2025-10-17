package com.gege.activitypartner.dto;

import com.gege.activitypartner.entity.ParticipantStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {

    private Long id;
    private Long activityId;
    private String activityTitle;
    private UserSimpleResponse user;
    private ParticipantStatus status;
    private Boolean isFriend;
    private LocalDateTime joinedAt;
    private LocalDateTime updatedAt;
}
