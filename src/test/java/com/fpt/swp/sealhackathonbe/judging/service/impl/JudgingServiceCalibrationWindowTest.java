package com.fpt.swp.sealhackathonbe.judging.service.impl;

import com.fpt.swp.sealhackathonbe.auth.service.impl.AuthenticationServiceImpl;
import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.core.constant.SubmissionStatusConstants;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.judging.dto.ScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.repository.EvaluationAuditLogRepository;
import com.fpt.swp.sealhackathonbe.judging.repository.JudgingRepository;
import com.fpt.swp.sealhackathonbe.ranking.repository.RoundRankingRepository;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundJudgeResponse;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.entity.RoundCriterion;
import com.fpt.swp.sealhackathonbe.round.entity.RoundJudge;
import com.fpt.swp.sealhackathonbe.round.entity.RoundStatus;
import com.fpt.swp.sealhackathonbe.round.repository.RoundCriterionRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.round.service.RoundJudgeService;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cua so cham diem cua VONG HIEU CHUAN.
 *
 * Vong hieu chuan khong sinh ra ket qua thi nao nen co y KHONG bi bo gio: giam khao cham duoc
 * truoc startDate, sau judgingDeadline, va khi round chua o trang thai "Judging". Doi lai no
 * dong khi cuoc thi da thuc su vao cham.
 *
 * Test cuoi cung la BANG CHUNG REGRESSION: vong thi that van giu nguyen 3 lop kiem tra cu.
 */
@ExtendWith(MockitoExtension.class)
class JudgingServiceCalibrationWindowTest {

    @Mock private JudgingRepository judgingRepository;
    @Mock private SubmissionsRepository submissionRepository;
    @Mock private EvaluationAuditLogRepository evaluationAuditLogRepository;
    @Mock private RoundCriterionRepository roundCriterionRepository;
    @Mock private AuthenticationServiceImpl authenticationServiceImpl;
    @Mock private RoundJudgeService roundJudgeService;
    @Mock private RoundJudgeRepository roundJudgeRepository;
    @Mock private TeamMembersRepository teamMembersRepository;
    @Mock private RoundRankingRepository roundRankingRepository;
    @Mock private RoundRepository roundRepository;
    @Mock private com.fpt.swp.sealhackathonbe.notification.service.NotificationService notificationService;

    @InjectMocks
    private JudgingServiceImpl judgingService;

    private UUID submissionId;
    private UUID roundId;
    private UUID userId;
    private UUID criterionId;
    private UUID categoryId;

    private Category category;
    private Round calibrationRound;
    private Submissions sampleSubmission;
    private User actor;
    private RoundJudge roundJudge;
    private RoundCriterion criterion;

    @BeforeEach
    void setUp() {
        submissionId = UUID.randomUUID();
        roundId = UUID.randomUUID();
        userId = UUID.randomUUID();
        criterionId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        actor = new User();
        actor.setUserId(userId);
        actor.setFullName("Judge One");

        Event event = new Event();
        event.setEventId(UUID.randomUUID());

        category = new Category();
        category.setCategoryId(categoryId);
        category.setEvent(event);

        calibrationRound = new Round();
        calibrationRound.setRoundId(roundId);
        calibrationRound.setRoundName("Calibration");
        calibrationRound.setCategory(category);
        calibrationRound.setIsCalibrationRound(true);

        // Bai mau khong thuoc doi nao -> TeamID = null.
        sampleSubmission = new Submissions();
        sampleSubmission.setSubmissionId(submissionId);
        sampleSubmission.setRoundId(roundId);
        sampleSubmission.setTeam(null);
        sampleSubmission.setIsSampleSubmission(true);
        sampleSubmission.setSubmissionStatusId(SubmissionStatusConstants.SUBMITTED);
        sampleSubmission.setIsScoreApproved(false);

        roundJudge = new RoundJudge();
        roundJudge.setRoundJudgeId(UUID.randomUUID());
        roundJudge.setRound(calibrationRound);
        roundJudge.setJudge(actor);

        criterion = new RoundCriterion();
        criterion.setRoundCriterionId(criterionId);
        criterion.setCriterionName("Innovation");
        criterion.setMaxScore(BigDecimal.valueOf(10));
        criterion.setWeight(BigDecimal.ONE);

        lenient().when(roundRepository.findById(roundId)).thenReturn(Optional.of(calibrationRound));
    }

