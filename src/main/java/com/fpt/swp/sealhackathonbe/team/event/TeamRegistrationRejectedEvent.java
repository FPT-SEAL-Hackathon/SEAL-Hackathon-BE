package com.fpt.swp.sealhackathonbe.team.event;

import java.util.List;
import java.util.UUID;

public record TeamRegistrationRejectedEvent(
        List<UUID> recipientUserIds,
        UUID organizerUserId,
        UUID eventId,
        String teamName,
        String note
) {
}
