package com.fpt.swp.sealhackathonbe.eventparticipant.service;

import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantBulkStatusUpdateRequest;
import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantResponse;
import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface EventParticipantService {
    EventParticipantResponse register(UUID eventId, UUID currentUserId);

    EventParticipantResponse getOwnStatus(UUID eventId, UUID currentUserId);

    List<EventParticipantResponse> getOwnParticipations(UUID currentUserId);

    Page<EventParticipantResponse> search(
            UUID eventId,
            UUID categoryId,
            String status,
            String keyword,
            String university,
            Pageable pageable
    );

    EventParticipantResponse updateStatus(UUID participantId, EventParticipantStatusUpdateRequest request, UUID organizerUserId);

    List<EventParticipantResponse> updateStatuses(EventParticipantBulkStatusUpdateRequest request, UUID organizerUserId);

    void assertActiveParticipant(UUID eventId, UUID userId);
}
