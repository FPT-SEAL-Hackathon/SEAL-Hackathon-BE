package com.fpt.swp.sealhackathonbe.team.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Disqualifications")
public class Disqualifications {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "DisqualificationID")
  private UUID disqualificationId;

  @Column(name = "TeamID")
  private UUID teamId;

  @Column(name = "SubmissionID")
  private UUID submissionId;

  @Column(name = "Reason", nullable = false, columnDefinition = "NVARCHAR(MAX)")
  private String reason;

  @Column(name = "DisqualifiedByID", nullable = false)
  private UUID disqualifiedById;

  @Column(name = "DisqualifiedAt", nullable = false)
  private LocalDateTime disqualifiedAt;

  @Column(name = "IsReversed", nullable = false)
  private Boolean reversed;

  @Column(name = "ReversedAt")
  private LocalDateTime reversedAt;

  @Column(name = "ReversedByID")
  private UUID reversedById;

  @Column(name = "ReversalReason", columnDefinition = "NVARCHAR(MAX)")
  private String reversalReason;

  public UUID getDisqualificationId() {
    return disqualificationId;
  }

  public void setDisqualificationId(UUID disqualificationId) {
    this.disqualificationId = disqualificationId;
  }

  public UUID getTeamId() {
    return teamId;
  }

  public void setTeamId(UUID teamId) {
    this.teamId = teamId;
  }

  public UUID getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(UUID submissionId) {
    this.submissionId = submissionId;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public UUID getDisqualifiedById() {
    return disqualifiedById;
  }

  public void setDisqualifiedById(UUID disqualifiedById) {
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

  public LocalDateTime getReversedAt() {
    return reversedAt;
  }

  public void setReversedAt(LocalDateTime reversedAt) {
    this.reversedAt = reversedAt;
  }

  public UUID getReversedById() {
    return reversedById;
  }

  public void setReversedById(UUID reversedById) {
    this.reversedById = reversedById;
  }

  public String getReversalReason() {
    return reversalReason;
  }

  public void setReversalReason(String reversalReason) {
    this.reversalReason = reversalReason;
  }
}
