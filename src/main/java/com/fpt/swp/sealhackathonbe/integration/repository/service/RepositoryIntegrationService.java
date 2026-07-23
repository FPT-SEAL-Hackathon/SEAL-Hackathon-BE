package com.fpt.swp.sealhackathonbe.integration.repository.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fpt.swp.sealhackathonbe.core.exception.RepositoryIntegrationException;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.GitHubRepoDto;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.response.RepositorySyncResponse;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryEntity;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryIntegration;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryIssue;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositorySyncLog;
import com.fpt.swp.sealhackathonbe.integration.repository.repository.RepositoryEntityRepository;
import com.fpt.swp.sealhackathonbe.integration.repository.repository.RepositoryIntegrationRepository;
import com.fpt.swp.sealhackathonbe.integration.repository.repository.RepositoryIssueRepository;
import com.fpt.swp.sealhackathonbe.integration.repository.repository.RepositorySyncLogRepository;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryIntegrationService {

    private final RepositoryIntegrationRepository integrationRepository;
    private final RepositoryEntityRepository repositoryEntityRepository;
    private final RepositoryIssueRepository issueRepository;
    private final RepositorySyncLogRepository syncLogRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final GitHubApiClient gitHubApiClient;
    private final RepositoryTokenEncryptionService encryptionService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.EVENT_REPOSITORY_ACCESS_DENIED, "User not authenticated");
        }
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.EVENT_REPOSITORY_ACCESS_DENIED, "User not found");
        }
        return user;
    }

    private void requireRepositoryManagementPermission(UUID eventId, UUID currentUserId) {
        boolean isCreator = eventRepository.findById(eventId)
                .map(e -> e.getCreatedBy().getUserId().equals(currentUserId))
                .orElse(false);
        if (!isCreator) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.EVENT_REPOSITORY_ACCESS_DENIED, "You do not have repository-management permission for this event");
        }
    }

    public void testConnection(UUID eventId, String url, String token) {
        User currentUser = getCurrentUser();
        requireRepositoryManagementPermission(eventId, currentUser.getUserId());
        gitHubApiClient.fetchRepository(url, token);
    }

    // Connect and run synchronous initial sync
    public RepositorySyncResponse connectIntegration(UUID eventId, String url, String token) {
        User currentUser = getCurrentUser();
        requireRepositoryManagementPermission(eventId, currentUser.getUserId());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.GITHUB_REPOSITORY_NOT_FOUND, "Event not found"));

        GitHubRepoDto repoDto = gitHubApiClient.fetchRepository(url, token);
        String externalId = String.valueOf(repoDto.getId());

        // Check if this specific repository is already connected to this event
        List<RepositoryIntegration> integrations = integrationRepository.findAllByEvent_EventId(eventId);
        RepositoryIntegration integration = null;

        for (RepositoryIntegration existingInt : integrations) {
            Optional<RepositoryEntity> repo = repositoryEntityRepository.findByIntegration_IntegrationIdAndExternalId(existingInt.getIntegrationId(), externalId);
            if (repo.isPresent()) {
                if ("CONNECTED".equals(existingInt.getConnectionStatus())) {
                    throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.REPOSITORY_ALREADY_CONNECTED, "Repository is already connected to this event");
                }
                integration = existingInt;
                break;
            }
        }

        if (integration == null) {
            integration = new RepositoryIntegration();
            integration.setEvent(event);
            integration.setProvider("GITHUB");
            integration.setCreatedBy(currentUser);
        }

        RepositoryTokenEncryptionService.EncryptedData encryptedData = encryptionService.encrypt(token);
        integration.setEncryptedToken(encryptedData.getCipherText());
        integration.setEncryptionIv(encryptedData.getIv());
        integration.setEncryptionFormatVersion(encryptedData.getVersion());
        integration.setConnectionStatus("CONNECTED");

        integration = saveIntegration(integration);
        
        RepositoryEntity repositoryEntity = repositoryEntityRepository.findByIntegration_IntegrationIdAndExternalId(integration.getIntegrationId(), externalId)
                .orElse(new RepositoryEntity());

        repositoryEntity.setIntegration(integration);
        repositoryEntity.setExternalId(externalId);
        repositoryEntity.setRepositoryName(repoDto.getName());
        repositoryEntity.setRepositoryFullName(repoDto.getFullName());
        repositoryEntity.setRepositoryUrl(repoDto.getHtmlUrl());
        repositoryEntity.setDescription(repoDto.getDescription());
        if (repositoryEntity.getSyncStatus() == null) {
            repositoryEntity.setSyncStatus("IDLE");
        }
        
        repositoryEntity = saveRepositoryEntity(repositoryEntity);

        // Run initial synchronization synchronously
        return performSynchronization(repositoryEntity.getRepositoryId(), currentUser.getUserId(), "INITIAL");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RepositoryIntegration saveIntegration(RepositoryIntegration integration) {
        return integrationRepository.save(integration);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RepositoryEntity saveRepositoryEntity(RepositoryEntity repo) {
        return repositoryEntityRepository.save(repo);
    }

    @Transactional(readOnly = true)
    public List<RepositoryEntity> getRepositories(UUID eventId) {
        User currentUser = getCurrentUser();
        requireRepositoryManagementPermission(eventId, currentUser.getUserId());
        return repositoryEntityRepository.findAllByIntegration_Event_EventId(eventId);
    }

    @Transactional(readOnly = true)
    public Page<RepositoryIssue> getIssues(UUID eventId, UUID repositoryId, Pageable pageable) {
        User currentUser = getCurrentUser();
        RepositoryEntity repository = repositoryEntityRepository.findById(repositoryId)
                .orElseThrow(() -> new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.GITHUB_REPOSITORY_NOT_FOUND, "Repository not found"));

        requireRepositoryManagementPermission(repository.getIntegration().getEvent().getEventId(), currentUser.getUserId());
        return issueRepository.findAllByRepository_RepositoryIdOrderByExternalUpdatedAtDesc(repositoryId, pageable);
    }

    public RepositorySyncResponse triggerManualSync(UUID repositoryId) {
        User currentUser = getCurrentUser();
        // Verify repo access implicitly through event authorization
        RepositoryEntity repository = repositoryEntityRepository.findById(repositoryId)
                .orElseThrow(() -> new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.GITHUB_REPOSITORY_NOT_FOUND, "Repository not found"));

        requireRepositoryManagementPermission(repository.getIntegration().getEvent().getEventId(), currentUser.getUserId());

        if (!"CONNECTED".equals(repository.getIntegration().getConnectionStatus())) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.INVALID_GITHUB_TOKEN, "Repository integration is disconnected");
        }

        return performSynchronization(repositoryId, currentUser.getUserId(), "MANUAL");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RepositorySyncLog createRunningSyncLog(UUID repositoryId, UUID userId, String syncType) {
        RepositoryEntity repository = repositoryEntityRepository.findById(repositoryId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        if ("RUNNING".equals(repository.getSyncStatus())) {
            // Stale lock recovery (e.g. 1 hour)
            if (repository.getLastSyncAt() != null && repository.getLastSyncAt().isBefore(LocalDateTime.now().minusHours(1))) {
                log.warn("Found stale RUNNING lock for repository {}. Overriding.", repositoryId);
            } else {
                throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.REPOSITORY_SYNC_ALREADY_RUNNING, "A synchronization operation is already in progress");
            }
        }

        repository.setSyncStatus("RUNNING");
        repository.setLastSyncAt(LocalDateTime.now());
        repositoryEntityRepository.save(repository);

        RepositorySyncLog syncLog = new RepositorySyncLog();
        syncLog.setRepository(repository);
        syncLog.setTriggeredBy(user);
        syncLog.setSyncType(syncType);
        syncLog.setStatus("RUNNING");
        return syncLogRepository.save(syncLog);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void upsertIssuesPage(UUID repositoryId, List<JsonNode> issues, RepositorySyncLog syncLog) {
        RepositoryEntity repository = repositoryEntityRepository.findById(repositoryId).orElseThrow();

        for (JsonNode issueNode : issues) {
            syncLog.setItemsFetched(syncLog.getItemsFetched() + 1);
            try {
                String externalId = issueNode.get("id").asText();
                Optional<RepositoryIssue> existingOpt = issueRepository.findByRepository_RepositoryIdAndExternalId(repositoryId, externalId);

                RepositoryIssue issue;
                boolean isNew = false;
                if (existingOpt.isPresent()) {
                    issue = existingOpt.get();
                } else {
                    issue = new RepositoryIssue();
                    isNew = true;
                }
                
                issue.setRepository(repository);
                issue.setExternalId(externalId);
                issue.setNumber(issueNode.get("number").asInt());
                issue.setTitle(issueNode.get("title").asText());
                
                if (issueNode.hasNonNull("body")) {
                    issue.setBody(issueNode.get("body").asText());
                }
                
                issue.setState(issueNode.get("state").asText());
                issue.setUrl(issueNode.get("html_url").asText());
                
                if (issueNode.hasNonNull("user")) {
                    issue.setAuthorUsername(issueNode.get("user").get("login").asText());
                }
                
                if (issueNode.hasNonNull("assignee")) {
                    issue.setAssigneeUsername(issueNode.get("assignee").get("login").asText());
                }
                
                if (issueNode.hasNonNull("labels")) {
                    StringBuilder labels = new StringBuilder();
                    for (JsonNode label : issueNode.get("labels")) {
                        labels.append(label.get("name").asText()).append(",");
                    }
                    if (labels.length() > 0) labels.setLength(labels.length() - 1);
                    issue.setLabels(labels.toString());
                }
                
                if (issueNode.hasNonNull("milestone")) {
                    issue.setMilestone(issueNode.get("milestone").get("title").asText());
                }
                
                issue.setCommentCount(issueNode.hasNonNull("comments") ? issueNode.get("comments").asInt() : 0);
                
                if (issueNode.hasNonNull("created_at")) {
                    issue.setExternalCreatedAt(java.time.OffsetDateTime.parse(issueNode.get("created_at").asText()).toLocalDateTime());
                } else {
                    issue.setExternalCreatedAt(LocalDateTime.now());
                }
                
                if (issueNode.hasNonNull("updated_at")) {
                    issue.setExternalUpdatedAt(java.time.OffsetDateTime.parse(issueNode.get("updated_at").asText()).toLocalDateTime());
                } else {
                    issue.setExternalUpdatedAt(LocalDateTime.now());
                }
                
                if (issueNode.hasNonNull("closed_at")) {
                    issue.setExternalClosedAt(java.time.OffsetDateTime.parse(issueNode.get("closed_at").asText()).toLocalDateTime());
                }

                issueRepository.save(issue);
                
                if (isNew) {
                    syncLog.setItemsCreated(syncLog.getItemsCreated() + 1);
                } else {
                    syncLog.setItemsUpdated(syncLog.getItemsUpdated() + 1);
                }
            } catch (Exception e) {
                log.error("Failed to upsert issue", e);
                syncLog.setItemsFailed(syncLog.getItemsFailed() + 1);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeSyncLog(UUID syncLogId, String status, String errorCode, String errorMessage, boolean hasMore,
                                int itemsFetched, int itemsCreated, int itemsUpdated, int itemsFailed) {
        RepositorySyncLog syncLog = syncLogRepository.findById(syncLogId).orElseThrow();
        syncLog.setStatus(status);
        syncLog.setErrorCode(errorCode);
        syncLog.setErrorMessage(errorMessage);
        syncLog.setHasMore(hasMore);
        
        // update stats since they were manipulated outside TX in orchestration loop
        syncLog.setItemsFetched(itemsFetched);
        syncLog.setItemsCreated(itemsCreated);
        syncLog.setItemsUpdated(itemsUpdated);
        syncLog.setItemsFailed(itemsFailed);
        
        syncLog.setCompletedAt(LocalDateTime.now());
        syncLogRepository.save(syncLog);

        RepositoryEntity repository = syncLog.getRepository();
        repository.setSyncStatus(status);
        repositoryEntityRepository.save(repository);
    }

    public RepositorySyncResponse performSynchronization(UUID repositoryId, UUID userId, String syncType) {
        RepositoryEntity repository = repositoryEntityRepository.findById(repositoryId).orElseThrow();
        RepositoryIntegration integration = repository.getIntegration();

        if (!"CONNECTED".equals(integration.getConnectionStatus())) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.INVALID_GITHUB_TOKEN, "Cannot synchronize disconnected integration");
        }

        String plaintextToken;
        try {
            plaintextToken = encryptionService.decrypt(integration.getEncryptedToken(), integration.getEncryptionIv(), integration.getEncryptionFormatVersion());
        } catch (Exception e) {
            throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.TOKEN_ENCRYPTION_CONFIGURATION_ERROR, "Failed to decrypt token");
        }

        GitHubApiClient.RepoOwnerAndName info = gitHubApiClient.parseRepoUrl(repository.getRepositoryUrl());
        
        RepositorySyncLog syncLog = createRunningSyncLog(repositoryId, userId, syncType);

        int maxPages = 5;
        int perPage = 100;
        int currentPage = 1;
        boolean hasMore = false;
        String finalStatus = "SUCCESS";
        String errorCode = null;
        String errorMessage = null;

        // Keep local counters to push at the end, because the entity from createRunningSyncLog is detached from later txs
        int fetched = 0, created = 0, updated = 0, failed = 0;
        int pagesSucceeded = 0;

        try {
            while (currentPage <= maxPages) {
                GitHubApiClient.IssuesPageResult pageResult = gitHubApiClient.fetchIssuesPage(info.owner, info.repo, plaintextToken, currentPage, perPage);
                
                if (pageResult.issues.isEmpty()) {
                    break;
                }

                // Temporary object to hold counts for this page
                RepositorySyncLog tempLog = new RepositorySyncLog();
                tempLog.setItemsFetched(0); tempLog.setItemsCreated(0); tempLog.setItemsUpdated(0); tempLog.setItemsFailed(0);
                
                upsertIssuesPage(repositoryId, pageResult.issues, tempLog);
                
                fetched += tempLog.getItemsFetched();
                created += tempLog.getItemsCreated();
                updated += tempLog.getItemsUpdated();
                failed += tempLog.getItemsFailed();

                pagesSucceeded++;

                if (pageResult.hasMore) {
                    if (currentPage == maxPages) {
                        hasMore = true;
                        finalStatus = "PARTIAL_SUCCESS";
                    } else {
                        currentPage++;
                    }
                } else {
                    break;
                }
            }
        } catch (RepositoryIntegrationException ex) {
            errorCode = ex.getErrorCode().name();
            errorMessage = ex.getMessage();
            if (pagesSucceeded > 0) {
                finalStatus = "PARTIAL_SUCCESS"; // At least one page succeeded before failing
            } else {
                finalStatus = "FAILED";
            }
        } catch (Exception ex) {
            errorCode = RepositoryIntegrationException.ErrorCode.GITHUB_UPSTREAM_ERROR.name();
            errorMessage = "Unexpected error during synchronization: " + ex.getMessage();
            if (pagesSucceeded > 0) {
                finalStatus = "PARTIAL_SUCCESS";
            } else {
                finalStatus = "FAILED";
            }
        } finally {
            if (failed > 0 && "SUCCESS".equals(finalStatus)) {
                finalStatus = "PARTIAL_SUCCESS";
            }
            completeSyncLog(syncLog.getSyncLogId(), finalStatus, errorCode, errorMessage, hasMore, fetched, created, updated, failed);
            
            if ("FAILED".equals(finalStatus) && errorCode != null) {
                throw new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.valueOf(errorCode), errorMessage);
            }
        }

        String syncMessage;
        if ("PARTIAL_SUCCESS".equals(finalStatus)) {
            syncMessage = String.format("Synchronization partially completed. Processed %d issues (%d created, %d updated).", fetched, created, updated);
        } else if (fetched == 0) {
            syncMessage = "Synchronization completed. No GitHub issues were found.";
        } else {
            syncMessage = String.format("Synchronized %d issues: %d created, %d updated.", fetched, created, updated);
        }

        return RepositorySyncResponse.builder()
                .repositoryId(repositoryId)
                .status(finalStatus)
                .itemsFetched(fetched)
                .itemsCreated(created)
                .itemsUpdated(updated)
                .itemsFailed(failed)
                .hasMore(hasMore)
                .message(syncMessage)
                .build();
    }

    @Transactional
    public void disconnectIntegration(UUID integrationId) {
        User currentUser = getCurrentUser();
        RepositoryIntegration integration = integrationRepository.findById(integrationId)
                .orElseThrow(() -> new RepositoryIntegrationException(RepositoryIntegrationException.ErrorCode.REPOSITORY_INTEGRATION_NOT_FOUND, "Integration not found"));

        requireRepositoryManagementPermission(integration.getEvent().getEventId(), currentUser.getUserId());

        integration.setConnectionStatus("DISCONNECTED");
        integration.setEncryptedToken(null);
        integration.setEncryptionIv(null);
        integration.setEncryptionFormatVersion(null);
        integrationRepository.save(integration);
    }
}
