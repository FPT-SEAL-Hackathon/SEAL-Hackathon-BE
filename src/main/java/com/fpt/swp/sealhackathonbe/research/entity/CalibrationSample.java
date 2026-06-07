package com.fpt.swp.sealhackathonbe.research.entity;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "CalibrationSamples")
public class CalibrationSample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SampleID", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RoundID", nullable = false)
    private Round roundID;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SubmissionID", nullable = false)
    private Submission submissionID;

    @Nationalized
    @Lob
    @Column(name = "ReferenceScoreJSON")
    private String referenceScoreJSON;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AddedByID", nullable = false)
    private User addedByID;

    @NotNull
    @ColumnDefault("getutcdate()")
    @Column(name = "AddedAt", nullable = false)
    private Instant addedAt;


}