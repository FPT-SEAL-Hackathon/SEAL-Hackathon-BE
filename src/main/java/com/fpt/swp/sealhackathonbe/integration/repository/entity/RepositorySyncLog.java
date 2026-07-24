package com.fpt.swp.sealhackathonbe.integration.repository.entity;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "RepositorySyncLogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositorySyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "SyncLogID")
    private UUID syncLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RepositoryID", nullable = false)
    private RepositoryEntity repository;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TriggeredByUserID", nullable = false)
    private User triggeredBy;

    @Column(name = "SyncType", nullable = false)
    private String syncType; // 'INITIAL', 'MANUAL'

    @Column(name = "Status", nullable = false)
    private String status; // 'RUNNING', 'SUCCESS', 'PARTIAL_SUCCESS', 'FAILED'

    @Column(name = "ItemsFetched", nullable = false)
    private Integer itemsFetched;

    @Column(name = "ItemsCreated", nullable = false)
    private Integer itemsCreated;

    @Column(name = "ItemsUpdated", nullable = false)
    private Integer itemsUpdated;

    @Column(name = "ItemsFailed", nullable = false)
    private Integer itemsFailed;

    @Column(name = "HasMore", nullable = false)
    private Boolean hasMore;

    @Column(name = "ErrorCode")
    private String errorCode;

    @Column(name = "ErrorMessage", columnDefinition = "NVARCHAR(MAX)")
    private String errorMessage;

    @Column(name = "StartedAt", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "CompletedAt")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        if (itemsFetched == null) itemsFetched = 0;
        if (itemsCreated == null) itemsCreated = 0;
        if (itemsUpdated == null) itemsUpdated = 0;
        if (itemsFailed == null) itemsFailed = 0;
        if (hasMore == null) hasMore = false;
    }
}
