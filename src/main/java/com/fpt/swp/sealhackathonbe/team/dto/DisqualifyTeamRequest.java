package com.fpt.swp.sealhackathonbe.team.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DisqualifyTeamRequest {
    @NotBlank(message = "Reason is required")
    private String reason;
}
