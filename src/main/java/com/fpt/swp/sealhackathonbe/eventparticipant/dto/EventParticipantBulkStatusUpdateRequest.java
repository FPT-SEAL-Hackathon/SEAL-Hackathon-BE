package com.fpt.swp.sealhackathonbe.eventparticipant.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

public class EventParticipantBulkStatusUpdateRequest {
    @NotEmpty(message = "Participant IDs are required")
    @Getter
    @Setter
    private List<UUID> participantIds;

    @NotBlank(message = "Status is required")
    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private String rejectedReason;
}
