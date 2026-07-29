package com.fpt.swp.sealhackathonbe.submission.service;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.core.constant.SubmissionStatusConstants;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.eventparticipant.service.EventParticipantService;
import com.fpt.swp.sealhackathonbe.integration.repository.service.SubmissionRepositoryService;
import com.fpt.swp.sealhackathonbe.ranking.entity.RoundRanking;
import com.fpt.swp.sealhackathonbe.ranking.repository.RoundRankingRepository;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.submission.dto.CreateSubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.submission.service.impl.SubmissionCommandServiceImpl;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.DisqualificationsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionCommandServiceImplTest {

    @Mock
    private SubmissionsRepository submissionsRepository;

    @Mock
    private SubmissionHistoryService submissionHistoryService;

    @Mock
    private TeamsRepository teamsRepository;

    @Mock
    private TeamMembersRepository teamMembersRepository;

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private RoundRankingRepository roundRankingRepository;

    @Mock
    private DisqualificationsRepository disqualificationsRepository;

    @Mock
    private EventParticipantService eventParticipantService;

    @Mock
    private SubmissionRepositoryService submissionRepositoryService;

    @Mock
    private PlatformTransactionManager transactionManager;

    private SubmissionCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SubmissionCommandServiceImpl(
                submissionsRepository,
                submissionHistoryService,
                teamsRepository,
                teamMembersRepository,
                roundRepository,
                roundRankingRepository,
                disqualificationsRepository,
                eventParticipantService,
                submissionRepositoryService,
                transactionManager
        );
    }

    @Test
    void teamSubmissionToCalibrationRoundIsRejected() {
        UUID categoryId = UUID.randomUUID();
        Teams team = team(categoryId);
        Round calibrationRound = round(categoryId, 1, true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> validateTeamAdvancedFromPreviousRound(team, calibrationRound));

        assertEquals("Teams cannot submit work to calibration rounds", exception.getMessage());
    }

    @Test
    void firstCompetitionRoundAllowsSubmissionWithoutRankingLookup() {
        UUID categoryId = UUID.randomUUID();
        Teams team = team(categoryId);
        Round firstCompetitionRound = round(categoryId, 2, false);

        when(roundRepository
                .findTopByCategoryCategoryIdAndRoundOrderLessThanAndIsCalibrationRoundFalseOrderByRoundOrderDesc(
                        categoryId,
                        firstCompetitionRound.getRoundOrder()
                ))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> validateTeamAdvancedFromPreviousRound(team, firstCompetitionRound));

        verify(roundRankingRepository, never())
                .findByRound_RoundIdAndCategory_CategoryIdAndTeam_TeamId(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void laterRoundRequiresApprovedAdvancedRankingFromPreviousCompetitionRound() {
        UUID categoryId = UUID.randomUUID();
        Teams team = team(categoryId);
        Round previousRound = round(categoryId, 2, false);
        Round laterRound = round(categoryId, 4, false);
        RoundRanking ranking = ranking(true, true);

        when(roundRepository
                .findTopByCategoryCategoryIdAndRoundOrderLessThanAndIsCalibrationRoundFalseOrderByRoundOrderDesc(
                        categoryId,
                        laterRound.getRoundOrder()
                ))
                .thenReturn(Optional.of(previousRound));
        when(roundRankingRepository.findByRound_RoundIdAndCategory_CategoryIdAndTeam_TeamId(
                previousRound.getRoundId(), categoryId, team.getTeamId()))
                .thenReturn(Optional.of(ranking));

        assertDoesNotThrow(() -> validateTeamAdvancedFromPreviousRound(team, laterRound));
    }

    @Test
    void laterRoundRejectsAdvancedRankingThatIsNotApproved() {
        UUID categoryId = UUID.randomUUID();
        Teams team = team(categoryId);
        Round previousRound = round(categoryId, 2, false);
        Round laterRound = round(categoryId, 4, false);
        RoundRanking ranking = ranking(true, false);

        when(roundRepository
                .findTopByCategoryCategoryIdAndRoundOrderLessThanAndIsCalibrationRoundFalseOrderByRoundOrderDesc(
                        categoryId,
                        laterRound.getRoundOrder()
                ))
                .thenReturn(Optional.of(previousRound));
        when(roundRankingRepository.findByRound_RoundIdAndCategory_CategoryIdAndTeam_TeamId(
                previousRound.getRoundId(), categoryId, team.getTeamId()))
                .thenReturn(Optional.of(ranking));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> validateTeamAdvancedFromPreviousRound(team, laterRound));

        assertEquals("Team has not advanced from the previous competition round", exception.getMessage());
    }

    @Test
    void roundAfterCalibrationIsAllowedWhenNoPreviousCompetitionRoundExists() {
        UUID categoryId = UUID.randomUUID();
        Teams team = team(categoryId);
        Round laterRound = round(categoryId, 2, false);

        when(roundRepository
                .findTopByCategoryCategoryIdAndRoundOrderLessThanAndIsCalibrationRoundFalseOrderByRoundOrderDesc(
                        categoryId,
                        laterRound.getRoundOrder()
                ))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> validateTeamAdvancedFromPreviousRound(team, laterRound));

        verify(roundRankingRepository, never())
                .findByRound_RoundIdAndCategory_CategoryIdAndTeam_TeamId(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void upsertSubmissionUpdatesExistingSubmissionWithoutStoredProcedure() {
        UUID teamId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        CreateSubmissionRequest request = submissionRequest(teamId, roundId);
        Submissions existing = new Submissions();
        existing.setSubmissionId(UUID.randomUUID());

        when(submissionsRepository.findByTeamIdAndRoundId(teamId, roundId))
                .thenReturn(Optional.of(existing));
        when(submissionsRepository.save(org.mockito.ArgumentMatchers.any(Submissions.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Submissions saved = upsertSubmission(request, currentUserId);

        assertEquals(existing.getSubmissionId(), saved.getSubmissionId());
        assertSubmissionMatchesRequest(saved, request, currentUserId);
        verify(submissionsRepository).save(existing);
    }

    @Test
    void upsertSubmissionCreatesNewSubmissionWhenNoneExists() {
        UUID teamId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        CreateSubmissionRequest request = submissionRequest(teamId, roundId);

        when(submissionsRepository.findByTeamIdAndRoundId(teamId, roundId))
                .thenReturn(Optional.empty());
        when(submissionsRepository.save(org.mockito.ArgumentMatchers.any(Submissions.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Submissions saved = upsertSubmission(request, currentUserId);

        assertSubmissionMatchesRequest(saved, request, currentUserId);
        verify(submissionsRepository).save(org.mockito.ArgumentMatchers.any(Submissions.class));
    }

    @Test
    void upsertSubmissionRejectsPreviouslyDisqualifiedSubmission() {
        UUID teamId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        CreateSubmissionRequest request = submissionRequest(teamId, roundId);
        Submissions existing = new Submissions();
        existing.setSubmissionId(UUID.randomUUID());
        existing.setSubmissionStatusId(SubmissionStatusConstants.DISQUALIFIED);

        when(submissionsRepository.findByTeamIdAndRoundId(teamId, roundId))
                .thenReturn(Optional.of(existing));

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> upsertSubmission(request, currentUserId)
        );

        assertEquals("This submission has been disqualified and cannot be updated", exception.getMessage());
        verify(submissionsRepository, never()).save(org.mockito.ArgumentMatchers.any(Submissions.class));
    }

    private void validateTeamAdvancedFromPreviousRound(Teams team, Round round) {
        ReflectionTestUtils.invokeMethod(service, "validateTeamAdvancedFromPreviousRound", team, round);
    }

    private Submissions upsertSubmission(CreateSubmissionRequest request, UUID currentUserId) {
        return ReflectionTestUtils.invokeMethod(service, "upsertSubmission", request, currentUserId);
    }

    private CreateSubmissionRequest submissionRequest(UUID teamId, UUID roundId) {
        CreateSubmissionRequest request = new CreateSubmissionRequest();
        request.setTeamId(teamId);
        request.setRoundId(roundId);
        request.setRepositoryUrl("https://github.com/example/api-alpha");
        request.setDemoUrl("https://example.test/demo");
        request.setReportUrl("https://example.test/report.pdf");
        request.setSlideUrl("https://example.test/slides");
        request.setNotes("Ready for live test.");
        return request;
    }

    private void assertSubmissionMatchesRequest(
            Submissions submission,
            CreateSubmissionRequest request,
            UUID currentUserId) {
        assertEquals(request.getTeamId(), submission.getTeamId());
        assertEquals(request.getRoundId(), submission.getRoundId());
        assertEquals(SubmissionStatusConstants.SUBMITTED, submission.getSubmissionStatusId());
        assertEquals(request.getRepositoryUrl(), submission.getRepositoryUrl());
        assertEquals(request.getDemoUrl(), submission.getDemoUrl());
        assertEquals(request.getReportUrl(), submission.getReportUrl());
        assertEquals(request.getSlideUrl(), submission.getSlideUrl());
        assertEquals(request.getNotes(), submission.getNotes());
        assertEquals(currentUserId, submission.getSubmittedByUserId());
        assertFalse(submission.getIsScoreApproved());
        assertFalse(submission.getIsSampleSubmission());
    }

    private Teams team(UUID categoryId) {
        Teams team = new Teams();
        team.setTeamId(UUID.randomUUID());
        team.setCategoryId(categoryId);
        return team;
    }

    private Round round(UUID categoryId, Integer roundOrder, boolean calibration) {
        Event event = new Event();
        event.setEventId(UUID.randomUUID());

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setEvent(event);

        Round round = new Round();
        round.setRoundId(UUID.randomUUID());
        round.setCategory(category);
        round.setRoundOrder(roundOrder);
        round.setIsCalibrationRound(calibration);
        return round;
    }

    private RoundRanking ranking(boolean advanced, boolean approved) {
        RoundRanking ranking = new RoundRanking();
        ranking.setIsAdvanced(advanced);
        ranking.setIsApproved(approved);
        return ranking;
    }
}
