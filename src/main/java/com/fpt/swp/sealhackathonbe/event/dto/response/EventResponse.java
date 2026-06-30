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
    private UUID id;
    private UUID eventId;
    private String name;
    private String title;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDeadline;

    private LocalDate startDate;
    private LocalDate eventStartDate;
    private LocalDate endDate;
    private LocalDate eventEndDate;

    private Integer maxTeamSize;
    private Integer minTeamSize;

    private UUID createdById;

    private UUID eventParticipantId;
    private String userParticipationStatus;
    private UUID myParticipantId;
    private String myRegistrationStatus;
    private String participantStatus;
    private String rejectedReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appliedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;
}
