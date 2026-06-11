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
@Table(name = "TeamJoinRequests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamJoinRequests {

  // Entity lưu đơn xin vào team; requestStatus đi theo các giá trị PENDING/APPROVED/REJECTED trong service.
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "RequestID")
  private UUID requestId;

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

  @Column(name = "RequestStatus", nullable = false, length = 20)
  private String requestStatus;

  @Column(name = "RequestedAt", nullable = false)
  private LocalDateTime requestedAt;

  @Column(name = "RespondedAt")
  private LocalDateTime respondedAt;

  @Column(name = "RespondedByID")
  private UUID respondedById;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "RespondedByID", insertable = false, updatable = false)
  private User respondedBy;

  @Column(name = "ResponseNote", length = 500)
  private String responseNote;
}
