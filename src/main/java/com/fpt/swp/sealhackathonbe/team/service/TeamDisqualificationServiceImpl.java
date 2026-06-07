package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.team.dto.DisqualificationResponse;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualifyTeamRequest;
import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.DisqualificationsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TeamDisqualificationServiceImpl implements TeamDisqualificationService {
    private static final UUID TEAM_STATUS_DISQUALIFIED =
            UUID.fromString("60000000-0000-0000-0000-000000000003");

    private final TeamsRepository teamsRepository;
    private final DisqualificationsRepository disqualificationsRepository;

    public TeamDisqualificationServiceImpl(
            TeamsRepository teamsRepository,
            DisqualificationsRepository disqualificationsRepository
    ) {
        this.teamsRepository = teamsRepository;
        this.disqualificationsRepository = disqualificationsRepository;
    }

    @Override
    @Transactional
    public DisqualificationResponse disqualifyTeam(
            UUID teamId,
            DisqualifyTeamRequest request,
            UUID adminUserId
    ) {
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

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
}
