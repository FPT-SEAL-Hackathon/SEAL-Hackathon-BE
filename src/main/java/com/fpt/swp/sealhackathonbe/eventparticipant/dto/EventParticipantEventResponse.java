package com.fpt.swp.sealhackathonbe.eventparticipant.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class EventParticipantEventResponse {
    private UUID eventId;
    private String eventName;
    private String eventStatusName;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private LocalDateTime eventStartDate;
    private LocalDateTime eventEndDate;
    private String bannerImageUrl;
}
