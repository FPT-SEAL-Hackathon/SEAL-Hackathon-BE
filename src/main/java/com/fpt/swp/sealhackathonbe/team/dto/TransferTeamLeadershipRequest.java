package com.fpt.swp.sealhackathonbe.team.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TransferTeamLeadershipRequest {
    // User nhan quyen phai la active member cua cung team; service se validate.
    @NotNull(message = "New leader user ID is required")
    private UUID newLeaderUserId;
}
