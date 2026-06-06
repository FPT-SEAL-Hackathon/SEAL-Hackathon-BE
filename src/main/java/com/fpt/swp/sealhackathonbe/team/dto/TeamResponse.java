package com.fpt.swp.sealhackathonbe.team.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TeamResponse {

    private UUID teamId;
    private UUID eventId;
    private UUID categoryId;
    private String teamName;
    private UUID teamStatusId;
    private UUID leaderUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TeamMemberResponse> members;

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

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

    public UUID getTeamStatusId() {
        return teamStatusId;
    }

    public void setTeamStatusId(UUID teamStatusId) {
        this.teamStatusId = teamStatusId;
    }

    public UUID getLeaderUserId() {
        return leaderUserId;
    }

    public void setLeaderUserId(UUID leaderUserId) {
        this.leaderUserId = leaderUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TeamMemberResponse> getMembers() {
        return members;
    }

    public void setMembers(List<TeamMemberResponse> members) {
        this.members = members;
    }
}
