package com.fpt.swp.sealhackathonbe.team.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TeamResponse {

    private String teamID;
    private Integer eventID;
    private Integer categoryID;
    private String teamName;
    private Short teamStatusId;
    private Integer leaderUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TeamMemberResponse> members;

    public String getTeamID() {
        return teamID;
    }

    public void setTeamID(String teamID) {
        this.teamID = teamID;
    }

    public Integer getEventID() {
        return eventID;
    }

    public void setEventID(Integer eventID) {
        this.eventID = eventID;
    }

    public Integer getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(Integer categoryID) {
        this.categoryID = categoryID;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Short getTeamStatusId() {
        return teamStatusId;
    }

    public void setTeamStatusId(Short teamStatusId) {
        this.teamStatusId = teamStatusId;
    }

    public Integer getLeaderUserId() {
        return leaderUserId;
    }

    public void setLeaderUserId(Integer leaderUserId) {
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
