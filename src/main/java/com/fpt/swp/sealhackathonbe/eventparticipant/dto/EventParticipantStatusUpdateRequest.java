package com.fpt.swp.sealhackathonbe.eventparticipant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

public class EventParticipantStatusUpdateRequest {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|ACTIVE|REJECTED", message = "Invalid participant status. Allowed values are: PENDING, ACTIVE, REJECTED.")
    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private String rejectedReason;
}
