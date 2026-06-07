package com.fpt.swp.sealhackathonbe.ranking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

// Mock imports
import com.fpt.swp.sealhackathonbe.team.entity.Team;
import com.fpt.swp.sealhackathonbe.submission.entity.Submission;
import com.fpt.swp.sealhackathonbe.core.entity.User;

@Entity
@Table(name = "Disqualifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Disqualification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "DisqualificationID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeamID")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SubmissionID")
    private Submission submission;

    @Column(name = "Reason", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DisqualifiedByID", nullable = false)
    private User disqualifiedBy;

    @CreationTimestamp
    @Column(name = "DisqualifiedAt", nullable = false, updatable = false)
    private LocalDateTime disqualifiedAt;

    @Column(name = "IsReversed", nullable = false)
    private Boolean isReversed = false;

    @Column(name = "ReversedAt")
    private LocalDateTime reversedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReversedByID")
    private User reversedBy;

    @Column(name = "ReversalReason", columnDefinition = "NVARCHAR(MAX)")
    private String reversalReason;
}
