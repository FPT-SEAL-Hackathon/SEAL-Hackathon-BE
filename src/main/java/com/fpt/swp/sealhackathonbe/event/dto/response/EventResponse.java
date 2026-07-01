package com.fpt.swp.sealhackathonbe.event.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private UUID eventStatusId;
    private String eventStatusName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationEnd;

    private LocalDate eventStartDate;
    private LocalDate eventEndDate;

    private Integer maxTeamSize;
    private Integer minTeamSize;

    private UUID createdById;

    private String participantStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
