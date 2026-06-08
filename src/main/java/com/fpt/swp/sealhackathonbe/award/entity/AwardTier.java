package com.fpt.swp.sealhackathonbe.award.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "AwardTier")
public class AwardTier {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Đã sửa lỗi Sequence
    @Column(name = "TierID", nullable = false)
    private UUID id;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "TierName", nullable = false, length = 50)
    private String tierName;
}