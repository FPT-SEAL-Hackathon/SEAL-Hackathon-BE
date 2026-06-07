package com.fpt.swp.sealhackathonbe.award.entity;

import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "Awards")
public class Award {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Sửa lại chuẩn JPA cho UUID
    @Column(name = "AwardID", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EventID", nullable = false)
    private Event event; // Đã đổi tên chuẩn

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID")
    private Category category; // Đã đổi tên chuẩn

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TeamID", nullable = false)
    private Teams team; // Đã đổi tên chuẩn

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AwardTierID", nullable = false)
    private AwardTier awardTier; // Đã đổi tên chuẩn

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
    @ColumnDefault("'VND'")
    @Column(name = "PrizeCurrency", length = 3)
    private String prizeCurrency;

    @NotNull
    @Column(name = "AwardedAt", nullable = false)
    private Instant awardedAt; // Xóa @ColumnDefault("getutcdate()"), sẽ xử lý gán ngày giờ ở tầng Service

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AwardedByID", nullable = false)
    private User awardedBy; // Đã đổi tên chuẩn

    @NotNull
    @ColumnDefault("0")
    @Column(name = "IsPublished", nullable = false)
    private Boolean isPublished = false; // Gán giá trị mặc định thẳng trên code Java

    @Column(name = "PublishedAt")
    private Instant publishedAt;
}