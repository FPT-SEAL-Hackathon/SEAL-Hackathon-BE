package com.fpt.swp.sealhackathonbe.team.entity;

import com.fpt.swp.sealhackathonbe.user.entity.User;
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

  // Entity ánh xạ quan hệ user-team; khi rời team chỉ set IsActive=false và ghi LeftAt.
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "TeamMemberID")
  private UUID teamMemberId;

  @Column(name = "TeamID", nullable = false)
  private UUID teamId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TeamID", nullable = false, insertable = false, updatable = false)
  private Teams team;

  @Column(name = "UserID", nullable = false)
  private UUID userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "UserID", nullable = false, insertable = false, updatable = false)
  private User user;

  @Column(name = "JoinedAt", nullable = false)
  private LocalDateTime joinedAt;

  @Column(name = "LeftAt")
  private LocalDateTime leftAt;

  @Column(name = "IsActive", nullable = false)
  private Boolean active;
}
