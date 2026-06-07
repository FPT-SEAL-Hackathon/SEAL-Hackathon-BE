package com.fpt.swp.sealhackathonbe.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class HandleJoinRequest {
    @NotBlank(message = "Action is required")
    @Pattern(
            regexp = "APPROVED|REJECTED",
            message = "Action must be APPROVED or REJECTED"
    )
    private String action;

    @Size(max = 500, message = "Response note must not exceed 500 characters")
    private String responseNote;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResponseNote() {
        return responseNote;
    }

    public void setResponseNote(String responseNote) {
        this.responseNote = responseNote;
    }
}
