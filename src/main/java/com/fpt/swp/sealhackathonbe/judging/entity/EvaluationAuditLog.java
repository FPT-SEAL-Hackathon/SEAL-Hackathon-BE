package com.fpt.swp.sealhackathonbe.judging.entity;

import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "EvaluationAuditLogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "EvaluationAuditLogID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EventID", nullable = false)
    private Event event;

    @Column(name = "ActionType", nullable = false)
    private String actionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ActorUserID", nullable = false)
    private User actor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JudgingID", nullable = false)
    private Judging score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeamID", nullable = false)
    private Teams team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SubmissionID", nullable = false)
    private Submissions submission;

    @Column(name = "OldValue", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "NewValue", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "Reason", columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;
}
