package com.fpt.swp.sealhackathonbe.round.entity;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Rounds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Round {
    @Id
    @Column(name = "RoundID")
    private UUID roundId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID", nullable = false)
    private Category category;

    @Column(name = "RoundName")
    private String roundName;

    @Column(name = "Description")
    private String description;

    @Column(name = "RoundOrder")
    private Integer roundOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoundStatusID")
    private RoundStatus roundStatus;

    @Column(name = "StartDate")
    private LocalDateTime startDate;

    @Column(name = "EndDate")
    private LocalDateTime endDate;

    @Column(name = "SubmissionDeadline")
    private LocalDateTime submissionDeadline;

    @Column(name = "JudgingDeadline")
    private LocalDateTime judgingDeadline;

    @Column(name = "AdvancementTopN")
    private Integer advancementTopN;

    @Column(name = "IsCalibrationRound")
    @Builder.Default
    private Boolean isCalibrationRound = false;
}
