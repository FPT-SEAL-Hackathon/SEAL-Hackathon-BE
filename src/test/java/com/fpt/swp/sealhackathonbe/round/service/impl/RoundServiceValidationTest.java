package com.fpt.swp.sealhackathonbe.round.service.impl;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.round.dto.request.CreateRoundRequest;
import com.fpt.swp.sealhackathonbe.round.entity.RoundStatus;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundStatusRepository;
import com.fpt.swp.sealhackathonbe.round.service.mapper.RoundMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
        // Event boundary 1 day: 25/07/2026 -> 25/07/2026
        event.setEventStartDate(LocalDate.of(2026, 7, 25));
        event.setEventEndDate(LocalDate.of(2026, 7, 25));

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
}
