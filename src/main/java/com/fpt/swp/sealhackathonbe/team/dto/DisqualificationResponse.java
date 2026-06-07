package com.fpt.swp.sealhackathonbe.team.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class DisqualificationResponse {
    private UUID disqualificationId;
    private UUID teamId;
    private String reason;
    private UUID disqualifiedById;
    private LocalDateTime disqualifiedAt;
    private Boolean reversed;

    public UUID getDisqualificationId() {
        return disqualificationId;
    }

    public void setDisqualificationId(UUID disqualificationId) {
        this.disqualificationId = disqualificationId;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public UUID getDisqualifiedById() {
        return disqualifiedById;
    }

    public void setDisqualifiedById(UUID disqualifiedById) {
        this.disqualifiedById = disqualifiedById;
    }

    public LocalDateTime getDisqualifiedAt() {
        return disqualifiedAt;
    }

    public void setDisqualifiedAt(LocalDateTime disqualifiedAt) {
        this.disqualifiedAt = disqualifiedAt;
    }

    public Boolean getReversed() {
        return reversed;
    }

    public void setReversed(Boolean reversed) {
        this.reversed = reversed;
    }
}
