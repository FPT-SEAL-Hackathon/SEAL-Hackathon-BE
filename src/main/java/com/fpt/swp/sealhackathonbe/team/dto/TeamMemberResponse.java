package com.fpt.swp.sealhackathonbe.team.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class TeamMemberResponse {
    private UUID teamMemberId;
    private UUID userId;
    private LocalDateTime joinedAt;
    private Boolean active;

    public UUID getTeamMemberId() {
        return teamMemberId;
    }

    public void setTeamMemberId(UUID teamMemberId) {
        this.teamMemberId = teamMemberId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
