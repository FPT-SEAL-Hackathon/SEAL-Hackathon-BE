package com.fpt.swp.sealhackathonbe.team.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Teams {

  // Entity ánh xạ bảng Teams; service cập nhật trạng thái và leaderUserId, không chứa logic nghiệp vụ.
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "TeamID")
  private UUID teamId;

  @Column(name = "EventID", nullable = false)
  private UUID eventId;

  @Column(name = "CategoryID", nullable = false)
  private UUID categoryId;

  @Column(name = "TeamName", nullable = false, length = 300)
  private String teamName;

  @Column(name = "TeamStatusID", nullable = false)
  private UUID teamStatusId;

  @Column(name = "LeaderUserID", nullable = false)
  private UUID leaderUserId;

  @Column(name = "CreatedAt", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "UpdatedAt", nullable = false)
  private LocalDateTime updatedAt;
}
