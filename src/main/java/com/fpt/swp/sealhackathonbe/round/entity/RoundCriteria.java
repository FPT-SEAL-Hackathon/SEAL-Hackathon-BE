package com.fpt.swp.sealhackathonbe.round.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "RoundCriteria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundCriteria {
    @Id
    @Column(name = "RoundCriterionID")
    private UUID roundCriteriaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoundID")
    private Round roundId;

    @Column(name = "EventCriterionID")
    private UUID eventCriterionId;

    @Column(name = "criterionName")
    private String criterionName;

    @Column(name = "description")
    private String description;

    @Column(name = "Weight")
    private BigDecimal weight;

    @Column(name = "MaxScore")
    private BigDecimal maxScore;

    @Column(name = "SortOrder")
    private Integer sortOrder;

}
