package com.fpt.swp.sealhackathonbe.award.entity;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "AwardPatterns",
        uniqueConstraints = @UniqueConstraint(
                name = "UQ_AwardPatterns_Category_Rank",
                columnNames = {"CategoryID", "RankPosition"}
        )
)
public class AwardPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "PatternID", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EventID", nullable = false)
    private Event event;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CategoryID", nullable = false)
    private Category category;

    @NotNull
    @Column(name = "RankPosition", nullable = false)
    private Integer rankPosition;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AwardTierID", nullable = false)
    private AwardTier awardTier;

    @Size(max = 300)
    @NotNull
    @Nationalized
    @Column(name = "AwardTitle", nullable = false, length = 300)
    private String awardTitle;

    @Nationalized
    @Lob
    @Column(name = "Description")
    private String description;

    @Column(name = "PrizeValue", precision = 12, scale = 2)
    private BigDecimal prizeValue;

    @Size(max = 3)
    @Nationalized
    @ColumnDefault("'USD'")
    @Column(name = "PrizeCurrency", length = 3)
    private String prizeCurrency;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;
}
