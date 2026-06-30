package com.fpt.swp.sealhackathonbe.eventparticipant.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class EventParticipantResponse {
    private UUID id;
    private UUID participantId;
    private UUID eventId;
    private String eventName;
    private String eventStatus;
    private UUID studentId;
    private String studentName;
    private String studentEmail;
    private EventParticipantUserResponse user;
    private EventParticipantEventResponse event;
    private String currentStatus;
    private String participantStatus;
    private LocalDateTime registeredAt;
    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;
    private EventParticipantUserResponse approvedBy;
    private String rejectedReason;
}
