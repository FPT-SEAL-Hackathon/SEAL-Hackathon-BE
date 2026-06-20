package com.fpt.swp.sealhackathonbe.criteria.entity;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CriterionTemplate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriterionTemplate {
    @Id
    @Column(name = "TemplateID")
    private UUID templateId;

    @Column(name = "CriterionName")
    private String criterionName;

    @Column(name = "Description")
    private String description;

    @Column(name = "DefaultWeight")
    private BigDecimal defaultWeight;

    @Column(name = "MaxScore")
    private BigDecimal maxScore;

    @Column(name = "IsActive")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedByID", nullable = false)
    private User createdBy;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

}
