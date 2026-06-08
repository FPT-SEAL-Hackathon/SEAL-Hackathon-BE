package com.fpt.swp.sealhackathonbe.judging.entity;


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

import com.fpt.swp.sealhackathonbe.criteria.entity.EventCriteria;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.user.entity.User;

@Entity
@Table(name = "judging")
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
    @JoinColumn(name = "JudgeUserid", nullable = false)
    private User judgeUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EventCriterionID", nullable = false)
    private EventCriteria eventCriterion;

    @Column(name = "score_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal scoreValue;

    @Column(name = "comment", columnDefinition = "TEXT")
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
