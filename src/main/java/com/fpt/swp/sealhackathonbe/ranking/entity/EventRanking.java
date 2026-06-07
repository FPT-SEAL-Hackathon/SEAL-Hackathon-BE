package com.fpt.swp.sealhackathonbe.ranking.entity;

import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// Mock imports

@Entity
@Table(name = "EventRankings", uniqueConstraints = {
    @UniqueConstraint(name = "UQ_EventRankings", columnNames = {"EventID", "CategoryID", "TeamID"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "EventRankingID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EventID", nullable = false)
    private User event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID", nullable = false)
    private User category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeamID", nullable = false)
    private Teams team;

    @Column(name = "FinalScore", nullable = false, precision = 10, scale = 4)
    private BigDecimal finalScore;

    @Column(name = "RankPosition", nullable = false)
    private Integer rankPosition;

    @CreationTimestamp
    @Column(name = "ComputedAt", nullable = false, updatable = false)
    private LocalDateTime computedAt;
}
