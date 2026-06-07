package com.fpt.swp.sealhackathonbe.team.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Disqualifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
