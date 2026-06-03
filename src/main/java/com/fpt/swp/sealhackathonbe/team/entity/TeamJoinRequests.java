package com.fpt.swp.sealhackathonbe.team.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TeamJoinRequests")
public class TeamJoinRequests {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "RequestID")
  private Integer requestId;

  @Column(name = "TeamID", nullable = false)
  private Integer teamId;

  @Column(name = "UserID", nullable = false)
  private Integer userId;

  @Column(name = "RequestStatus", nullable = false, length = 20)
  private String requestStatus;

  @Column(name = "RequestedAt", nullable = false)
  private LocalDateTime requestedAt;

  @Column(name = "RespondedAt")
  private LocalDateTime respondedAt;

  @Column(name = "RespondedByID")
  private Integer respondedById;

  @Column(name = "ResponseNote", length = 500)
  private String responseNote;

  public Integer getRequestId() {
    return requestId;
  }

  public void setRequestId(Integer requestId) {
    this.requestId = requestId;
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

  public Integer getRespondedById() {
    return respondedById;
  }

  public void setRespondedById(Integer respondedById) {
    this.respondedById = respondedById;
  }

  public String getResponseNote() {
    return responseNote;
  }

  public void setResponseNote(String responseNote) {
    this.responseNote = responseNote;
  }
}