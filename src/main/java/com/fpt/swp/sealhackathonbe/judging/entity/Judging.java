package com.fpt.swp.sealhackathonbe.judging.entity;


import com.fpt.swp.sealhackathonbe.round.entity.RoundCriteria;
import com.fpt.swp.sealhackathonbe.round.entity.RoundJudge;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;

@Entity
@Table(name = "Judging")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Judging {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "JudgingID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SubmissionID", nullable = false)
    private Submissions submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoundJudgeID", nullable = false)
    private RoundJudge roundJudge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoundCriterionID", nullable = false)
    private RoundCriteria roundCriterion;

    @Column(name = "ScoreValue", nullable = false, precision = 10, scale = 2)
    private BigDecimal scoreValue;

    @Column(name = "Comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "IsCalibration")
    private Boolean isCalibration = false;

    @CreationTimestamp
    @Column(name = "JudgedAt", updatable = false)
    private LocalDateTime scoredAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
