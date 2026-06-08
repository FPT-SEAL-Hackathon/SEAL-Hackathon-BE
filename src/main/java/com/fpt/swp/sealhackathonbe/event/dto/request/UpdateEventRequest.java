package com.fpt.swp.sealhackathonbe.event.dto.request;

import com.fpt.swp.sealhackathonbe.event.entity.EventStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class UpdateEventRequest {
    private String eventName;
    private String description;
    private String location;
    private String bannerImageUrl;

    private UUID eventStatusId;

    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;

    private LocalDate eventStartDate;
    private LocalDate eventEndDate;

    private Integer maxTeamSize;
    private Integer minTeamSize;
}
