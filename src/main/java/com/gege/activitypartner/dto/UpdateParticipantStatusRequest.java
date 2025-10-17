package com.gege.activitypartner.dto;

import com.gege.activitypartner.entity.ParticipantStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateParticipantStatusRequest {

    @NotNull(message = "Status is required")
    private ParticipantStatus status;
}
