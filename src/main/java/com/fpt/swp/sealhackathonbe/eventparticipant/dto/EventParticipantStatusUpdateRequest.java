package com.fpt.swp.sealhackathonbe.eventparticipant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class EventParticipantStatusUpdateRequest {
    @NotBlank(message = "Status is required")
    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private String rejectedReason;
}
