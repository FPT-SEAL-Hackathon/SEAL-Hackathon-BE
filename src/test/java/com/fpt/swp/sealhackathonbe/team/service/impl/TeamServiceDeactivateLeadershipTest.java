package com.fpt.swp.sealhackathonbe.team.service.impl;

import com.fpt.swp.sealhackathonbe.auth.repository.AuditLogRepository;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.eventparticipant.repository.EventParticipantRepository;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import com.fpt.swp.sealhackathonbe.team.dto.LeadershipReassignmentResult;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamJoinRequestsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.service.TeamEventRegistrationService;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceDeactivateLeadershipTest {

    @Mock private EventRepository eventRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TeamsRepository teamsRepository;
    @Mock private TeamMembersRepository teamMembersRepository;
    @Mock private TeamJoinRequestsRepository teamJoinRequestsRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private TeamEventRegistrationService teamEventRegistrationService;
    @Mock private TeamJoinRequestCleaner teamJoinRequestCleaner;
    @Mock private EventParticipantRepository eventParticipantRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private TeamServiceImpl service;

    private TeamMembers member(UUID teamId, UUID userId, String fullName) {
        TeamMembers m = new TeamMembers();
        m.setTeamId(teamId);
        m.setUserId(userId);
        m.setActive(true);
        User u = new User();
        u.setUserId(userId);
        u.setFullName(fullName);
        m.setUser(u);
        return m;
    }

    private Teams team(UUID teamId, UUID leaderId) {
        Teams t = new Teams();
        t.setTeamId(teamId);
        t.setLeaderUserId(leaderId);
        t.setEventId(UUID.randomUUID());
        t.setTeamName("Team X");
        return t;
    }

    @Test
    void leaderWithAnotherMember_transfersLeadershipKeepsMembership() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Teams t = team(teamId, leaderId);

        when(teamMembersRepository.findAllByUserIdAndActiveTrue(leaderId))
                .thenReturn(List.of(member(teamId, leaderId, "Leader")));
        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(t));
        when(teamMembersRepository.findByTeamIdAndActiveTrueOrderByJoinedAtAscTeamMemberIdAsc(teamId))
                .thenReturn(List.of(member(teamId, leaderId, "Leader"), member(teamId, memberId, "Bob")));

        LeadershipReassignmentResult result = service.reassignLeadershipForDeactivatedUser(leaderId, actorId);

        // Quyền chuyển sang thành viên còn lại; team KHÔNG bị xóa; membership không đụng.
        assertEquals(memberId, t.getLeaderUserId());
        verify(teamsRepository).save(t);
        verify(teamMembersRepository, never()).deleteByUserId(any());
        verify(teamsRepository, never()).delete(any());
        assertEquals(1, result.getTransfers().size());
        assertTrue(result.getTransfers().get(0).getNewLeaderName().equals("Bob"));
        assertTrue(result.getFrozenTeams().isEmpty());
    }

    @Test
    void soleLeader_isFrozenNotDissolved() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Teams t = team(teamId, leaderId);

        when(teamMembersRepository.findAllByUserIdAndActiveTrue(leaderId))
                .thenReturn(List.of(member(teamId, leaderId, "Leader")));
        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(t));
        when(teamMembersRepository.findByTeamIdAndActiveTrueOrderByJoinedAtAscTeamMemberIdAsc(teamId))
                .thenReturn(List.of(member(teamId, leaderId, "Leader")));

        LeadershipReassignmentResult result = service.reassignLeadershipForDeactivatedUser(leaderId, actorId);

        // Không chuyển, không xóa; đưa vào frozen để cảnh báo.
        assertEquals(leaderId, t.getLeaderUserId());
        verify(teamsRepository, never()).save(any());
        verify(teamsRepository, never()).delete(any());
        assertEquals(1, result.getFrozenTeams().size());
        assertTrue(result.getTransfers().isEmpty());
    }

    @Test
    void plainMember_isNoOp() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Teams t = team(teamId, leaderId);

        when(teamMembersRepository.findAllByUserIdAndActiveTrue(memberId))
                .thenReturn(List.of(member(teamId, memberId, "Bob")));
        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(t));

        LeadershipReassignmentResult result = service.reassignLeadershipForDeactivatedUser(memberId, actorId);

        // User không phải leader → không đụng gì.
        assertEquals(leaderId, t.getLeaderUserId());
        verify(teamsRepository, never()).save(any());
        assertTrue(result.getTransfers().isEmpty());
        assertTrue(result.getFrozenTeams().isEmpty());
    }
}
