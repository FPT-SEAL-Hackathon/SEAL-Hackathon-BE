package com.fpt.swp.sealhackathonbe.event.mapper;

import com.fpt.swp.sealhackathonbe.event.dto.response.EventResponse;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.eventparticipant.entity.EventParticipant;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
    private static final String NOT_REGISTERED = "NOT_REGISTERED";

    public EventResponse toEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getEventId())
                .eventId(event.getEventId())
                .name(event.getEventName())
                .title(event.getEventName())
                .eventName(event.getEventName())
                .description(event.getDescription())
                .location(event.getLocation())
                .bannerImageUrl(event.getBannerImageUrl())
                .eventStatus(event.getEventStatus())
                .eventStatusId(event.getEventStatus() != null ? event.getEventStatus().getEventStatusId() : null)
                .eventStatusName(event.getEventStatus() != null ? event.getEventStatus().getEventStatusName() : null)
                .registrationStart(event.getRegistrationStart())
                .registrationEnd(event.getRegistrationEnd())
                .registrationDeadline(event.getRegistrationEnd())
                .startDate(event.getEventStartDate())
                .eventStartDate(event.getEventStartDate())
                .endDate(event.getEventEndDate())
                .eventEndDate(event.getEventEndDate())
                .maxTeamSize(event.getMaxTeamSize())
                .minTeamSize(event.getMinTeamSize())
                .createdById(event.getCreatedBy().getUserId())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    public EventResponse toEventResponse(Event event, EventParticipant participant) {
        String participationStatus = participationStatus(participant);
        return EventResponse.builder()
                .id(event.getEventId())
                .eventId(event.getEventId())
                .name(event.getEventName())
                .title(event.getEventName())
                .eventName(event.getEventName())
                .description(event.getDescription())
                .location(event.getLocation())
                .bannerImageUrl(event.getBannerImageUrl())
                .eventStatus(event.getEventStatus())
                .eventStatusId(event.getEventStatus() != null ? event.getEventStatus().getEventStatusId() : null)
                .eventStatusName(event.getEventStatus() != null ? event.getEventStatus().getEventStatusName() : null)
                .registrationStart(event.getRegistrationStart())
                .registrationEnd(event.getRegistrationEnd())
                .registrationDeadline(event.getRegistrationEnd())
                .startDate(event.getEventStartDate())
                .eventStartDate(event.getEventStartDate())
                .endDate(event.getEventEndDate())
                .eventEndDate(event.getEventEndDate())
                .maxTeamSize(event.getMaxTeamSize())
                .minTeamSize(event.getMinTeamSize())
                .createdById(event.getCreatedBy().getUserId())
                .eventParticipantId(participant != null ? participant.getEventParticipantId() : null)
                .userParticipationStatus(participationStatus)
                .myParticipantId(participant != null ? participant.getEventParticipantId() : null)
                .myRegistrationStatus(participationStatus)
                .participantStatus(participationStatus)
                .rejectedReason(participant != null ? participant.getRejectedReason() : null)
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .appliedAt(participant != null ? participant.getAppliedAt() : null)
                .approvedAt(participant != null ? participant.getApprovedAt() : null)
                .build();
    }

    private String participationStatus(EventParticipant participant) {
        if (participant == null || participant.getParticipantStatus() == null) {
            return NOT_REGISTERED;
        }

        return toApiParticipantStatus(participant.getParticipantStatus().getStatusName());
    }

    private String toApiParticipantStatus(String statusName) {
        if ("PENDING_APPROVAL".equalsIgnoreCase(statusName)) {
            return "PENDING";
        }
        return statusName;
    }
}
