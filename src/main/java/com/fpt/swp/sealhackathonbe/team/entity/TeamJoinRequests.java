package com.fpt.swp.sealhackathonbe.team.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TeamJoinRequests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
