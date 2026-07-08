package com.fpt.swp.sealhackathonbe.team.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TransferTeamLeadershipRequest {
    @NotNull(message = "New leader user ID is required")
    private UUID newLeaderUserId;
}
