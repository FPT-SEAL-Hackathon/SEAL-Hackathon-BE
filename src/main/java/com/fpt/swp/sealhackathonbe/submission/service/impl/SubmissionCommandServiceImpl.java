package com.fpt.swp.sealhackathonbe.submission.service.impl;

import com.fpt.swp.sealhackathonbe.submission.dto.CreateSubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionCommandService;
import com.fpt.swp.sealhackathonbe.submission.service.mapper.SubmissionMapper;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SubmissionCommandServiceImpl implements SubmissionCommandService {
    private static final UUID TEAM_STATUS_DISQUALIFIED =
            UUID.fromString("60000000-0000-0000-0000-000000000003");

    private static final UUID TEAM_STATUS_WITHDRAWN =
            UUID.fromString("60000000-0000-0000-0000-000000000004");

    private final SubmissionsRepository submissionsRepository;
    private final TeamsRepository teamsRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final EntityManager entityManager;

    public SubmissionCommandServiceImpl(
            SubmissionsRepository submissionsRepository,
            TeamsRepository teamsRepository,
            TeamMembersRepository teamMembersRepository,
            EntityManager entityManager
    ) {
        this.submissionsRepository = submissionsRepository;
        this.teamsRepository = teamsRepository;
        this.teamMembersRepository = teamMembersRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public SubmissionResponse submitWork(CreateSubmissionRequest request, UUID currentUserId) {
        validateUserBelongsToTeam(request.getTeamId(), currentUserId);
        validateTeamCanSubmit(request.getTeamId());
        validateSubmissionDeadline(request.getRoundId());

        callUpsertSubmissionProcedure(request, currentUserId);

        Submissions submission = submissionsRepository
                .findByTeamIdAndRoundId(request.getTeamId(), request.getRoundId())
                .orElseThrow(() -> new RuntimeException("Submission was not created or updated"));

        return SubmissionMapper.toSubmissionResponse(submission);
    }

    private void validateUserBelongsToTeam(UUID teamId, UUID currentUserId) {
        boolean isMember = teamMembersRepository
                .findByTeamIdAndUserIdAndActiveTrue(teamId, currentUserId)
                .isPresent();

        if (!isMember) {
            throw new RuntimeException("User does not belong to this team");
        }
    }

    private void validateTeamCanSubmit(UUID teamId) {
        var team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (TEAM_STATUS_DISQUALIFIED.equals(team.getTeamStatusId())
                || TEAM_STATUS_WITHDRAWN.equals(team.getTeamStatusId())) {
            throw new RuntimeException("This team cannot submit because it is disqualified or withdrawn");
        }
    }

    private void validateSubmissionDeadline(UUID roundId) {
        Object result;

        try {
            result = entityManager
                    .createNativeQuery(
                            "SELECT SubmissionDeadline FROM Rounds WHERE RoundID = CAST(:roundId AS uniqueidentifier)"
                    )
                    .setParameter("roundId", roundId.toString())
                    .getSingleResult();
        } catch (NoResultException exception) {
            throw new RuntimeException("Round not found");
        }

        if (result == null) {
            return;
        }

        LocalDateTime deadline;

        if (result instanceof Timestamp timestamp) {
            deadline = timestamp.toLocalDateTime();
        } else if (result instanceof LocalDateTime localDateTime) {
            deadline = localDateTime;
        } else if (result instanceof java.sql.Date date) {
            deadline = date.toLocalDate().atStartOfDay();
        } else {
            throw new RuntimeException("Invalid submission deadline type");
        }

        if (LocalDateTime.now().isAfter(deadline)) {
            throw new RuntimeException("Submission deadline has passed");
        }
    }

    private void callUpsertSubmissionProcedure(CreateSubmissionRequest request, UUID currentUserId) {
        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery("sp_UpsertSubmission");

        query.registerStoredProcedureParameter("TeamID", UUID.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("RoundID", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("RepositoryURL", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("DemoURL", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("ReportURL", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("SlideURL", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("Notes", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("RepoMetadataJSON", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("RepoLastCommitAt", LocalDateTime.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("RepoStarCount", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("RepoForkCount", Integer.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("SubmittedByUserID", UUID.class, ParameterMode.IN);

        query.setParameter("TeamID", request.getTeamId());
        query.setParameter("RoundID", request.getRoundId().toString());
        query.setParameter("RepositoryURL", request.getRepositoryUrl());
        query.setParameter("DemoURL", request.getDemoUrl());
        query.setParameter("ReportURL", request.getReportUrl());
        query.setParameter("SlideURL", request.getSlideUrl());
        query.setParameter("Notes", request.getNotes());
        query.setParameter("RepoMetadataJSON", null);
        query.setParameter("RepoLastCommitAt", null);
        query.setParameter("RepoStarCount", null);
        query.setParameter("RepoForkCount", null);
        query.setParameter("SubmittedByUserID", currentUserId);

        query.execute();
    }
}
