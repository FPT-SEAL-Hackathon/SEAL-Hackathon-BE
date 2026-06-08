package com.fpt.swp.sealhackathonbe.event.dto;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class EventDTO {
    private UUID eventId;
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
    private UUID createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}
