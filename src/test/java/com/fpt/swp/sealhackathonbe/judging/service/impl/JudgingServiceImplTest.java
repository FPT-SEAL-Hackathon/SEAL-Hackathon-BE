package com.fpt.swp.sealhackathonbe.judging.service.impl;

import com.fpt.swp.sealhackathonbe.auth.service.impl.AuthenticationServiceImpl;
import com.fpt.swp.sealhackathonbe.core.constant.SubmissionStatusConstants;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.judging.dto.JudgingDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.ScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.UpdateScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.entity.EvaluationAuditLog;
import com.fpt.swp.sealhackathonbe.judging.entity.Judging;
import com.fpt.swp.sealhackathonbe.judging.repository.EvaluationAuditLogRepository;
import com.fpt.swp.sealhackathonbe.judging.repository.JudgingRepository;
import com.fpt.swp.sealhackathonbe.ranking.entity.RoundRanking;
import com.fpt.swp.sealhackathonbe.ranking.repository.RoundRankingRepository;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundJudgeResponse;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.entity.RoundCriterion;
import com.fpt.swp.sealhackathonbe.round.entity.RoundJudge;
import com.fpt.swp.sealhackathonbe.round.entity.RoundStatus;
import com.fpt.swp.sealhackathonbe.round.repository.RoundCriterionRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.round.service.RoundJudgeService;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JudgingServiceImplTest {

    @Mock
    private JudgingRepository judgingRepository;
    @Mock
    private SubmissionsRepository submissionRepository;
    @Mock
    private EvaluationAuditLogRepository evaluationAuditLogRepository;
    @Mock
    private RoundCriterionRepository roundCriterionRepository;
    @Mock
    private AuthenticationServiceImpl authenticationServiceImpl;
    @Mock
    private RoundJudgeService roundJudgeService;
    @Mock
    private RoundJudgeRepository roundJudgeRepository;
    @Mock
    private TeamMembersRepository teamMembersRepository;
    @Mock
    private RoundRankingRepository roundRankingRepository;
    @Mock
    private RoundRepository roundRepository;

    @InjectMocks
    private JudgingServiceImpl judgingService;

    private UUID submissionId;
    private UUID roundId;
    private UUID userId;
    private UUID criterionId;
    private UUID teamId;
    private UUID categoryId;
    private UUID eventId;

    private Submissions submission;
    private User actor;
    private RoundJudge roundJudge;
    private Round round;
    private RoundStatus roundStatus;
    private Teams team;
    private Event event;
    private RoundCriterion criterion;

    @BeforeEach
    void setUp() {
        submissionId = UUID.randomUUID();
        roundId = UUID.randomUUID();
        userId = UUID.randomUUID();
        criterionId = UUID.randomUUID();
        teamId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        eventId = UUID.randomUUID();

        actor = new User();
        actor.setUserId(userId);
        actor.setFullName("John Doe");

        event = new Event();
        event.setEventId(eventId);

        com.fpt.swp.sealhackathonbe.category.entity.Category category = new com.fpt.swp.sealhackathonbe.category.entity.Category();
        category.setCategoryId(categoryId);
        category.setEvent(event);

        roundStatus = new RoundStatus();
        roundStatus.setStatusName("Judging");

        round = new Round();
        round.setRoundId(roundId);
        round.setStartDate(LocalDateTime.now().minusDays(1));
        round.setJudgingDeadline(LocalDateTime.now().plusDays(1));
        round.setRoundStatus(roundStatus);
        round.setCategory(category);

        team = new Teams();
        team.setTeamId(teamId);
        team.setCategoryId(categoryId);
        team.setEvent(event); // Trích xuất context Event trực tiếp từ team

        submission = new Submissions();
        submission.setSubmissionId(submissionId);
        submission.setRoundId(roundId);
        submission.setTeam(team);
        submission.setSubmissionStatusId(SubmissionStatusConstants.SUBMITTED);
        submission.setIsScoreApproved(false);

        roundJudge = new RoundJudge();
        roundJudge.setRoundJudgeId(UUID.randomUUID());
        roundJudge.setRound(round);
        roundJudge.setJudge(actor);

        criterion = new RoundCriterion();
        criterion.setRoundCriterionId(criterionId);
        criterion.setCriterionName("Criteria 1");
        criterion.setMaxScore(BigDecimal.valueOf(10));
        criterion.setWeight(BigDecimal.ONE);

        lenient().when(roundRepository.findById(any())).thenReturn(Optional.of(round));
    }

    @Test
    void testRecordJudging_Success() {
        // Arrange
        ScoreSubmissionDTO dto = new ScoreSubmissionDTO();
        dto.setSubmissionId(submissionId);
        dto.setRoundCriterionId(criterionId);
        dto.setScoreValue(BigDecimal.valueOf(8.5));
        dto.setComment("Good job");
        dto.setIsCalibration(false);

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(authenticationServiceImpl.getCurrentUser()).thenReturn(actor);

        RoundJudgeResponse judgeResponse = RoundJudgeResponse.builder().judgeId(userId).build();
        when(roundJudgeService.getJudgesByRound(roundId)).thenReturn(List.of(judgeResponse));
        when(roundJudgeRepository.findByJudge_UserIdAndRound_RoundId(userId, roundId)).thenReturn(Optional.of(roundJudge));
        when(roundCriterionRepository.findById(criterionId)).thenReturn(Optional.of(criterion));
        when(judgingRepository.findBySubmission_SubmissionIdAndRoundJudge_RoundJudgeIdAndRoundCriterion_RoundCriterionId(
                submissionId, roundJudge.getRoundJudgeId(), criterionId)).thenReturn(Optional.empty());

        // Act
        assertDoesNotThrow(() -> judgingService.recordJudging(List.of(dto)));

        // Assert
        verify(judgingRepository).saveAll(anyList());
        verify(evaluationAuditLogRepository).saveAll(anyList());
        verify(submissionRepository).save(submission);
        assertEquals(SubmissionStatusConstants.IN_PROGRESS, submission.getSubmissionStatusId());
    }

    @Test
    void testRecordJudging_ScoreExceedsMax() {
        // Arrange
        ScoreSubmissionDTO dto = new ScoreSubmissionDTO();
        dto.setSubmissionId(submissionId);
        dto.setRoundCriterionId(criterionId);
        dto.setScoreValue(BigDecimal.valueOf(12)); // Vượt quá max score (10)
        dto.setComment("Too good");

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(authenticationServiceImpl.getCurrentUser()).thenReturn(actor);

        RoundJudgeResponse judgeResponse = RoundJudgeResponse.builder().judgeId(userId).build();
        when(roundJudgeService.getJudgesByRound(roundId)).thenReturn(List.of(judgeResponse));
        when(roundJudgeRepository.findByJudge_UserIdAndRound_RoundId(userId, roundId)).thenReturn(Optional.of(roundJudge));
        when(roundCriterionRepository.findById(criterionId)).thenReturn(Optional.of(criterion));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                judgingService.recordJudging(List.of(dto)));
        assertTrue(exception.getMessage().contains("exceeds the maximum allowed value"));
    }

    @Test
    void testRecordJudging_ScoreAlreadyExists() {
        // Arrange
        ScoreSubmissionDTO dto = new ScoreSubmissionDTO();
        dto.setSubmissionId(submissionId);
        dto.setRoundCriterionId(criterionId);
        dto.setScoreValue(BigDecimal.valueOf(8));

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(authenticationServiceImpl.getCurrentUser()).thenReturn(actor);

        RoundJudgeResponse judgeResponse = RoundJudgeResponse.builder().judgeId(userId).build();
        when(roundJudgeService.getJudgesByRound(roundId)).thenReturn(List.of(judgeResponse));
        when(roundJudgeRepository.findByJudge_UserIdAndRound_RoundId(userId, roundId)).thenReturn(Optional.of(roundJudge));
        when(roundCriterionRepository.findById(criterionId)).thenReturn(Optional.of(criterion));

        Judging existingJudging = new Judging();
        existingJudging.setIsActive(true);
        when(judgingRepository.findBySubmission_SubmissionIdAndRoundJudge_RoundJudgeIdAndRoundCriterion_RoundCriterionId(
                submissionId, roundJudge.getRoundJudgeId(), criterionId)).thenReturn(Optional.of(existingJudging));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                judgingService.recordJudging(List.of(dto)));
        assertTrue(exception.getMessage().contains("already exists. Please use the update API."));
    }

    @Test
    void testUpdateJudging_Success() {
        // Arrange
        UUID judgingId = UUID.randomUUID();
        UpdateScoreSubmissionDTO dto = new UpdateScoreSubmissionDTO();
        dto.setJudgingId(judgingId);
        dto.setScoreValue(BigDecimal.valueOf(9.0));
        dto.setComment("Updated score");
        dto.setReason("Correction");

        when(authenticationServiceImpl.getCurrentUser()).thenReturn(actor);

        Judging existingJudging = new Judging();
        existingJudging.setId(judgingId);
        existingJudging.setRoundJudge(roundJudge);
        existingJudging.setRoundCriterion(criterion);
        existingJudging.setSubmission(submission);
        existingJudging.setScoreValue(BigDecimal.valueOf(8.0));
        existingJudging.setComment("Old comment");

        when(judgingRepository.findById(judgingId)).thenReturn(Optional.of(existingJudging));

        // Act
        assertDoesNotThrow(() -> judgingService.updateJudging(List.of(dto)));

        // Assert
        verify(judgingRepository).saveAll(anyList());
        verify(evaluationAuditLogRepository).saveAll(anyList());
        assertEquals(BigDecimal.valueOf(9.0), existingJudging.getScoreValue());
        assertEquals("Updated score", existingJudging.getComment());
    }

    @Test
    void testGetPublishedScoresBySubmission_Success() {
        // Arrange
        when(authenticationServiceImpl.getCurrentUser()).thenReturn(actor);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));

        TeamMembers teamMember = new TeamMembers();
        teamMember.setUserId(userId);
        teamMember.setTeamId(teamId);
        teamMember.setActive(true);
        when(teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, userId)).thenReturn(Optional.of(teamMember));

        RoundRanking roundRanking = new RoundRanking();
        roundRanking.setIsPublished(true);
        when(roundRankingRepository.findByRound_RoundIdAndCategory_CategoryIdAndTeam_TeamId(
                roundId, categoryId, teamId)).thenReturn(Optional.of(roundRanking));

        Judging judging = new Judging();
        judging.setIsActive(true);
        judging.setRoundJudge(roundJudge);
        judging.setRoundCriterion(criterion);
        judging.setScoreValue(BigDecimal.valueOf(8.5));
        judging.setComment("Well done");
        when(judgingRepository.findBySubmission_SubmissionIdIn(anyList())).thenReturn(List.of(judging));

        // Act
        List<JudgingDTO> result = judgingService.getPublishedScoresBySubmission(submissionId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Judge 1", result.get(0).getJudgeName());
    }

    @Test
    void testRejectSubmissionScores_Success() {
        // Arrange
        when(authenticationServiceImpl.getCurrentUser()).thenReturn(actor);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));

        Judging judging = new Judging();
        judging.setIsActive(true);
        when(judgingRepository.findBySubmission_SubmissionId(submissionId)).thenReturn(List.of(judging));

        // Act
        assertDoesNotThrow(() -> judgingService.rejectSubmissionScores(submissionId, "Scores are incorrect"));

        // Assert
        assertFalse(judging.getIsActive());
        assertFalse(submission.getIsScoreApproved());
        assertEquals(SubmissionStatusConstants.IN_PROGRESS, submission.getSubmissionStatusId());
        verify(judgingRepository).saveAll(anyList());
        verify(evaluationAuditLogRepository).saveAll(anyList());
        verify(submissionRepository).save(submission);
    }

    @Test
    void testRejectSubmissionScoreForJudge_Success() {
        // Arrange
        when(authenticationServiceImpl.getCurrentUser()).thenReturn(actor);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));

        Judging judging = new Judging();
        judging.setIsActive(true);
        when(judgingRepository.findBySubmission_SubmissionIdAndRoundJudge_Judge_UserId(submissionId, userId))
                .thenReturn(List.of(judging));

        // Act
        assertDoesNotThrow(() -> judgingService.rejectSubmissionScoreForJudge(submissionId, userId, "Incorrect scoring"));

        // Assert
        assertFalse(judging.getIsActive());
        assertFalse(submission.getIsScoreApproved());
        assertEquals(SubmissionStatusConstants.IN_PROGRESS, submission.getSubmissionStatusId());
        verify(judgingRepository, atLeastOnce()).saveAll(anyList());
        verify(evaluationAuditLogRepository, atLeastOnce()).saveAll(anyList());
        verify(submissionRepository, atLeastOnce()).save(submission);
    }

    @Test
    void testRejectJudgeScoresInRound_Success() {
        // Arrange
        when(authenticationServiceImpl.getCurrentUser()).thenReturn(actor);

        Judging judging = new Judging();
        judging.setIsActive(true);
        judging.setSubmission(submission);
        when(judgingRepository.findActiveByRoundIdAndJudgeUserId(roundId, userId))
                .thenReturn(List.of(judging));

        // Act
        assertDoesNotThrow(() -> judgingService.rejectJudgeScoresInRound(roundId, userId, "Judge bias"));

        // Assert
        assertFalse(judging.getIsActive());
        assertFalse(submission.getIsScoreApproved());
        assertEquals(SubmissionStatusConstants.IN_PROGRESS, submission.getSubmissionStatusId());
        verify(judgingRepository, atLeastOnce()).saveAll(anyList());
        verify(evaluationAuditLogRepository, atLeastOnce()).saveAll(anyList());
        verify(submissionRepository, atLeastOnce()).saveAll(anySet());
    }

    @Test
    void testDeleteJudging_Success() {
        // Arrange
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(authenticationServiceImpl.getCurrentUser()).thenReturn(actor);
        when(roundJudgeRepository.findByJudge_UserIdAndRound_RoundId(userId, roundId)).thenReturn(Optional.of(roundJudge));

        Judging judging = new Judging();
        judging.setIsActive(true);
        judging.setSubmission(submission);
        judging.setScoreValue(BigDecimal.valueOf(7.0));
        when(judgingRepository.findBySubmission_SubmissionIdAndRoundJudge_Judge_UserId(submissionId, userId))
                .thenReturn(List.of(judging));

        // Act
        assertDoesNotThrow(() -> judgingService.deleteJudging(submissionId, "Reset score"));

        // Assert
        assertFalse(judging.getIsActive());
        verify(judgingRepository).saveAll(anyList());
        verify(evaluationAuditLogRepository).saveAll(anyList());
    }

    @Test
    void testApproveScore_NotApproved_Success() {
        // Arrange
        submission.setIsScoreApproved(false);
        submission.setIsSampleSubmission(false);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(submissionRepository.save(any(Submissions.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SubmissionResponse response = judgingService.approveScore(submissionId, false);

        // Assert
        assertNotNull(response);
        assertFalse(response.getIsScoreApproved());
        assertEquals(SubmissionStatusConstants.IN_PROGRESS, response.getSubmissionStatusId());
        verify(submissionRepository).save(submission);
    }

    @Test
    void testApproveScore_ApproveSampleSubmission_ThrowsIllegalStateException() {
        // Arrange
        submission.setIsSampleSubmission(true);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                judgingService.approveScore(submissionId, true));
        assertEquals("Cannot finalize score for a sample calibration submission.", ex.getMessage());
    }

    @Test
    void testApproveScore_ApproveNoCriteria_ThrowsIllegalStateException() {
        // Arrange
        submission.setIsSampleSubmission(false);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(roundCriterionRepository.findByRoundRoundIdOrderBySortOrderAsc(roundId)).thenReturn(Collections.emptyList());

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                judgingService.approveScore(submissionId, true));
        assertEquals("Cannot finalize score because no criteria are configured for this round.", ex.getMessage());
    }

    @Test
    void testApproveScore_ApproveNoJudges_ThrowsIllegalStateException() {
        // Arrange
        submission.setIsSampleSubmission(false);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(roundCriterionRepository.findByRoundRoundIdOrderBySortOrderAsc(roundId)).thenReturn(List.of(criterion));
        when(roundJudgeRepository.findActiveByRoundRoundId(roundId)).thenReturn(Collections.emptyList());

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                judgingService.approveScore(submissionId, true));
        assertEquals("Cannot finalize score because no active judges are assigned to this round.", ex.getMessage());
    }

    @Test
    void testApproveScore_ApproveNoScores_ThrowsIllegalStateException() {
        // Arrange
        submission.setIsSampleSubmission(false);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(roundCriterionRepository.findByRoundRoundIdOrderBySortOrderAsc(roundId)).thenReturn(List.of(criterion));
        when(roundJudgeRepository.findActiveByRoundRoundId(roundId)).thenReturn(List.of(roundJudge));
        when(judgingRepository.findBySubmission_SubmissionId(submissionId)).thenReturn(Collections.emptyList());

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                judgingService.approveScore(submissionId, true));
        assertEquals("Cannot finalize score because no scores have been submitted yet.", ex.getMessage());
    }

    @Test
    void testApproveScore_ApproveJudgeNotScored_ThrowsIllegalStateException() {
        // Arrange
        submission.setIsSampleSubmission(false);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(roundCriterionRepository.findByRoundRoundIdOrderBySortOrderAsc(roundId)).thenReturn(List.of(criterion));

        User judgeUser = new User();
        judgeUser.setUserId(userId);
        judgeUser.setFullName("Judge One");
        RoundJudge activeJudge = new RoundJudge();
        activeJudge.setJudge(judgeUser);

        when(roundJudgeRepository.findActiveByRoundRoundId(roundId)).thenReturn(List.of(activeJudge));

        // Score only for a different judge
        UUID otherJudgeId = UUID.randomUUID();
        User otherJudgeUser = new User();
        otherJudgeUser.setUserId(otherJudgeId);
        RoundJudge otherJudge = new RoundJudge();
        otherJudge.setJudge(otherJudgeUser);

        Judging score = new Judging();
        score.setIsActive(true);
        score.setRoundJudge(otherJudge);

        when(judgingRepository.findBySubmission_SubmissionId(submissionId)).thenReturn(List.of(score));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                judgingService.approveScore(submissionId, true));
        assertTrue(ex.getMessage().contains("Cannot finalize score because no judge has fully scored all criteria for this submission."));
    }

    @Test
    void testApproveScore_ApproveJudgeScoredIncomplete_ThrowsIllegalStateException() {
        // Arrange
        submission.setIsSampleSubmission(false);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));

        RoundCriterion criterion2 = new RoundCriterion();
        when(roundCriterionRepository.findByRoundRoundIdOrderBySortOrderAsc(roundId)).thenReturn(List.of(criterion, criterion2));

        User judgeUser = new User();
        judgeUser.setUserId(userId);
        judgeUser.setFullName("Judge One");
        RoundJudge activeJudge = new RoundJudge();
        activeJudge.setJudge(judgeUser);

        when(roundJudgeRepository.findActiveByRoundRoundId(roundId)).thenReturn(List.of(activeJudge));

        // Only 1 score instead of 2
        Judging score = new Judging();
        score.setIsActive(true);
        score.setRoundJudge(activeJudge);

        when(judgingRepository.findBySubmission_SubmissionId(submissionId)).thenReturn(List.of(score));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                judgingService.approveScore(submissionId, true));
        assertTrue(ex.getMessage().contains("Cannot finalize score because no judge has fully scored all criteria for this submission."));
    }

    @Test
    void testApproveScore_ApproveSuccess() {
        // Arrange
        submission.setIsSampleSubmission(false);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(roundCriterionRepository.findByRoundRoundIdOrderBySortOrderAsc(roundId)).thenReturn(List.of(criterion));

        User judgeUser = new User();
        judgeUser.setUserId(userId);
        judgeUser.setFullName("Judge One");
        RoundJudge activeJudge = new RoundJudge();
        activeJudge.setJudge(judgeUser);

        when(roundJudgeRepository.findActiveByRoundRoundId(roundId)).thenReturn(List.of(activeJudge));

        Judging score = new Judging();
        score.setIsActive(true);
        score.setRoundJudge(activeJudge);

        when(judgingRepository.findBySubmission_SubmissionId(submissionId)).thenReturn(List.of(score));
        when(submissionRepository.save(any(Submissions.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SubmissionResponse response = judgingService.approveScore(submissionId, true);

        // Assert
        assertNotNull(response);
        assertTrue(response.getIsScoreApproved());
        assertEquals(SubmissionStatusConstants.SCORED, response.getSubmissionStatusId());
    }

    @Test
    void testApproveScore_ApproveSuccess_MultipleJudgesOneScored() {
        // Arrange
        submission.setIsSampleSubmission(false);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(roundCriterionRepository.findByRoundRoundIdOrderBySortOrderAsc(roundId)).thenReturn(List.of(criterion));

        User judgeUser1 = new User();
        judgeUser1.setUserId(userId);
        judgeUser1.setFullName("Judge One");
        RoundJudge activeJudge1 = new RoundJudge();
        activeJudge1.setJudge(judgeUser1);

        User judgeUser2 = new User();
        judgeUser2.setUserId(UUID.randomUUID());
        judgeUser2.setFullName("Judge Two");
        RoundJudge activeJudge2 = new RoundJudge();
        activeJudge2.setJudge(judgeUser2);

        // Two active judges assigned to round
        when(roundJudgeRepository.findActiveByRoundRoundId(roundId)).thenReturn(List.of(activeJudge1, activeJudge2));

        // Only Judge One scored
        Judging score = new Judging();
        score.setIsActive(true);
        score.setRoundJudge(activeJudge1);

        when(judgingRepository.findBySubmission_SubmissionId(submissionId)).thenReturn(List.of(score));
        when(submissionRepository.save(any(Submissions.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SubmissionResponse response = judgingService.approveScore(submissionId, true);

        // Assert
        assertNotNull(response);
        assertTrue(response.getIsScoreApproved());
        assertEquals(SubmissionStatusConstants.SCORED, response.getSubmissionStatusId());
        verify(submissionRepository).save(submission);
    }

    @Test
    void testApproveScore_DisqualifiedSubmission_ThrowsBusinessConflictException() {
        submission.setSubmissionStatusId(SubmissionStatusConstants.DISQUALIFIED);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));

        BusinessConflictException ex = assertThrows(BusinessConflictException.class, () ->
                judgingService.approveScore(submissionId, true));
        assertEquals("Disqualified submissions cannot have scores approved or rejected", ex.getMessage());
    }

    @Test
    void testApproveScore_DisqualifiedTeam_ThrowsBusinessConflictException() {
        submission.setSubmissionStatusId(SubmissionStatusConstants.SUBMITTED);
        Teams teamEntity = new Teams();
        teamEntity.setTeamStatusId(TeamStatusConstants.DISQUALIFIED);
        submission.setTeam(teamEntity);

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));

        BusinessConflictException ex = assertThrows(BusinessConflictException.class, () ->
                judgingService.approveScore(submissionId, true));
        assertEquals("Submissions from disqualified or withdrawn teams cannot have scores approved or rejected", ex.getMessage());
    }

    @Test
    void testApproveScore_WithdrawnTeam_ThrowsBusinessConflictException() {
        submission.setSubmissionStatusId(SubmissionStatusConstants.SUBMITTED);
        Teams teamEntity = new Teams();
        teamEntity.setTeamStatusId(TeamStatusConstants.WITHDRAWN);
        submission.setTeam(teamEntity);

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));

        BusinessConflictException ex = assertThrows(BusinessConflictException.class, () ->
                judgingService.approveScore(submissionId, true));
        assertEquals("Submissions from disqualified or withdrawn teams cannot have scores approved or rejected", ex.getMessage());
    }
}
