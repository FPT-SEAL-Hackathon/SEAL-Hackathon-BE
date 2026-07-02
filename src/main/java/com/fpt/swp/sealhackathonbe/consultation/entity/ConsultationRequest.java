package com.fpt.swp.sealhackathonbe.consultation.entity;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ConsultationRequests")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConsultationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "RequestID")
    private UUID requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EventID", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryID", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeamID", nullable = false)
    private Teams team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MentorUserID", nullable = false)
    private User mentor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedByUserID", nullable = false)
    private User createdBy;

    @Column(name = "Title", nullable = false, length = 150)
    private String title;

    @Column(name = "Description", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "Priority", nullable = false, length = 20)
    private ConsultationPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private ConsultationStatus status;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "ClosedAt")
    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
