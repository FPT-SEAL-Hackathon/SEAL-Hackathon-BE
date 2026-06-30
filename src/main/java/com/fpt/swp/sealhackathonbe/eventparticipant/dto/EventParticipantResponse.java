package com.fpt.swp.sealhackathonbe.eventparticipant.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class EventParticipantResponse {
    private UUID participantId;
    private EventParticipantUserResponse user;
    private EventParticipantEventResponse event;
    private String currentStatus;
    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;
    private EventParticipantUserResponse approvedBy;
    private String rejectedReason;
}
