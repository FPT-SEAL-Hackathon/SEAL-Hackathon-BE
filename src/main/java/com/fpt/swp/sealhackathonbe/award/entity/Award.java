package com.fpt.swp.sealhackathonbe.award.entity;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Nationalized;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "Awards")
public class Award {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Standard JPA UUID generation
    @Column(name = "AwardID", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EventID", nullable = false)
    private Event event; // Standardized field name

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID")
    private Category category; // Standardized field name

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TeamID", nullable = false)
    private Teams team; // Standardized field name

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AwardTierID", nullable = false)
    private AwardTier awardTier; // Standardized field name

    @Size(max = 300)
    @NotNull
    @Nationalized
    @Column(name = "AwardTitle", nullable = false, length = 300)
    private String awardTitle;

    @Nationalized
    @JdbcTypeCode(SqlTypes.LONGNVARCHAR)
    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "PrizeValue", precision = 12, scale = 2)
    private BigDecimal prizeValue;

    @Size(max = 3)
    @Nationalized
    @ColumnDefault("'VND'")
    @Column(name = "PrizeCurrency", length = 3)
    private String prizeCurrency;

    @NotNull
    @Column(name = "AwardedAt", nullable = false)
    private Instant awardedAt; // Assigned in the service layer

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AwardedByID", nullable = false)
    private User awardedBy; // Standardized field name

    @NotNull
    @ColumnDefault("0")
    @Column(name = "IsPublished", nullable = false)
    private Boolean isPublished = false; // Default value assigned in Java code

    @Column(name = "PublishedAt")
    private Instant publishedAt;
}
