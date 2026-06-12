package com.fpt.swp.sealhackathonbe.team.entity;

import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.user.entity.User;
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

  // Entity lưu lịch sử loại team/submission; hiện package team chỉ tạo bản ghi loại team.
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "DisqualificationID")
  private UUID disqualificationId;

  @Column(name = "TeamID")
  private UUID teamId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TeamID", insertable = false, updatable = false)
  private Teams team;

  @Column(name = "SubmissionID")
  private UUID submissionId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "SubmissionID", insertable = false, updatable = false)
  private Submissions submission;

  @Column(name = "Reason", nullable = false, columnDefinition = "NVARCHAR(MAX)")
  private String reason;

  @Column(name = "DisqualifiedByID", nullable = false)
  private UUID disqualifiedById;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "DisqualifiedByID", nullable = false, insertable = false, updatable = false)
  private User disqualifiedBy;

  @Column(name = "DisqualifiedAt", nullable = false)
  private LocalDateTime disqualifiedAt;

  @Column(name = "IsReversed", nullable = false)
  private Boolean reversed;

  // Các trường reverse đang có trong DB nhưng chưa có API xử lý trong package team.
  @Column(name = "ReversedAt")
  private LocalDateTime reversedAt;

  @Column(name = "ReversedByID")
  private UUID reversedById;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ReversedByID", insertable = false, updatable = false)
  private User reversedBy;

  @Column(name = "ReversalReason", columnDefinition = "NVARCHAR(MAX)")
  private String reversalReason;
}