    private ScoreSubmissionDTO scoreDto() {
        ScoreSubmissionDTO dto = new ScoreSubmissionDTO();
        dto.setSubmissionId(submissionId);
        dto.setRoundCriterionId(criterionId);
        dto.setScoreValue(BigDecimal.valueOf(8));
        dto.setComment("ok");
        return dto;
    }

    /**
     * Mock toi ngay TRUOC buoc kiem tra cua so cham diem. Cac test "bi chan" chi can den day —
     * stub them nua se bi Mockito bao UnnecessaryStubbing vi luong da nem loi truoc do.
     */
    private void stubUpToWindowCheck() {
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(sampleSubmission));
        when(authenticationServiceImpl.getCurrentUser()).thenReturn(actor);
        when(roundJudgeService.getJudgesByRound(roundId))
                .thenReturn(List.of(RoundJudgeResponse.builder().judgeId(userId).build()));
        when(roundJudgeRepository.findByJudge_UserIdAndRound_RoundId(userId, roundId))
                .thenReturn(Optional.of(roundJudge));
    }

    /** Phan con lai cua duong ghi diem, chi can cho cac test di den cuoi. */
    private void stubScoreWriting() {
        when(roundCriterionRepository.findById(criterionId)).thenReturn(Optional.of(criterion));
        when(judgingRepository.findBySubmission_SubmissionIdAndRoundJudge_RoundJudgeIdAndRoundCriterion_RoundCriterionId(
                submissionId, roundJudge.getRoundJudgeId(), criterionId)).thenReturn(Optional.empty());
    }

    private void stubHappyPath() {
        stubUpToWindowCheck();
        stubScoreWriting();
    }

    private Round competitionRound(String statusName) {
        RoundStatus status = new RoundStatus();
        status.setStatusName(statusName);
        Round round = new Round();
        round.setRoundId(UUID.randomUUID());
        round.setRoundName("Qualifier");
        round.setCategory(category);
        round.setIsCalibrationRound(false);
        round.setRoundStatus(status);
        return round;
    }

    private void setCalibrationStatus(String statusName) {
        RoundStatus status = new RoundStatus();
        status.setStatusName(statusName);
        calibrationRound.setRoundStatus(status);
    }

    @Test
    void allowsScoringBeforeTheCalibrationRoundHasStarted() {
        calibrationRound.setStartDate(LocalDateTime.now().plusDays(3));
        calibrationRound.setJudgingDeadline(LocalDateTime.now().plusDays(5));
        setCalibrationStatus("Upcoming");
        stubHappyPath();
        when(roundRepository.findByCategoryCategoryIdOrderByRoundOrderAsc(categoryId))
                .thenReturn(List.of(calibrationRound));

        assertDoesNotThrow(() -> judgingService.recordJudging(List.of(scoreDto())));
        verify(judgingRepository).saveAll(anyList());
    }

    @Test
    void allowsScoringAfterTheCalibrationJudgingDeadlineHasPassed() {
        calibrationRound.setStartDate(LocalDateTime.now().minusDays(10));
        calibrationRound.setJudgingDeadline(LocalDateTime.now().minusDays(5));
        setCalibrationStatus("Submission Open");
        stubHappyPath();
        when(roundRepository.findByCategoryCategoryIdOrderByRoundOrderAsc(categoryId))
                .thenReturn(List.of(calibrationRound));

        assertDoesNotThrow(() -> judgingService.recordJudging(List.of(scoreDto())));
        verify(judgingRepository).saveAll(anyList());
    }

    @Test
    void blocksScoringOnceACompetitionRoundEntersJudging() {
        setCalibrationStatus("Submission Open");
        stubUpToWindowCheck();
        when(roundRepository.findByCategoryCategoryIdOrderByRoundOrderAsc(categoryId))
                .thenReturn(List.of(calibrationRound, competitionRound("Judging")));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> judgingService.recordJudging(List.of(scoreDto())));
        assertTrue(exception.getMessage().contains("Calibration scoring is closed"));
    }

    @Test
    void blocksScoringOnceACompetitionRoundIsCompleted() {
        setCalibrationStatus("Submission Open");
        stubUpToWindowCheck();
        when(roundRepository.findByCategoryCategoryIdOrderByRoundOrderAsc(categoryId))
                .thenReturn(List.of(calibrationRound, competitionRound("Completed")));

        assertThrows(IllegalStateException.class, () -> judgingService.recordJudging(List.of(scoreDto())));
    }

    @Test
    void blocksScoringWhenOrganizerClosesTheCalibrationRound() {
        setCalibrationStatus("Completed");
        stubUpToWindowCheck();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> judgingService.recordJudging(List.of(scoreDto())));
        assertTrue(exception.getMessage().contains("closed by the organizer"));
    }

    /**
     * REGRESSION: noi long o tren chi danh cho vong hieu chuan. Vong thi that qua han cham van
     * phai bi chan — day la rang buoc dam bao cong bang giua cac doi, khong duoc noi long.
     */
    @Test
    void competitionRoundStillRejectsScoringAfterItsJudgingDeadline() {
        calibrationRound.setIsCalibrationRound(false);
        calibrationRound.setStartDate(LocalDateTime.now().minusDays(10));
        calibrationRound.setJudgingDeadline(LocalDateTime.now().minusDays(1));
        setCalibrationStatus("Judging");

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(sampleSubmission));
        when(authenticationServiceImpl.getCurrentUser()).thenReturn(actor);
        when(roundJudgeService.getJudgesByRound(roundId))
                .thenReturn(List.of(RoundJudgeResponse.builder().judgeId(userId).build()));
        when(roundJudgeRepository.findByJudge_UserIdAndRound_RoundId(userId, roundId))
                .thenReturn(Optional.of(roundJudge));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> judgingService.recordJudging(List.of(scoreDto())));
        assertTrue(exception.getMessage().contains("judging deadline"));
    }

    /** REGRESSION: vong thi that chua toi gio bat dau van bi chan. */
    @Test
    void competitionRoundStillRejectsScoringBeforeItStarts() {
        calibrationRound.setIsCalibrationRound(false);
        calibrationRound.setStartDate(LocalDateTime.now().plusDays(2));
        setCalibrationStatus("Judging");

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(sampleSubmission));
        when(authenticationServiceImpl.getCurrentUser()).thenReturn(actor);
        when(roundJudgeService.getJudgesByRound(roundId))
                .thenReturn(List.of(RoundJudgeResponse.builder().judgeId(userId).build()));
        when(roundJudgeRepository.findByJudge_UserIdAndRound_RoundId(userId, roundId))
                .thenReturn(Optional.of(roundJudge));

        assertThrows(IllegalStateException.class, () -> judgingService.recordJudging(List.of(scoreDto())));
    }

    /** REGRESSION: vong thi that chua o trang thai Judging van bi chan. */
    @Test
    void competitionRoundStillRejectsScoringOutsideJudgingStatus() {
        calibrationRound.setIsCalibrationRound(false);
        calibrationRound.setStartDate(LocalDateTime.now().minusDays(1));
        calibrationRound.setJudgingDeadline(LocalDateTime.now().plusDays(1));
        setCalibrationStatus("Submission Open");

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(sampleSubmission));
        when(authenticationServiceImpl.getCurrentUser()).thenReturn(actor);
        when(roundJudgeService.getJudgesByRound(roundId))
                .thenReturn(List.of(RoundJudgeResponse.builder().judgeId(userId).build()));
        when(roundJudgeRepository.findByJudge_UserIdAndRound_RoundId(userId, roundId))
                .thenReturn(Optional.of(roundJudge));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> judgingService.recordJudging(List.of(scoreDto())));
        assertTrue(exception.getMessage().contains("'Judging' phase"));
    }

    /** Diem cham o vong hieu chuan phai duoc danh dau IsCalibration = true do SERVER quyet dinh. */
    @Test
    void marksScoresAsCalibrationBasedOnTheRoundNotTheClientPayload() {
        setCalibrationStatus("Upcoming");
        stubHappyPath();
        when(roundRepository.findByCategoryCategoryIdOrderByRoundOrderAsc(categoryId))
                .thenReturn(List.of(calibrationRound));

        ScoreSubmissionDTO dto = scoreDto();
        dto.setIsCalibration(false); // client noi doi

        judgingService.recordJudging(List.of(dto));

        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<List<com.fpt.swp.sealhackathonbe.judging.entity.Judging>> captor =
                org.mockito.ArgumentCaptor.forClass(List.class);
        verify(judgingRepository).saveAll(captor.capture());
        assertTrue(captor.getValue().get(0).getIsCalibration());
    }

    @Test
    void rejectsScoringWhenActorIsNotAssignedToTheRound() {
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(sampleSubmission));
        when(authenticationServiceImpl.getCurrentUser()).thenReturn(actor);
        when(roundJudgeService.getJudgesByRound(roundId))
                .thenReturn(List.of(RoundJudgeResponse.builder().judgeId(UUID.randomUUID()).build()));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> judgingService.recordJudging(List.of(scoreDto())));
        verify(judgingRepository, org.mockito.Mockito.never()).saveAll(any());
    }
}
