package com.fpt.swp.sealhackathonbe.team.service.impl;

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
    @Transactional(readOnly = true)
    public List<DisqualifiedTeamResponse> getDisqualifiedTeams() {
        // Luong du lieu: Disqualifications active -> Team -> member active -> DTO tong hop.
        return disqualificationsRepository.findActiveTeamDisqualifications()
                .stream()
                .map(disqualification -> TeamMapper.toDisqualifiedTeamResponse(
                        disqualification,
                        teamMembersRepository.findByTeamIdAndActiveTrue(disqualification.getTeamId())
                ))
                .toList();
    }
}
