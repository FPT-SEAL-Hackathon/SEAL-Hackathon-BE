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
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .description(event.getDescription())
                .location(event.getLocation())
                .bannerImageUrl(event.getBannerImageUrl())
                .eventStatus(event.getEventStatus())
                .eventStatusId(event.getEventStatus() != null ? event.getEventStatus().getEventStatusId() : null)
                .eventStatusName(event.getEventStatus() != null ? event.getEventStatus().getEventStatusName() : null)
                .registrationStart(event.getRegistrationStart())
                .registrationEnd(event.getRegistrationEnd())
                .eventStartDate(event.getEventStartDate())
                .eventEndDate(event.getEventEndDate())
                .maxTeamSize(event.getMaxTeamSize())
                .minTeamSize(event.getMinTeamSize())
                .createdById(event.getCreatedBy() != null ? event.getCreatedBy().getUserId() : null)
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    public EventResponse toEventResponse(Event event, EventParticipant participant) {
        String participationStatus = participationStatus(participant);
        return EventResponse.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .description(event.getDescription())
                .location(event.getLocation())
                .bannerImageUrl(event.getBannerImageUrl())
                .eventStatus(event.getEventStatus())
                .eventStatusId(event.getEventStatus() != null ? event.getEventStatus().getEventStatusId() : null)
                .eventStatusName(event.getEventStatus() != null ? event.getEventStatus().getEventStatusName() : null)
                .registrationStart(event.getRegistrationStart())
                .registrationEnd(event.getRegistrationEnd())
                .eventStartDate(event.getEventStartDate())
                .eventEndDate(event.getEventEndDate())
                .maxTeamSize(event.getMaxTeamSize())
                .minTeamSize(event.getMinTeamSize())
                .createdById(event.getCreatedBy() != null ? event.getCreatedBy().getUserId() : null)
                .participantStatus(participationStatus)
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    private String participationStatus(EventParticipant participant) {
        if (participant == null || participant.getParticipantStatus() == null) {
            return NOT_REGISTERED;
        }

        return toApiParticipantStatus(participant.getParticipantStatus().getStatusName());
    }

    private String toApiParticipantStatus(String statusName) {
        return statusName;
    }
}
