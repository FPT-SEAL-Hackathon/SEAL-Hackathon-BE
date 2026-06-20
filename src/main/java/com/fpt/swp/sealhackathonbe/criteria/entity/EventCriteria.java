package com.fpt.swp.sealhackathonbe.criteria.entity;

import com.fpt.swp.sealhackathonbe.event.entity.Event;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "EventCriteria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCriteria {

    @Id
    @Column(name = "EventCriterionID")
    private UUID eventCriterionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EventID")
    private Event event;

    @Column(name = "TemplateID")
    private UUID templateId;

    @Column(name = "CriterionName")
    private String criterionName;

    @Column(name = "Description")
    private String description;

    @Column(name = "Weight")
    private BigDecimal weight;

    @Column(name = "MaxScore")
    private BigDecimal maxScore;

    @Column(name = "SortOrder")
    private Integer sortOrder;

    @Column(name = "IsActive")
    private Boolean isActive;
}
