package com.fpt.swp.sealhackathonbe.team.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Disqualifications")
public class Disqualifications {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "DisqualificationID")
  private Integer disqualificationId;

  @Column(name = "TeamID")
  private Integer teamId;

  @Column(name = "SubmissionID")
  private Integer submissionId;

  @Column(name = "Reason", nullable = false, columnDefinition = "NVARCHAR(MAX)")
  private String reason;

  @Column(name = "DisqualifiedByID", nullable = false)
  private Integer disqualifiedById;

  @Column(name = "DisqualifiedAt", nullable = false)
  private LocalDateTime disqualifiedAt;

  @Column(name = "IsReversed", nullable = false)
  private Boolean reversed;

  @Column(name = "ReversedAt")
  private LocalDateTime reversedAt;

  @Column(name = "ReversedByID")
  private Integer reversedById;

  @Column(name = "ReversalReason", columnDefinition = "NVARCHAR(MAX)")
  private String reversalReason;

  public Integer getDisqualificationId() {
    return disqualificationId;
  }

  public void setDisqualificationId(Integer disqualificationId) {
    this.disqualificationId = disqualificationId;
  }

  public Integer getTeamId() {
    return teamId;
  }

  public void setTeamId(Integer teamId) {
    this.teamId = teamId;
  }

  public Integer getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Integer submissionId) {
    this.submissionId = submissionId;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public Integer getDisqualifiedById() {
    return disqualifiedById;
  }

  public void setDisqualifiedById(Integer disqualifiedById) {
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

  public Integer getReversedById() {
    return reversedById;
  }

  public void setReversedById(Integer reversedById) {
    this.reversedById = reversedById;
  }

  public String getReversalReason() {
    return reversalReason;
  }

  public void setReversalReason(String reversalReason) {
    this.reversalReason = reversalReason;
  }
}