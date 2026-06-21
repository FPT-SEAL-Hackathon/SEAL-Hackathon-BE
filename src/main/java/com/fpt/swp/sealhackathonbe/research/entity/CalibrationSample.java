package com.fpt.swp.sealhackathonbe.research.entity;

import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "CalibrationSamples")
public class CalibrationSample {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "SampleID", nullable = false, updatable = false)
    private UUID sampleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RoundID", nullable = false)
    private Round round;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SubmissionID", nullable = false)
    private Submissions submission;

    @Nationalized
    @Lob
    @Column(name = "ReferenceScoreJSON")
    private String referenceScoreJson;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AddedByID", nullable = false)
    private User addedBy;

    @CreationTimestamp
    @Column(name = "AddedAt", nullable = false, updatable = false)
    private LocalDateTime addedAt;
}
