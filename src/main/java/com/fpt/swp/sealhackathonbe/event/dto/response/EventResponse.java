package com.fpt.swp.sealhackathonbe.event.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.entity.EventStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {
    private UUID eventId;
    private String eventName;
    private String description;
    private String location;
    private String bannerImageUrl;

    private EventStatus eventStatus;

    private LocalDateTime registrationStart;

    private LocalDateTime registrationEnd;

    private LocalDate eventStartDate;
    private LocalDate eventEndDate;

    private Integer maxTeamSize;
    private Integer minTeamSize;

    private UUID createdById;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
