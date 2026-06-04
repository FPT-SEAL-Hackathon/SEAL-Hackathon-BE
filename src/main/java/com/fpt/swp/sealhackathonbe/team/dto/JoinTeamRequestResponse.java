package com.fpt.swp.sealhackathonbe.team.dto;

import java.time.LocalDateTime;

public class JoinTeamRequestResponse {
    private Integer requestId;
    private Integer teamId;
    private Integer userId;
    private String requestStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
    private Integer respondedById;
    private String responseNote;

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
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

    public Integer getRespondedById() {
        return respondedById;
    }

    public void setRespondedById(Integer respondedById) {
        this.respondedById = respondedById;
    }

    public String getResponseNote() {
        return responseNote;
    }

    public void setResponseNote(String responseNote) {
        this.responseNote = responseNote;
    }
}
