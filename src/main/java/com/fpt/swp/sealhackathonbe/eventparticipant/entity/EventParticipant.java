package com.fpt.swp.sealhackathonbe.eventparticipant.entity;

import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores a user's approval state for one event without changing the Users table.
 */
@Entity
@Table(
        name = "EventParticipants",
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_EventParticipants_Event_User", columnNames = {"EventID", "UserID"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "EventParticipantID", nullable = false, updatable = false)
    private UUID eventParticipantId;

    @Column(name = "EventID", nullable = false)
    private UUID eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EventID", nullable = false, insertable = false, updatable = false)
    private Event event;

    @Column(name = "UserID", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false, insertable = false, updatable = false)
    private User user;

    @Column(name = "ParticipantStatusID", nullable = false)
    private UUID participantStatusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ParticipantStatusID", nullable = false, insertable = false, updatable = false)
    private ParticipantStatus participantStatus;

    @Column(name = "AppliedAt", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "ApprovedAt")
    private LocalDateTime approvedAt;

    @Column(name = "ApprovedBy")
    private UUID approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ApprovedBy", insertable = false, updatable = false)
    private User approvedByUser;

    @Column(name = "RejectedReason", length = 1000)
    private String rejectedReason;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (appliedAt == null) {
            appliedAt = now;
        }
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
