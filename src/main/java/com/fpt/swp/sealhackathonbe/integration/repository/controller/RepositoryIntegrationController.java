package com.fpt.swp.sealhackathonbe.integration.repository.controller;

import com.fpt.swp.sealhackathonbe.integration.repository.dto.request.ConnectIntegrationRequest;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.response.RepositoryResponse;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.response.RepositorySyncResponse;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryEntity;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryIssue;
import com.fpt.swp.sealhackathonbe.integration.repository.service.RepositoryIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/events/{eventId}/integrations/repository")
@RequiredArgsConstructor
public class RepositoryIntegrationController {

    private final RepositoryIntegrationService integrationService;

    @PostMapping("/test")
    public ResponseEntity<Void> testConnection(@PathVariable UUID eventId,
                                               @Valid @RequestBody ConnectIntegrationRequest request) {
        integrationService.testConnection(eventId, request.getRepositoryUrl(), request.getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/connect")
    public ResponseEntity<RepositorySyncResponse> connectIntegration(@PathVariable UUID eventId,
                                                                  @Valid @RequestBody ConnectIntegrationRequest request) {
        RepositorySyncResponse response = integrationService.connectIntegration(eventId, request.getRepositoryUrl(), request.getToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RepositoryResponse>> getRepositories(@PathVariable UUID eventId) {
        List<RepositoryEntity> repositories = integrationService.getRepositories(eventId);
        List<RepositoryResponse> responses = repositories.stream().map(repo -> RepositoryResponse.builder()
                .integrationId(repo.getIntegration().getIntegrationId())
                .repositoryId(repo.getRepositoryId())
                .connectionStatus(repo.getIntegration().getConnectionStatus())
                .syncStatus(repo.getSyncStatus())
                .externalId(repo.getExternalId())
                .repositoryName(repo.getRepositoryName())
                .repositoryFullName(repo.getRepositoryFullName())
                .repositoryUrl(repo.getRepositoryUrl())
                .description(repo.getDescription())
                .connectedAt(repo.getConnectedAt())
                .lastSyncAt(repo.getLastSyncAt())
                .build()).collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{repositoryId}/sync")
    public ResponseEntity<RepositorySyncResponse> syncRepository(@PathVariable UUID eventId,
                                                               @PathVariable UUID repositoryId) {
        RepositorySyncResponse response = integrationService.triggerManualSync(repositoryId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{integrationId}")
    public ResponseEntity<Void> disconnectIntegration(@PathVariable UUID eventId,
                                                      @PathVariable UUID integrationId) {
        integrationService.disconnectIntegration(integrationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{repositoryId}/issues")
    public ResponseEntity<Page<RepositoryIssue>> getIssues(
            @PathVariable UUID eventId,
            @PathVariable UUID repositoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size);
        return ResponseEntity.ok(integrationService.getIssues(eventId, repositoryId, pageRequest));
    }
}
