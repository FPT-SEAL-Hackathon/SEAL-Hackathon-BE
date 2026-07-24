package com.fpt.swp.sealhackathonbe.integration.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Repositories", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_Integration_ExternalRepository", columnNames = {"IntegrationID", "ExternalRepositoryID"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "RepositoryID")
    private UUID repositoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IntegrationID", nullable = false)
    private RepositoryIntegration integration;

    @Column(name = "ExternalRepositoryID", nullable = false)
    private String externalId;

    @Column(name = "RepositoryName", nullable = false)
    private String repositoryName;

    @Column(name = "RepositoryFullName", nullable = false)
    private String repositoryFullName;

    @Column(name = "RepositoryUrl")
    private String repositoryUrl;

    @Column(name = "Description")
    private String description;

    @Column(name = "SyncStatus", nullable = false)
    private String syncStatus; // 'IDLE', 'RUNNING', 'SUCCESS', 'PARTIAL_SUCCESS', 'FAILED'

    @Column(name = "ConnectedAt", nullable = false, updatable = false)
    private LocalDateTime connectedAt;

    @Column(name = "LastSyncAt")
    private LocalDateTime lastSyncAt;

    @PrePersist
    protected void onCreate() {
        connectedAt = LocalDateTime.now();
        if (syncStatus == null) {
            syncStatus = "IDLE";
        }
    }
}
