package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.team.dto.TeamResponse;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.service.impl.TeamServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplLeadershipTest {
    private static final UUID WITHDRAWN_STATUS =
            UUID.fromString("60000000-0000-0000-0000-000000000004");

    @Mock
    private TeamsRepository teamsRepository;

    @Mock
    private TeamMembersRepository teamMembersRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    @Test
    void leaderLeavingTransfersLeadershipToFirstRemainingMember() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID successorId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        TeamMembers leader = member(team, leaderId);
        TeamMembers successor = member(team, successorId);

        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, leaderId))
                .thenReturn(Optional.of(leader));
        when(teamMembersRepository.findByTeamIdAndActiveTrueOrderByJoinedAtAscTeamMemberIdAsc(teamId))
                .thenReturn(List.of(successor));

        teamService.removeMember(teamId, leaderId, leaderId);

        assertFalse(leader.getActive());
        assertNotNull(leader.getLeftAt());
        assertEquals(successorId, team.getLeaderUserId());
        verify(teamsRepository).save(team);
    }

    @Test
    void lastLeaderLeavingWithdrawsTeam() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        TeamMembers leader = member(team, leaderId);

        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, leaderId))
                .thenReturn(Optional.of(leader));
        when(teamMembersRepository.findByTeamIdAndActiveTrueOrderByJoinedAtAscTeamMemberIdAsc(teamId))
                .thenReturn(List.of());

        teamService.removeMember(teamId, leaderId, leaderId);

        assertEquals(WITHDRAWN_STATUS, team.getTeamStatusId());
        verify(teamsRepository).save(team);
    }

    @Test
    void regularMemberCanLeaveWithoutMinimumSizeCheck() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        TeamMembers member = member(team, memberId);

        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, memberId))
                .thenReturn(Optional.of(member));

        teamService.removeMember(teamId, memberId, memberId);

        assertFalse(member.getActive());
        verify(teamsRepository, never()).save(team);
        verify(teamMembersRepository, never()).countByTeamIdAndActiveTrue(teamId);
    }

    @Test
    void currentLeaderCanTransferLeadershipToActiveMember() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID newLeaderId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        TeamMembers newLeader = member(team, newLeaderId);

        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, newLeaderId))
                .thenReturn(Optional.of(newLeader));
        when(teamsRepository.save(team)).thenReturn(team);
        when(teamMembersRepository.findByTeamIdAndActiveTrue(teamId)).thenReturn(List.of(newLeader));

        TeamResponse response = teamService.transferLeadership(teamId, newLeaderId, leaderId);

        assertEquals(newLeaderId, team.getLeaderUserId());
        assertEquals(newLeaderId, response.getLeaderUserId());
    }

    @Test
    void nonLeaderCannotTransferLeadership() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));

        assertThrows(
                AccessDeniedException.class,
                () -> teamService.transferLeadership(teamId, UUID.randomUUID(), requesterId)
        );
    }

    @Test
    void leadershipCannotBeTransferredToInactiveOrExternalUser() {
        UUID teamId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID externalUserId = UUID.randomUUID();
        Teams team = team(teamId, leaderId);
        when(teamsRepository.findByIdForUpdate(teamId)).thenReturn(Optional.of(team));
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, externalUserId))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessConflictException.class,
                () -> teamService.transferLeadership(teamId, externalUserId, leaderId)
        );
    }

    private Teams team(UUID teamId, UUID leaderId) {
        Teams team = new Teams();
        team.setTeamId(teamId);
        team.setLeaderUserId(leaderId);
        return team;
    }

    private TeamMembers member(Teams team, UUID userId) {
        TeamMembers member = new TeamMembers();
        member.setTeamId(team.getTeamId());
        member.setTeam(team);
        member.setUserId(userId);
        member.setActive(true);
        return member;
    }
}
