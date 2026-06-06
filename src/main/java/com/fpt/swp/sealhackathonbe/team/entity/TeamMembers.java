package com.fpt.swp.sealhackathonbe.team.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TeamMembers")
public class TeamMembers {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "TeamMemberID")
  private UUID teamMemberId;

  @Column(name = "TeamID", nullable = false)
  private UUID teamId;

  @Column(name = "UserID", nullable = false)
  private UUID userId;

  @Column(name = "JoinedAt", nullable = false)
  private LocalDateTime joinedAt;

  @Column(name = "LeftAt")
  private LocalDateTime leftAt;

  @Column(name = "IsActive", nullable = false)
  private Boolean active;

  public UUID getTeamMemberId() {
    return teamMemberId;
  }

  public void setTeamMemberId(UUID teamMemberId) {
    this.teamMemberId = teamMemberId;
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
