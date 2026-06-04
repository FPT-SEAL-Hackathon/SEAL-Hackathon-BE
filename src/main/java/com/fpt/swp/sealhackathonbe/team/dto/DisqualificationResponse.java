package com.fpt.swp.sealhackathonbe.team.dto;

import java.time.LocalDateTime;

public class DisqualificationResponse {
    private Integer disqualificationId;
    private Integer teamId;
    private String reason;
    private Integer disqualifiedById;
    private LocalDateTime disqualifiedAt;
    private Boolean reversed;

    public Integer getDisqualificationId() {
        return disqualificationId;
    }

    public void setDisqualificationId(Integer disqualificationId) {
        this.disqualificationId = disqualificationId;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getDisqualifiedById() {
        return disqualifiedById;
    }

    public void setDisqualifiedById(Integer disqualifiedById) {
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
