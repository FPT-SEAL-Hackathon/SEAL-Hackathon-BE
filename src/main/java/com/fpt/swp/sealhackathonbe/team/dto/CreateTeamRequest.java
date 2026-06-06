package com.fpt.swp.sealhackathonbe.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateTeamRequest {
    @NotNull(message = "Event ID is required")
    private UUID eventId;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotBlank(message = "Team name is required")
    @Size(max = 300, message = "Team name must not exceed: 300 characters")
    private String teamName;
}
