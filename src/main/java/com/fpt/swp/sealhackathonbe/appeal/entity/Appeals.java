package com.fpt.swp.sealhackathonbe.appeal.entity;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Appeals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appeals {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "AppealID", updatable = false, nullable = false)
    private UUID appealId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeamID", nullable = false)
    private Teams team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EventID", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID", nullable = false)
    private Category category;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "Reason", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private AppealStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "AppealType", nullable = false)
    private AppealType appealType;

    @Column(name = "ResolutionNote", columnDefinition = "NVARCHAR(MAX)")
    private String resolutionNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ResolvedByUserID")
    private User resolvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedByUserID", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = AppealStatus.PENDING;
        }
    }
}
