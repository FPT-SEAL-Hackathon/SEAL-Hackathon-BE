package com.fpt.swp.sealhackathonbe.integration.repository.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class IntegrationOverviewResponse {
    private UUID integrationId;
    private UUID repositoryId;
    private String provider;
    private String repositoryName;
    private String repositoryFullName;
    private String repositoryUrl;
    private String description;
    private LocalDateTime connectedAt;
    private LocalDateTime lastSyncAt;
    private String lastSyncStatus;
}
