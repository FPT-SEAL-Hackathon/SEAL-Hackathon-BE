package com.fpt.swp.sealhackathonbe.team.entity;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TeamWithdrawalRequests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamWithdrawalRequest {
    // Ban ghi lich su khi leader rut team khoi event/competition.
    // Hien tai request duoc approve ngay, nhung van giu cac cot responded* de mo rong flow duyet sau nay.

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "RequestID")
    private UUID requestId;

    @Column(name = "TeamID", nullable = false)
    private UUID teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeamID", nullable = false, insertable = false, updatable = false)
    private Teams team;

    @Column(name = "RequestedByID", nullable = false)
    private UUID requestedById;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RequestedByID", nullable = false, insertable = false, updatable = false)
    private User requestedBy;

    @Column(name = "Reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "RequestStatus", nullable = false, length = 20)
    private String requestStatus;

    // Thoi diem leader gui yeu cau; dung de sap xep danh sach admin/member.
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
