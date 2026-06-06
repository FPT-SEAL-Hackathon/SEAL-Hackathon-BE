package com.fpt.swp.sealhackathonbe.team.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TeamJoinRequests")
public class TeamJoinRequests {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "RequestID")
  private UUID requestId;

  @Column(name = "TeamID", nullable = false)
  private UUID teamId;

  @Column(name = "UserID", nullable = false)
  private UUID userId;

  @Column(name = "RequestStatus", nullable = false, length = 20)
  private String requestStatus;

  @Column(name = "RequestedAt", nullable = false)
  private LocalDateTime requestedAt;

  @Column(name = "RespondedAt")
  private LocalDateTime respondedAt;

  @Column(name = "RespondedByID")
  private UUID respondedById;

  @Column(name = "ResponseNote", length = 500)
  private String responseNote;

  public UUID getRequestId() {
    return requestId;
  }

  public void setRequestId(UUID requestId) {
    this.requestId = requestId;
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

  public UUID getRespondedById() {
    return respondedById;
  }

  public void setRespondedById(UUID respondedById) {
    this.respondedById = respondedById;
  }

  public String getResponseNote() {
    return responseNote;
  }

  public void setResponseNote(String responseNote) {
    this.responseNote = responseNote;
  }
}
