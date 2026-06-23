package com.fpt.swp.sealhackathonbe.team.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EligibilityDecisionRequest {
    @NotNull(message = "Approved decision is required")
    private Boolean approved;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}
