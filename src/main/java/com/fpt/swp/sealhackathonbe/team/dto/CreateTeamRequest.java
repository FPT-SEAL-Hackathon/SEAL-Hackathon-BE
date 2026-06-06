package com.fpt.swp.sealhackathonbe.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class CreateTeamRequest {
    @NotNull(message = "Event ID is required")
    private UUID eventId;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotBlank(message = "Team name is required")
    @Size(max = 300, message = "Team name must not exceed: 300 characters")
    private String teamName;

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }


}
