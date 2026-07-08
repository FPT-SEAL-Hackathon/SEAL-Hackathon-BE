package com.fpt.swp.sealhackathonbe.team.event;

import java.util.UUID;

public record TeamJoinApprovedEvent(
        UUID recipientUserId,
        UUID leaderUserId,
        UUID eventId,
        String teamName
) {
}
