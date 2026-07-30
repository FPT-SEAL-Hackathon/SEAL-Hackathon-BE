package com.fpt.swp.sealhackathonbe.round.service.impl;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.judging.repository.JudgingRepository;
import com.fpt.swp.sealhackathonbe.ranking.repository.RoundRankingRepository;
import com.fpt.swp.sealhackathonbe.round.dto.request.CreateRoundRequest;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.entity.RoundJudge;
import com.fpt.swp.sealhackathonbe.round.entity.RoundStatus;
import com.fpt.swp.sealhackathonbe.round.repository.RoundCriterionRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundStatusRepository;
import com.fpt.swp.sealhackathonbe.round.service.mapper.RoundMapper;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionHistoryRepository;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoundServiceValidationTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private RoundRepository roundRepository;
    @Mock
    private RoundStatusRepository roundStatusRepository;
    @Mock
    private RoundJudgeRepository roundJudgeRepository;
    @Mock
    private JudgingRepository judgingRepository;
    @Mock
    private RoundCriterionRepository roundCriterionRepository;
    @Mock
    private SubmissionsRepository submissionsRepository;
    @Mock
    private SubmissionHistoryRepository submissionHistoryRepository;
    @Mock
    private RoundRankingRepository roundRankingRepository;
    @Mock
    private RoundMapper roundMapper;

    @InjectMocks
    private RoundServiceImpl roundService;

    private UUID categoryId;
    private UUID roundStatusId;
    private Event event;
    private Category category;
    private RoundStatus roundStatus;
    private CreateRoundRequest request;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        roundStatusId = UUID.randomUUID();

        event = new Event();
        // Event boundary 1 day: 25/07/2026 00:00 -> 25/07/2026 cuoi ngay.
        // eventStartDate/eventEndDate la LocalDateTime, nen mot event "tron 1 ngay" phai dung
        // atStartOfDay()/atTime(MAX); de nguyen 00:00 o ca hai dau se khien moi round deu invalid.
        event.setEventStartDate(LocalDate.of(2026, 7, 25).atStartOfDay());
        event.setEventEndDate(LocalDate.of(2026, 7, 25).atTime(LocalTime.MAX));

        category = new Category();
        category.setCategoryId(categoryId);
        category.setEvent(event);

        roundStatus = new RoundStatus();
        roundStatus.setStatusId(roundStatusId);
        roundStatus.setStatusName("Upcoming");

        request = new CreateRoundRequest();
        request.setRoundStatusId(roundStatusId);
        request.setRoundName("Test Round");
    }

    @Test
    void testValidRoundInEvent() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(roundStatusRepository.findById(roundStatusId)).thenReturn(Optional.of(roundStatus));
        when(roundRepository.findMaxRoundOrderByCategory(categoryId)).thenReturn(0);
        when(roundRepository.save(any())).thenReturn(null);

        // Valid round within 25/07/2026
        request.setStartDate(LocalDateTime.of(2026, 7, 25, 8, 0));
        request.setEndDate(LocalDateTime.of(2026, 7, 25, 17, 0));
        
        assertDoesNotThrow(() -> roundService.create(categoryId, request));
    }

    @Test
    void testEqualStartEndDate_ThrowsBadRequest() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(roundStatusRepository.findById(roundStatusId)).thenReturn(Optional.of(roundStatus));

        // startDate == endDate
        LocalDateTime sameTime = LocalDateTime.of(2026, 7, 25, 8, 0);
        request.setStartDate(sameTime);
        request.setEndDate(sameTime);
        
        assertThrows(BadRequestException.class, () -> roundService.create(categoryId, request));
    }

    @Test
    void testStartDateAfterEndDate_ThrowsBadRequest() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(roundStatusRepository.findById(roundStatusId)).thenReturn(Optional.of(roundStatus));

        // startDate > endDate
        request.setStartDate(LocalDateTime.of(2026, 7, 25, 17, 0));
        request.setEndDate(LocalDateTime.of(2026, 7, 25, 8, 0));
        
        assertThrows(BadRequestException.class, () -> roundService.create(categoryId, request));
    }

    @Test
    void testRoundStartsBeforeEvent_ThrowsBadRequest() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(roundStatusRepository.findById(roundStatusId)).thenReturn(Optional.of(roundStatus));

        // Event starts 25/07, round starts 24/07 23:59
        request.setStartDate(LocalDateTime.of(2026, 7, 24, 23, 59));
        request.setEndDate(LocalDateTime.of(2026, 7, 25, 10, 0));
        
        assertThrows(BadRequestException.class, () -> roundService.create(categoryId, request));
    }

    @Test
    void testRoundEndsAfterEvent_ThrowsBadRequest() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(roundStatusRepository.findById(roundStatusId)).thenReturn(Optional.of(roundStatus));

        // Event ends 25/07, round ends 26/07 00:00
        request.setStartDate(LocalDateTime.of(2026, 7, 25, 8, 0));
        request.setEndDate(LocalDateTime.of(2026, 7, 26, 0, 0));
        
        assertThrows(BadRequestException.class, () -> roundService.create(categoryId, request));
    }

    @Test
    void testDeadlinesOutsideRound_ThrowsBadRequest() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(roundStatusRepository.findById(roundStatusId)).thenReturn(Optional.of(roundStatus));

        // Round: 25/07 08:00 - 17:00
        request.setStartDate(LocalDateTime.of(2026, 7, 25, 8, 0));
        request.setEndDate(LocalDateTime.of(2026, 7, 25, 17, 0));
        
        // Submission deadline outside (before start)
        request.setSubmissionDeadline(LocalDateTime.of(2026, 7, 25, 7, 59));
        assertThrows(BadRequestException.class, () -> roundService.create(categoryId, request));
    }

    @Test
    void testAppealStartBeforeJudgingDeadline_ThrowsBadRequest() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(roundStatusRepository.findById(roundStatusId)).thenReturn(Optional.of(roundStatus));

        request.setStartDate(LocalDateTime.of(2026, 7, 25, 8, 0));
        request.setSubmissionDeadline(LocalDateTime.of(2026, 7, 25, 12, 0));
        request.setJudgingDeadline(LocalDateTime.of(2026, 7, 25, 15, 0));
        request.setEndDate(LocalDateTime.of(2026, 7, 25, 20, 0));
        request.setAppealStartTime(LocalDateTime.of(2026, 7, 25, 14, 59));
        request.setAppealEndTime(LocalDateTime.of(2026, 7, 25, 16, 0));

        assertThrows(BadRequestException.class, () -> roundService.create(categoryId, request));
    }

    @Test
    void testAppealEndAfterRoundEnd_ThrowsBadRequest() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(roundStatusRepository.findById(roundStatusId)).thenReturn(Optional.of(roundStatus));

        request.setStartDate(LocalDateTime.of(2026, 7, 25, 8, 0));
        request.setJudgingDeadline(LocalDateTime.of(2026, 7, 25, 15, 0));
        request.setEndDate(LocalDateTime.of(2026, 7, 25, 20, 0));
        request.setAppealStartTime(LocalDateTime.of(2026, 7, 25, 15, 0));
        request.setAppealEndTime(LocalDateTime.of(2026, 7, 25, 20, 1));

        assertThrows(BadRequestException.class, () -> roundService.create(categoryId, request));
    }

    @Test
    void testRoundCanEndAtLastMinuteOfEventEndDate() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(roundStatusRepository.findById(roundStatusId)).thenReturn(Optional.of(roundStatus));
        when(roundRepository.save(any())).thenReturn(null);

        request.setStartDate(LocalDateTime.of(2026, 7, 25, 8, 0));
        request.setEndDate(LocalDateTime.of(2026, 7, 25, 23, 59));
        request.setAppealStartTime(LocalDateTime.of(2026, 7, 25, 22, 0));
        request.setAppealEndTime(LocalDateTime.of(2026, 7, 25, 23, 59));

        assertDoesNotThrow(() -> roundService.create(categoryId, request));
    }

    @Test
    void deleteUpcomingRoundWithoutDependenciesDeletesRound() {
        Round round = roundWithStatus("Upcoming");
        UUID roundId = round.getRoundId();
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        when(roundCriterionRepository.existsByRoundRoundId(roundId)).thenReturn(false);
        when(roundJudgeRepository.findByRoundRoundId(roundId)).thenReturn(List.of());
        when(submissionsRepository.existsByRoundId(roundId)).thenReturn(false);
        when(submissionHistoryRepository.existsByRoundId(roundId)).thenReturn(false);
        when(roundRankingRepository.existsByRoundRoundId(roundId)).thenReturn(false);
        when(roundRepository.countCalibrationSamplesByRoundId(roundId)).thenReturn(0L);

        assertDoesNotThrow(() -> roundService.delete(roundId));

        verify(roundRepository).delete(round);
    }

    @Test
    void deleteNonUpcomingRoundThrowsBusinessConflict() {
        Round round = roundWithStatus("Ongoing");
        UUID roundId = round.getRoundId();
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> roundService.delete(roundId)
        );

        assertEquals("Only delete round at status 'Upcoming'", exception.getMessage());
        verify(roundRepository, never()).delete(any());
    }

    @Test
    void deleteRoundWithActiveJudgeThrowsBusinessConflict() {
        Round round = roundWithStatus("Upcoming");
        UUID roundId = round.getRoundId();
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        when(roundCriterionRepository.existsByRoundRoundId(roundId)).thenReturn(false);
        when(roundJudgeRepository.findByRoundRoundId(roundId)).thenReturn(List.of(roundJudge(round, true)));

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> roundService.delete(roundId)
        );

        assertEquals("Cannot delete round because it already has assigned judges.", exception.getMessage());
        verify(roundRepository, never()).delete(any());
    }

    @Test
    void deleteRoundWithInactiveJudgeWithoutJudgingDeletesAssignmentThenRound() {
        Round round = roundWithStatus("Upcoming");
        UUID roundId = round.getRoundId();
        RoundJudge inactiveJudge = roundJudge(round, false);
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        when(roundCriterionRepository.existsByRoundRoundId(roundId)).thenReturn(false);
        when(roundJudgeRepository.findByRoundRoundId(roundId)).thenReturn(List.of(inactiveJudge));
        when(judgingRepository.existsByRoundJudge_RoundJudgeId(inactiveJudge.getRoundJudgeId())).thenReturn(false);
        when(submissionsRepository.existsByRoundId(roundId)).thenReturn(false);
        when(submissionHistoryRepository.existsByRoundId(roundId)).thenReturn(false);
        when(roundRankingRepository.existsByRoundRoundId(roundId)).thenReturn(false);
        when(roundRepository.countCalibrationSamplesByRoundId(roundId)).thenReturn(0L);

        assertDoesNotThrow(() -> roundService.delete(roundId));

        verify(roundJudgeRepository).deleteAll(List.of(inactiveJudge));
        verify(roundRepository).delete(round);
    }

    @Test
    void deleteRoundWithInactiveJudgeWithJudgingThrowsBusinessConflict() {
        Round round = roundWithStatus("Upcoming");
        UUID roundId = round.getRoundId();
        RoundJudge inactiveJudge = roundJudge(round, false);
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        when(roundCriterionRepository.existsByRoundRoundId(roundId)).thenReturn(false);
        when(roundJudgeRepository.findByRoundRoundId(roundId)).thenReturn(List.of(inactiveJudge));
        when(judgingRepository.existsByRoundJudge_RoundJudgeId(inactiveJudge.getRoundJudgeId())).thenReturn(true);

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> roundService.delete(roundId)
        );

        assertEquals("Cannot delete round because it already has judge scoring history.", exception.getMessage());
        verify(roundJudgeRepository, never()).deleteAll(any());
        verify(roundRepository, never()).delete(any());
    }

    @Test
    void deleteRoundWithCriteriaThrowsBusinessConflict() {
        Round round = roundWithStatus("Upcoming");
        UUID roundId = round.getRoundId();
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        when(roundCriterionRepository.existsByRoundRoundId(roundId)).thenReturn(true);

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> roundService.delete(roundId)
        );

        assertEquals("Cannot delete round because it already has criteria. Remove criteria first.", exception.getMessage());
        verify(roundRepository, never()).delete(any());
    }

    @Test
    void deleteRoundWithSubmissionsThrowsBusinessConflict() {
        Round round = roundWithStatus("Upcoming");
        UUID roundId = round.getRoundId();
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        when(roundCriterionRepository.existsByRoundRoundId(roundId)).thenReturn(false);
        when(roundJudgeRepository.findByRoundRoundId(roundId)).thenReturn(List.of());
        when(submissionsRepository.existsByRoundId(roundId)).thenReturn(true);

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> roundService.delete(roundId)
        );

        assertEquals("Cannot delete round because it already has submissions.", exception.getMessage());
        verify(roundRepository, never()).delete(any());
    }

    @Test
    void deleteRoundWithSubmissionHistoryThrowsBusinessConflict() {
        Round round = roundWithStatus("Upcoming");
        UUID roundId = round.getRoundId();
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        when(roundCriterionRepository.existsByRoundRoundId(roundId)).thenReturn(false);
        when(roundJudgeRepository.findByRoundRoundId(roundId)).thenReturn(List.of());
        when(submissionsRepository.existsByRoundId(roundId)).thenReturn(false);
        when(submissionHistoryRepository.existsByRoundId(roundId)).thenReturn(true);

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> roundService.delete(roundId)
        );

        assertEquals("Cannot delete round because it already has submission history.", exception.getMessage());
        verify(roundRepository, never()).delete(any());
    }

    @Test
    void deleteRoundWithRankingsThrowsBusinessConflict() {
        Round round = roundWithStatus("Upcoming");
        UUID roundId = round.getRoundId();
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        when(roundCriterionRepository.existsByRoundRoundId(roundId)).thenReturn(false);
        when(roundJudgeRepository.findByRoundRoundId(roundId)).thenReturn(List.of());
        when(submissionsRepository.existsByRoundId(roundId)).thenReturn(false);
        when(submissionHistoryRepository.existsByRoundId(roundId)).thenReturn(false);
        when(roundRankingRepository.existsByRoundRoundId(roundId)).thenReturn(true);

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> roundService.delete(roundId)
        );

        assertEquals("Cannot delete round because it already has rankings.", exception.getMessage());
        verify(roundRepository, never()).delete(any());
    }

    @Test
    void deleteRoundWithCalibrationSamplesThrowsBusinessConflict() {
        Round round = roundWithStatus("Upcoming");
        UUID roundId = round.getRoundId();
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        when(roundCriterionRepository.existsByRoundRoundId(roundId)).thenReturn(false);
        when(roundJudgeRepository.findByRoundRoundId(roundId)).thenReturn(List.of());
        when(submissionsRepository.existsByRoundId(roundId)).thenReturn(false);
        when(submissionHistoryRepository.existsByRoundId(roundId)).thenReturn(false);
        when(roundRankingRepository.existsByRoundRoundId(roundId)).thenReturn(false);
        when(roundRepository.countCalibrationSamplesByRoundId(roundId)).thenReturn(1L);

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> roundService.delete(roundId)
        );

        assertEquals("Cannot delete round because it already has calibration samples.", exception.getMessage());
        verify(roundRepository, never()).delete(any());
    }

    private Round roundWithStatus(String statusName) {
        RoundStatus status = new RoundStatus();
        status.setStatusId(UUID.randomUUID());
        status.setStatusName(statusName);

        Round round = new Round();
        round.setRoundId(UUID.randomUUID());
        round.setRoundStatus(status);
        return round;
    }

    private RoundJudge roundJudge(Round round, boolean active) {
        RoundJudge roundJudge = new RoundJudge();
        roundJudge.setRoundJudgeId(UUID.randomUUID());
        roundJudge.setRound(round);
        roundJudge.setIsActive(active);
        return roundJudge;
    }
}
