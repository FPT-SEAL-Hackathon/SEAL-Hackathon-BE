package com.fpt.swp.sealhackathonbe.team.entity;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.user.entity.User;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "EventID", nullable = false, insertable = false, updatable = false)
  private Event event;

  @Column(name = "CategoryID", nullable = false)
  private UUID categoryId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "CategoryID", nullable = false, insertable = false, updatable = false)
  private Category category;

  @Column(name = "TeamName", nullable = false, length = 300)
  private String teamName;

  @Column(name = "TeamStatusID", nullable = false)
  private UUID teamStatusId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TeamStatusID", nullable = false, insertable = false, updatable = false)
  private TeamStatus teamStatus;

  @Column(name = "LeaderUserID", nullable = false)
  private UUID leaderUserId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "LeaderUserID", nullable = false, insertable = false, updatable = false)
  private User leaderUser;

  @Column(name = "CreatedAt", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "UpdatedAt", nullable = false)
  private LocalDateTime updatedAt;
}
