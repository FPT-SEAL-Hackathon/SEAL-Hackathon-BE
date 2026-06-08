package com.fpt.swp.sealhackathonbe.team.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TeamMembers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
