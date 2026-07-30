package com.fpt.swp.sealhackathonbe.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTeamWithdrawalRequest {
    // Ly do leader cung cap khi rut team; duoc luu vao withdrawal request va audit log.
    @NotBlank(message = "Withdrawal reason is required")
    @Size(max = 1000, message = "Withdrawal reason must not exceed 1000 characters")
    private String reason;
}
