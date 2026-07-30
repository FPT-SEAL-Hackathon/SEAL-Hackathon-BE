package com.fpt.swp.sealhackathonbe.team.event;

import java.util.UUID;

// Domain event phat sau khi leader approve join request; listener se gui notification sau commit.
public record TeamJoinApprovedEvent(
        UUID recipientUserId,
        UUID leaderUserId,
        UUID eventId,
        String teamName
) {
}
