package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.eventparticipant.service.EventParticipantService;
import com.fpt.swp.sealhackathonbe.team.dto.HandleJoinRequest;
import com.fpt.swp.sealhackathonbe.team.entity.TeamJoinRequests;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.event.TeamJoinApprovedEvent;
import com.fpt.swp.sealhackathonbe.team.repository.TeamJoinRequestsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.service.impl.TeamJoinRequestServiceImpl;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamJoinRequestServiceImplTest {

    @Mock
    private TeamsRepository teamsRepository;

    @Mock
    private TeamMembersRepository teamMembersRepository;

    @Mock
    private TeamJoinRequestsRepository teamJoinRequestsRepository;

    @Mock
    private EventParticipantService eventParticipantService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TeamJoinRequestServiceImpl service;

    @Test
    void approvingFormerMemberReactivatesExistingMembership() {
        UUID requestId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();

        Event event = new Event();
        event.setEventId(eventId);
        event.setMaxTeamSize(5);
        event.setIsDeleted(false);

        Teams team = new Teams();
        team.setTeamId(teamId);
        team.setEventId(eventId);
        team.setEvent(event);
        team.setTeamName("Seal Team");
        team.setLeaderUserId(leaderId);

        User user = new User();
        user.setUserId(userId);

        TeamJoinRequests joinRequest = new TeamJoinRequests();
        joinRequest.setRequestId(requestId);
        joinRequest.setTeamId(teamId);
        joinRequest.setTeam(team);
        joinRequest.setUserId(userId);
        joinRequest.setUser(user);
        joinRequest.setRequestStatus("PENDING");
        joinRequest.setRequestedAt(LocalDateTime.now().minusDays(1));

        LocalDateTime previousJoinedAt = LocalDateTime.now().minusMonths(1);
        TeamMembers formerMembership = new TeamMembers();
        formerMembership.setTeamMemberId(UUID.randomUUID());
        formerMembership.setTeamId(teamId);
        formerMembership.setUserId(userId);
        formerMembership.setJoinedAt(previousJoinedAt);
        formerMembership.setLeftAt(LocalDateTime.now().minusDays(2));
        formerMembership.setActive(false);

        HandleJoinRequest command = new HandleJoinRequest();
        command.setAction("APPROVED");

        when(teamJoinRequestsRepository.findByRequestIdAndRequestStatus(requestId, "PENDING"))
                .thenReturn(Optional.of(joinRequest));
        when(teamMembersRepository.countByTeamIdAndActiveTrue(teamId)).thenReturn(1L);
        when(teamMembersRepository.existsByUserIdAndTeam_EventIdAndActiveTrue(userId, eventId))
                .thenReturn(false);
        when(teamMembersRepository.findByTeamIdAndUserId(teamId, userId))
                .thenReturn(Optional.of(formerMembership));
        when(teamJoinRequestsRepository.save(joinRequest)).thenReturn(joinRequest);

        service.handleJoinRequest(requestId, command, leaderId);

        ArgumentCaptor<TeamMembers> memberCaptor = ArgumentCaptor.forClass(TeamMembers.class);
        verify(teamMembersRepository).save(memberCaptor.capture());
        assertSame(formerMembership, memberCaptor.getValue());
        assertTrue(formerMembership.getActive());
        assertNull(formerMembership.getLeftAt());
        assertTrue(formerMembership.getJoinedAt().isAfter(previousJoinedAt));
        assertEquals("APPROVED", joinRequest.getRequestStatus());

        ArgumentCaptor<TeamJoinApprovedEvent> eventCaptor =
                ArgumentCaptor.forClass(TeamJoinApprovedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(userId, eventCaptor.getValue().recipientUserId());
        assertEquals(teamId, formerMembership.getTeamId());
    }
}
