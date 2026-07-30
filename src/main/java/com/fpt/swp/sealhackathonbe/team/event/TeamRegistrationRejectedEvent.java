package com.fpt.swp.sealhackathonbe.team.event;

import java.util.List;
import java.util.UUID;

// Domain event khi organizer reject team registration; gui broadcast cho toan bo member lien quan.
public record TeamRegistrationRejectedEvent(
        List<UUID> recipientUserIds,
        UUID organizerUserId,
        UUID eventId,
        String teamName,
        String note
) {
}
