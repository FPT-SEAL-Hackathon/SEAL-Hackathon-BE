package com.fpt.swp.sealhackathonbe.team.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Teams")
public class Teams {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "TeamID")
  private UUID teamId;

  @Column(name = "EventID", nullable = false)
  private UUID eventId;

  @Column(name = "CategoryID", nullable = false)
  private UUID categoryId;

  @Column(name = "TeamName", nullable = false, length = 300)
  private String teamName;

  @Column(name = "TeamStatusID", nullable = false)
  private UUID teamStatusId;

  @Column(name = "LeaderUserID", nullable = false)
  private UUID leaderUserId;

  @Column(name = "CreatedAt", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "UpdatedAt", nullable = false)
  private LocalDateTime updatedAt;

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
}
