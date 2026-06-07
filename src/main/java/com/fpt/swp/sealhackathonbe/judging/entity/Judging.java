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

import com.fpt.swp.sealhackathonbe.submission.entity.Submission;
// Mocking other entity imports to avoid compiler errors if they are not correctly mapped or if the user requested missing entity errors are fine.
// In reality, these should be imported from their respective packages.
// Since we are instructed to simulate/mock missing entities if needed and missing entity is fine,
// we will just refer to them by their expected package structure, relying on the compiler to fail if missing.
import com.fpt.swp.sealhackathonbe.core.entity.User; // Assuming User is in core
import com.fpt.swp.sealhackathonbe.event.entity.EventCriterion; // Assuming EventCriterion is in event

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
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JudgeUserid", nullable = false)
    private User judgeUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EventCriterionID", nullable = false)
    private EventCriterion eventCriterion;

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
