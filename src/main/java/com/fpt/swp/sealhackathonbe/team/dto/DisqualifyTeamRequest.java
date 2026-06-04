package com.fpt.swp.sealhackathonbe.team.dto;

import jakarta.validation.constraints.NotBlank;

public class DisqualifyTeamRequest {
    @NotBlank(message = "Reason is required")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
