package com.fpt.swp.sealhackathonbe.integration.repository.service;

import com.fpt.swp.sealhackathonbe.core.exception.RepositoryIntegrationException;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.GitHubRepoDto;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryEntity;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryIntegration;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositorySyncLog;
import com.fpt.swp.sealhackathonbe.integration.repository.repository.RepositoryEntityRepository;
import com.fpt.swp.sealhackathonbe.integration.repository.repository.RepositoryIntegrationRepository;
import com.fpt.swp.sealhackathonbe.integration.repository.repository.RepositoryIssueRepository;
import com.fpt.swp.sealhackathonbe.integration.repository.repository.RepositorySyncLogRepository;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RepositoryIntegrationServiceTest {

    @Mock
    private RepositoryIntegrationRepository integrationRepository;
    @Mock
    private RepositoryEntityRepository repositoryEntityRepository;
    @Mock
    private RepositoryIssueRepository issueRepository;
    @Mock
    private RepositorySyncLogRepository syncLogRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GitHubApiClient gitHubApiClient;
    @Mock
    private RepositoryTokenEncryptionService encryptionService;

    @InjectMocks
    private RepositoryIntegrationService repositoryIntegrationService;

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setEmail("organizer@fpt.edu.vn");

        testEvent = new Event();
        testEvent.setEventId(UUID.randomUUID());
        testEvent.setCreatedBy(testUser);
    }

    private void mockAuthentication(boolean isOrganizer) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "organizer@fpt.edu.vn",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority(isOrganizer ? "ROLE_ORGANIZER" : "ROLE_MEMBER"))
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        if (isOrganizer) {
            lenient().when(userRepository.findByEmail("organizer@fpt.edu.vn")).thenReturn(testUser);
        }
    }

    @Test
    void testConnection_NonCreator_ThrowsAccessDenied() {
        mockAuthentication(false);
        User creator = new User();
        creator.setUserId(UUID.randomUUID());
        testEvent.setCreatedBy(creator);
        when(userRepository.findByEmail("organizer@fpt.edu.vn")).thenReturn(testUser);
        when(eventRepository.findById(testEvent.getEventId())).thenReturn(Optional.of(testEvent));

        assertThrows(RepositoryIntegrationException.class, () ->
                repositoryIntegrationService.testConnection(testEvent.getEventId(), "https://github.com/fpt/test", "token"));
    }

    @Test
    void testConnectIntegration_Success() {
        mockAuthentication(true);
        when(eventRepository.findById(testEvent.getEventId())).thenReturn(Optional.of(testEvent));
        
        GitHubRepoDto repoDto = new GitHubRepoDto();
        repoDto.setId(12345L);
        repoDto.setName("test-repo");
        repoDto.setFullName("fpt/test-repo");
        repoDto.setHtmlUrl("https://github.com/fpt/test-repo");
        repoDto.setDescription("Test repository");

        when(gitHubApiClient.fetchRepository(anyString(), anyString())).thenReturn(repoDto);
        RepositoryTokenEncryptionService.EncryptedData encryptedData =
                new RepositoryTokenEncryptionService.EncryptedData("encrypted_token".getBytes(), "iv".getBytes(), 1);
        when(encryptionService.encrypt(anyString())).thenReturn(encryptedData);
        when(encryptionService.decrypt(any(), any(), any())).thenReturn("token");
        when(integrationRepository.findAllByEvent_EventId(testEvent.getEventId()))
                .thenReturn(Collections.emptyList());

        RepositoryIntegration savedIntegration = new RepositoryIntegration();
        savedIntegration.setIntegrationId(UUID.randomUUID());
        savedIntegration.setEvent(testEvent);
        savedIntegration.setConnectionStatus("CONNECTED");
        savedIntegration.setEncryptedToken("encrypted_token".getBytes());
        savedIntegration.setEncryptionIv("iv".getBytes());
        savedIntegration.setEncryptionFormatVersion(1);
        when(integrationRepository.save(any())).thenReturn(savedIntegration);
        when(repositoryEntityRepository.findByIntegration_IntegrationIdAndExternalId(any(), anyString()))
                .thenReturn(Optional.empty());
        final RepositoryEntity[] savedRepository = new RepositoryEntity[1];
        when(repositoryEntityRepository.save(any(RepositoryEntity.class))).thenAnswer(invocation -> {
            RepositoryEntity repository = invocation.getArgument(0);
            if (repository.getRepositoryId() == null) {
                repository.setRepositoryId(UUID.randomUUID());
            }
            savedRepository[0] = repository;
            return repository;
        });
        when(repositoryEntityRepository.findById(any())).thenAnswer(invocation -> Optional.of(savedRepository[0]));
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(gitHubApiClient.parseRepoUrl(anyString())).thenReturn(new GitHubApiClient.RepoOwnerAndName("fpt", "test-repo"));
        when(gitHubApiClient.fetchIssuesPage(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new GitHubApiClient.IssuesPageResult(Collections.emptyList(), false));
        when(syncLogRepository.save(any(RepositorySyncLog.class))).thenAnswer(invocation -> {
            RepositorySyncLog syncLog = invocation.getArgument(0);
            if (syncLog.getSyncLogId() == null) {
                syncLog.setSyncLogId(UUID.randomUUID());
            }
            return syncLog;
        });
        when(syncLogRepository.findById(any())).thenAnswer(invocation -> {
            RepositorySyncLog syncLog = new RepositorySyncLog();
            syncLog.setSyncLogId(invocation.getArgument(0));
            syncLog.setRepository(savedRepository[0]);
            return Optional.of(syncLog);
        });

        repositoryIntegrationService.connectIntegration(testEvent.getEventId(), "https://github.com/fpt/test-repo", "token");

        verify(integrationRepository).save(any(RepositoryIntegration.class));
        verify(repositoryEntityRepository, atLeastOnce()).save(any());
        verify(encryptionService).encrypt("token");
    }
}
