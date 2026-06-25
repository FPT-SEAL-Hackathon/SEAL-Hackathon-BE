package com.fpt.swp.sealhackathonbe.ranking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;

@Entity
@Table(name = "RoundRankings", uniqueConstraints = {
    @UniqueConstraint(name = "UQ_RoundRankings", columnNames = {"RoundID", "CategoryID", "TeamID"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoundRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "RankingID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoundID", nullable = false)
    private Round round;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeamID", nullable = false)
    private Teams team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SubmissionID", nullable = false)
    private Submissions submission;

    @Column(name = "TotalScore", nullable = false, precision = 10, scale = 4)
    private BigDecimal totalScore;

    @Column(name = "AverageScore", nullable = false, precision = 10, scale = 4)
    private BigDecimal averageScore;

    @Column(name = "RankPosition", nullable = false)
    private Integer rankPosition;

    @Column(name = "IsAdvanced", nullable = false)
    private Boolean isAdvanced = false;

    @CreationTimestamp
    @Column(name = "ComputedAt", nullable = false, updatable = false)
    private LocalDateTime computedAt;

    @Column(name = "IsPublished", nullable = false)
    private Boolean isPublished = false;
}
