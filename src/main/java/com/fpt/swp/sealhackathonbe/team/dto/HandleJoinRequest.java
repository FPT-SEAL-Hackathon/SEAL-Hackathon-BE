package com.fpt.swp.sealhackathonbe.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HandleJoinRequest {
    @NotBlank(message = "Action is required")
    @Pattern(
            regexp = "APPROVED|REJECTED",
            message = "Action must be APPROVED or REJECTED"
    )
    private String action;

    @Size(max = 500, message = "Response note must not exceed 500 characters")
    private String responseNote;
}
