package com.fpt.swp.sealhackathonbe.team.service.impl;

import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualificationResponse;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualifiedTeamResponse;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualifyTeamRequest;
import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.DisqualificationsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.service.TeamDisqualificationService;
import com.fpt.swp.sealhackathonbe.team.service.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamDisqualificationServiceImpl implements TeamDisqualificationService {
    private static final UUID TEAM_STATUS_DISQUALIFIED =
            UUID.fromString("60000000-0000-0000-0000-000000000003");

    private final TeamsRepository teamsRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final DisqualificationsRepository disqualificationsRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public DisqualificationResponse disqualifyTeam(
            UUID teamId,
            DisqualifyTeamRequest request,
            UUID adminUserId
    ) {
        // Luong du lieu:
        // 1. Admin chon team can loai.
        // 2. Kiem tra team chua co disqualification active de tranh tao trung.
        // 3. Doi trang thai team thanh DISQUALIFIED.
        // 4. Ghi Disqualifications voi TeamID co gia tri va SubmissionID = null.
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        boolean alreadyDisqualified = disqualificationsRepository.findByTeamId(teamId)
                .stream()
                .anyMatch(disqualification -> !Boolean.TRUE.equals(disqualification.getReversed()));

        if (alreadyDisqualified) {
            throw new RuntimeException("Team is already disqualified");
        }

        team.setTeamStatusId(TEAM_STATUS_DISQUALIFIED);
        team.setUpdatedAt(LocalDateTime.now());
        teamsRepository.save(team);

        Disqualifications disqualification = new Disqualifications();
        disqualification.setTeamId(teamId);
        disqualification.setSubmissionId(null);
        disqualification.setReason(request.getReason());
        disqualification.setDisqualifiedById(adminUserId);
        disqualification.setDisqualifiedAt(LocalDateTime.now());
        disqualification.setReversed(false);

        Disqualifications saved = disqualificationsRepository.save(disqualification);
        return TeamMapper.toDisqualificationResponse(saved);
    }

    @Override
    @Transactional
    public List<DisqualificationResponse> disqualifyIneligibleTeams(
            UUID eventId,
            DisqualifyTeamRequest request,
            UUID adminUserId
    ) {
        Event event = eventRepository.findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        return teamsRepository.findByEventId(eventId)
                .stream()
                .filter(team -> !TEAM_STATUS_DISQUALIFIED.equals(team.getTeamStatusId()))
                .filter(team -> !hasActiveDisqualification(team.getTeamId()))
                .filter(team -> isTeamSizeIneligible(team, event))
                .map(team -> disqualifyTeamForPreCheck(team, request.getReason(), adminUserId))
                .map(TeamMapper::toDisqualificationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisqualifiedTeamResponse> getDisqualifiedTeams(UUID roundId, UUID categoryId) {
        // Luong du lieu: RoundID + CategoryID -> disqualification active -> DTO.
        return disqualificationsRepository.findActiveTeamDisqualifications(roundId, categoryId)
                .stream()
                .map(TeamMapper::toDisqualifiedTeamResponse)
                .toList();
    }

    private boolean hasActiveDisqualification(UUID teamId) {
        return disqualificationsRepository.findByTeamId(teamId)
                .stream()
                .anyMatch(disqualification -> !Boolean.TRUE.equals(disqualification.getReversed()));
    }

    private boolean isTeamSizeIneligible(Teams team, Event event) {
        long activeMemberCount = teamMembersRepository.countByTeamIdAndActiveTrue(team.getTeamId());

        Integer minTeamSize = event.getMinTeamSize();
        if (minTeamSize != null && activeMemberCount < minTeamSize) {
            return true;
        }

        Integer maxTeamSize = event.getMaxTeamSize();
        return maxTeamSize != null && activeMemberCount > maxTeamSize;
    }

    private Disqualifications disqualifyTeamForPreCheck(
            Teams team,
            String reason,
            UUID adminUserId
    ) {
        team.setTeamStatusId(TEAM_STATUS_DISQUALIFIED);
        team.setUpdatedAt(LocalDateTime.now());
        teamsRepository.save(team);

        Disqualifications disqualification = new Disqualifications();
        disqualification.setTeamId(team.getTeamId());
        disqualification.setSubmissionId(null);
        disqualification.setReason(reason);
        disqualification.setDisqualifiedById(adminUserId);
        disqualification.setDisqualifiedAt(LocalDateTime.now());
        disqualification.setReversed(false);

        return disqualificationsRepository.save(disqualification);
    }
}
