package com.fpt.swp.sealhackathonbe.team.service.impl;

import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualificationResponse;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualifiedTeamResponse;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualifyTeamRequest;
import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.DisqualificationsRepository;
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
    private static final UUID SUBMISSION_STATUS_DISQUALIFIED =
            UUID.fromString("50000000-0000-0000-0000-000000000004");

    private final TeamsRepository teamsRepository;
    private final DisqualificationsRepository disqualificationsRepository;
    private final SubmissionsRepository submissionsRepository;

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
        // 4. Doi cac submission hien co cua team sang DISQUALIFIED de danh sach submission khong lech status.
        // 5. Ghi Disqualifications voi TeamID co gia tri va SubmissionID = null.
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        boolean alreadyDisqualified = disqualificationsRepository.findByTeamId(teamId)
                .stream()
                .anyMatch(disqualification -> !Boolean.TRUE.equals(disqualification.getReversed()));

        if (alreadyDisqualified) {
            throw new RuntimeException("Team is already disqualified");
        }

        LocalDateTime now = LocalDateTime.now();

        team.setTeamStatusId(TEAM_STATUS_DISQUALIFIED);
        team.setUpdatedAt(now);
        teamsRepository.save(team);

        List<Submissions> submissions = submissionsRepository.findByTeamId(teamId);
        submissions.forEach(submission -> {
            submission.setSubmissionStatusId(SUBMISSION_STATUS_DISQUALIFIED);
            submission.setLastUpdatedAt(now);
        });
        submissionsRepository.saveAll(submissions);

        Disqualifications disqualification = new Disqualifications();
        disqualification.setTeamId(teamId);
        disqualification.setSubmissionId(null);
        disqualification.setReason(request.getReason());
        disqualification.setDisqualifiedById(adminUserId);
        disqualification.setDisqualifiedAt(now);
        disqualification.setReversed(false);

        Disqualifications saved = disqualificationsRepository.save(disqualification);
        return TeamMapper.toDisqualificationResponse(saved);
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

}
