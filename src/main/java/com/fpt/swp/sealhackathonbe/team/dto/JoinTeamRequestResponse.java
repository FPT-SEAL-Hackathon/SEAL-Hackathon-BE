package com.fpt.swp.sealhackathonbe.team.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class JoinTeamRequestResponse {
    private UUID requestId;
    private UUID teamId;
    private UUID userId;
    private String requestStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
    private UUID respondedById;
    private String responseNote;

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public UUID getRespondedById() {
        return respondedById;
    }

    public void setRespondedById(UUID respondedById) {
        this.respondedById = respondedById;
    }

    public String getResponseNote() {
        return responseNote;
    }

    public void setResponseNote(String responseNote) {
        this.responseNote = responseNote;
    }
}
