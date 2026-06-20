package com.fpt.swp.sealhackathonbe.round.entity;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "RoundJudges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundJudge {
    @Id
    @Column(name = "RoundJudgeID")
    private UUID roundJudgeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoundID", nullable = false)
    private Round round;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID")
    private User judge;

    @Column(name = "AssignedAt")
    private LocalDateTime assignedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AssignedByID")
    private User assignedBy;
}
