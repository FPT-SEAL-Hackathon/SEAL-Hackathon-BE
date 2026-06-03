package com.fpt.swp.sealhackathonbe.team.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TeamMembers")
public class TeamMembers {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "TeamMemberID")
  private Integer teamMemberId;

  @Column(name = "TeamID", nullable = false)
  private Integer teamId;

  @Column(name = "UserID", nullable = false)
  private Integer userId;

  @Column(name = "JoinedAt", nullable = false)
  private LocalDateTime joinedAt;

  @Column(name = "LeftAt")
  private LocalDateTime leftAt;

  @Column(name = "IsActive", nullable = false)
  private Boolean active;

  public Integer getTeamMemberId() {
    return teamMemberId;
  }

  public void setTeamMemberId(Integer teamMemberId) {
    this.teamMemberId = teamMemberId;
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

  public LocalDateTime getJoinedAt() {
    return joinedAt;
  }

  public void setJoinedAt(LocalDateTime joinedAt) {
    this.joinedAt = joinedAt;
  }

  public LocalDateTime getLeftAt() {
    return leftAt;
  }

  public void setLeftAt(LocalDateTime leftAt) {
    this.leftAt = leftAt;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }
}