package com.fpt.swp.sealhackathonbe.event.mapper;

import com.fpt.swp.sealhackathonbe.event.dto.response.EventResponse;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public EventResponse toEventResponse(Event event) {
        return EventResponse.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .description(event.getDescription())
                .location(event.getLocation())
                .bannerImageUrl(event.getBannerImageUrl())
                .eventStatus(event.getEventStatus())
                .registrationStart(event.getRegistrationStart())
                .registrationEnd(event.getRegistrationEnd())
                .eventStartDate(event.getEventStartDate())
                .eventEndDate(event.getEventEndDate())
                .maxTeamSize(event.getMaxTeamSize())
                .minTeamSize(event.getMinTeamSize())
                .createdById(event.getCreatedBy().getUserId())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
