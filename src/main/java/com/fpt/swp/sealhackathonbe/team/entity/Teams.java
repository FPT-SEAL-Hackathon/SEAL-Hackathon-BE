package com.fpt.swp.sealhackathonbe.team.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Teams")
public class Teams {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "TeamID")
  private Integer teamId;

  @Column(name = "EventID", nullable = false)
  private Integer eventId;

  @Column(name = "CategoryID", nullable = false)
  private Integer categoryId;

  @Column(name = "TeamName", nullable = false, length = 300)
  private String teamName;

  @Column(name = "TeamStatusID", nullable = false)
  private Short teamStatusId;

  @Column(name = "LeaderUserID", nullable = false)
  private Integer leaderUserId;

  @Column(name = "CreatedAt", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "UpdatedAt", nullable = false)
  private LocalDateTime updatedAt;

  public Integer getTeamId() {
    return teamId;
  }

  public void setTeamId(Integer teamId) {
    this.teamId = teamId;
  }

  public Integer getEventId() {
    return eventId;
  }

  public void setEventId(Integer eventId) {
    this.eventId = eventId;
  }

  public Integer getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Integer categoryId) {
    this.categoryId = categoryId;
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
}