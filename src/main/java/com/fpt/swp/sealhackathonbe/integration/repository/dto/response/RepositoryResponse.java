package com.fpt.swp.sealhackathonbe.integration.repository.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryResponse {
    private UUID integrationId;
    private UUID repositoryId;
    private String connectionStatus;
    private String syncStatus;
    private String externalId;
    private String repositoryName;
    private String repositoryFullName;
    private String repositoryUrl;
    private String description;
    private LocalDateTime connectedAt;
    private LocalDateTime lastSyncAt;
}
