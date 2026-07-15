package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.eventparticipant.entity.EventParticipant;
import com.fpt.swp.sealhackathonbe.eventparticipant.entity.ParticipantStatus;
import com.fpt.swp.sealhackathonbe.eventparticipant.repository.EventParticipantRepository;
import com.fpt.swp.sealhackathonbe.eventparticipant.repository.ParticipantStatusRepository;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualifyTeamRequest;
import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.DisqualificationsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.service.impl.TeamDisqualificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamDisqualificationServiceImplTest {
    private static final UUID TEAM_STATUS_DISQUALIFIED =
            UUID.fromString("60000000-0000-0000-0000-000000000003");

    @Mock
    private TeamsRepository teamsRepository;

    @Mock
    private DisqualificationsRepository disqualificationsRepository;

    @Mock
    private SubmissionsRepository submissionsRepository;

    @Mock
    private TeamMembersRepository teamMembersRepository;

    @Mock
    private EventParticipantRepository eventParticipantRepository;

    @Mock
    private ParticipantStatusRepository participantStatusRepository;

    @InjectMocks
    private TeamDisqualificationServiceImpl service;

    @Test
    void disqualifyTeamSuspendsActiveMembersEventParticipants() {
        UUID teamId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID suspendedStatusId = UUID.randomUUID();

        Teams team = new Teams();
        team.setTeamId(teamId);
        team.setEventId(eventId);

        TeamMembers leader = member(teamId, leaderId);
        TeamMembers member = member(teamId, memberId);

        ParticipantStatus suspendedStatus = new ParticipantStatus();
        suspendedStatus.setStatusId(suspendedStatusId);
        suspendedStatus.setStatusName("SUSPENDED");

        EventParticipant leaderParticipant = participant(eventId, leaderId);
        EventParticipant memberParticipant = participant(eventId, memberId);

        DisqualificationRecord savedDisqualification = new DisqualificationRecord(teamId, adminId);

        when(teamsRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(disqualificationsRepository.findByTeamId(teamId)).thenReturn(List.of());
        when(submissionsRepository.findByTeamId(teamId)).thenReturn(List.of());
        when(teamMembersRepository.findByTeamIdAndActiveTrue(teamId)).thenReturn(List.of(leader, member));
        when(participantStatusRepository.findByStatusNameIgnoreCase("SUSPENDED"))
                .thenReturn(Optional.of(suspendedStatus));
        when(eventParticipantRepository.findByEventIdAndUserId(eventId, leaderId))
                .thenReturn(Optional.of(leaderParticipant));
        when(eventParticipantRepository.findByEventIdAndUserId(eventId, memberId))
                .thenReturn(Optional.of(memberParticipant));
        when(disqualificationsRepository.save(org.mockito.ArgumentMatchers.any(Disqualifications.class)))
                .thenReturn(savedDisqualification);

        DisqualifyTeamRequest request = new DisqualifyTeamRequest();
        request.setReason("Rule violation");

        service.disqualifyTeam(teamId, request, adminId);

        assertEquals(TEAM_STATUS_DISQUALIFIED, team.getTeamStatusId());
        assertEquals(suspendedStatusId, leaderParticipant.getParticipantStatusId());
        assertEquals(suspendedStatus, leaderParticipant.getParticipantStatus());
        assertEquals(suspendedStatusId, memberParticipant.getParticipantStatusId());
        assertEquals(suspendedStatus, memberParticipant.getParticipantStatus());

        verify(eventParticipantRepository).saveAll(List.of(leaderParticipant, memberParticipant));
    }

    private TeamMembers member(UUID teamId, UUID userId) {
        TeamMembers member = new TeamMembers();
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setActive(true);
        return member;
    }

    private EventParticipant participant(UUID eventId, UUID userId) {
        EventParticipant participant = new EventParticipant();
        participant.setEventId(eventId);
        participant.setUserId(userId);
        return participant;
    }

    private static class DisqualificationRecord extends Disqualifications {
        DisqualificationRecord(UUID teamId, UUID adminId) {
            setDisqualificationId(UUID.randomUUID());
            setTeamId(teamId);
            setDisqualifiedById(adminId);
        }
    }
}
