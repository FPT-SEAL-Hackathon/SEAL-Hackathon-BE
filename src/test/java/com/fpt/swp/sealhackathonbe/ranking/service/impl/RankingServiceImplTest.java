package com.fpt.swp.sealhackathonbe.ranking.service.impl;

import com.fpt.swp.sealhackathonbe.appeal.entity.AppealStatus;
import com.fpt.swp.sealhackathonbe.appeal.repository.AppealRepository;
import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.judging.entity.Judging;
import com.fpt.swp.sealhackathonbe.judging.service.JudgingService;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import com.fpt.swp.sealhackathonbe.ranking.dto.EventRankingDTO;
import com.fpt.swp.sealhackathonbe.ranking.dto.RoundRankingDTO;
import com.fpt.swp.sealhackathonbe.ranking.entity.EventRanking;
import com.fpt.swp.sealhackathonbe.ranking.entity.RoundRanking;
import com.fpt.swp.sealhackathonbe.ranking.repository.EventRankingRepository;
import com.fpt.swp.sealhackathonbe.ranking.repository.RoundRankingRepository;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundResponse;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.service.RoundService;
import com.fpt.swp.sealhackathonbe.submission.dto.DisqualifiedSubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionDisqualificationService;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionQueryService;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualifiedTeamResponse;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.service.TeamDisqualificationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
public class RankingServiceImplTest {

    @Mock
    private JudgingService judgingService;
    @Mock
    private RoundRankingRepository roundRankingRepository;
    @Mock
    private EventRankingRepository eventRankingRepository;
    @Mock
    private TeamDisqualificationService teamDisqualificationService;
    @Mock
    private SubmissionDisqualificationService submissionDisqualificationService;
    @Mock
    private SubmissionQueryService submissionQueryService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private RoundService roundService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private TeamMembersRepository teamMembersRepository;
    @Mock
    private AppealRepository appealRepository;

    @InjectMocks
    private RankingServiceImpl rankingService;

    private UUID roundId;
    private UUID categoryId;
    private UUID eventId;
    private UUID teamId;
    private UUID submissionId;

    private Round round;
    private Category category;
    private Event event;
    private Teams team;
    private Submissions submission;

    @BeforeEach
    void setUp() {
        roundId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        teamId = UUID.randomUUID();
        submissionId = UUID.randomUUID();

        event = new Event();
        event.setEventId(eventId);

        category = new Category();
        category.setCategoryId(categoryId);
        category.setEvent(event);
        category.setCategoryName("Web Development");

        round = new Round();
        round.setRoundId(roundId);
        round.setCategory(category);
        round.setRoundName("Round 1");
        round.setRoundOrder(1);

        team = new Teams();
        team.setTeamId(teamId);
        team.setTeamName("Alpha Team");
        team.setCategory(category);
        team.setEvent(event);

        submission = new Submissions();
        submission.setSubmissionId(submissionId);
        submission.setTeam(team);
        submission.setLastUpdatedAt(LocalDateTime.now());

        lenient().when(entityManager.getReference(Round.class, roundId)).thenReturn(round);
        lenient().when(entityManager.find(Round.class, roundId)).thenReturn(round);
        lenient().when(entityManager.getReference(Category.class, categoryId)).thenReturn(category);
        lenient().when(entityManager.find(Category.class, categoryId)).thenReturn(category);
    }

    @Test
    void testComputeRoundRankings_Success() {
        // Arrange
        SubmissionResponse subResponse = new SubmissionResponse();
        subResponse.setSubmissionId(submissionId);
        subResponse.setTeamId(teamId);
        subResponse.setTeamName("Alpha Team");
        subResponse.setIsScoreApproved(true);
        when(submissionQueryService.getSubmissionsByRound(roundId)).thenReturn(List.of(subResponse));

        when(submissionDisqualificationService.getDisqualifiedSubmissions(roundId)).thenReturn(Collections.emptyList());
        when(teamDisqualificationService.getDisqualifiedTeams(roundId, categoryId)).thenReturn(Collections.emptyList());

        when(roundRankingRepository.findByRound_RoundIdAndCategory_CategoryId(roundId, categoryId)).thenReturn(Collections.emptyList());

        Judging judging = mock(Judging.class);
        com.fpt.swp.sealhackathonbe.round.entity.RoundCriterion criterion = mock(com.fpt.swp.sealhackathonbe.round.entity.RoundCriterion.class);
        when(judging.getScoreValue()).thenReturn(BigDecimal.valueOf(8.5));
        when(judging.getIsCalibration()).thenReturn(false);
        when(judging.getRoundCriterion()).thenReturn(criterion);
        when(criterion.getWeight()).thenReturn(BigDecimal.ONE);

        Map<UUID, List<Judging>> mockJudgings = new HashMap<>();
        mockJudgings.put(submissionId, List.of(judging));
        when(judgingService.getJudgingsGroupedBySubmissionIds(List.of(submissionId))).thenReturn(mockJudgings);

        when(entityManager.getReference(Teams.class, teamId)).thenReturn(team);
        when(entityManager.getReference(Submissions.class, submissionId)).thenReturn(submission);

        when(roundService.getAdvancementTopN(roundId)).thenReturn(1);
        when(roundRankingRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<RoundRankingDTO> result = rankingService.computeRoundRankings(roundId, categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        RoundRankingDTO dto = result.get(0);
        assertEquals(teamId, dto.getTeamId());
        assertEquals(1, dto.getRankPosition());
        assertTrue(dto.getIsAdvanced());
        verify(roundRankingRepository).saveAll(anyList());
    }

    @Test
    void testComputeRoundRankings_RankingsLocked() {
        // Arrange
        RoundRanking lockedRanking = new RoundRanking();
        lockedRanking.setIsApproved(true);

        when(submissionQueryService.getSubmissionsByRound(roundId)).thenReturn(Collections.emptyList());
        when(submissionDisqualificationService.getDisqualifiedSubmissions(roundId)).thenReturn(Collections.emptyList());
        when(teamDisqualificationService.getDisqualifiedTeams(roundId, categoryId)).thenReturn(Collections.emptyList());
        when(roundRankingRepository.findByRound_RoundIdAndCategory_CategoryId(roundId, categoryId)).thenReturn(List.of(lockedRanking));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                rankingService.computeRoundRankings(roundId, categoryId));
        assertEquals("Cannot compute rankings because they have been approved and locked.", exception.getMessage());
    }

    @Test
    void testComputeRoundRankings_ScoresNotFinalized() {
        // Arrange
        SubmissionResponse subResponse = new SubmissionResponse();
        subResponse.setSubmissionId(submissionId);
        subResponse.setTeamId(teamId);
        subResponse.setIsScoreApproved(false); // Not approved!
        when(submissionQueryService.getSubmissionsByRound(roundId)).thenReturn(List.of(subResponse));

        when(submissionDisqualificationService.getDisqualifiedSubmissions(roundId)).thenReturn(Collections.emptyList());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                rankingService.computeRoundRankings(roundId, categoryId));
        assertTrue(exception.getMessage().contains("Cannot compute rankings because not all valid submissions have their scores finalized and approved."));
    }

    @Test
    void testPublishRoundRankings_Success() {
        // Arrange
        RoundRanking ranking = new RoundRanking();
        ranking.setRound(round);
        ranking.setCategory(category);
        ranking.setTeam(team);
        ranking.setSubmission(submission);

        when(roundRankingRepository.findByRound_RoundIdAndCategory_CategoryId(roundId, categoryId)).thenReturn(List.of(ranking));

        // finalized scores check
        SubmissionResponse subResponse = new SubmissionResponse();
        subResponse.setSubmissionId(submissionId);
        subResponse.setIsScoreApproved(true);
        when(submissionQueryService.getSubmissionsByRound(roundId)).thenReturn(List.of(subResponse));
        when(submissionDisqualificationService.getDisqualifiedSubmissions(roundId)).thenReturn(Collections.emptyList());

        TeamMembers member = new TeamMembers();
        member.setUserId(UUID.randomUUID());
        member.setTeamId(teamId);
        member.setActive(true);
        when(teamMembersRepository.findByTeamIdAndActiveTrue(teamId)).thenReturn(List.of(member));

        // Act
        rankingService.publishRoundRankings(roundId, categoryId, UUID.randomUUID(), 30);

        // Assert
        assertTrue(ranking.getIsPublished());
        assertNotNull(round.getAppealStartTime());
        assertNotNull(round.getAppealEndTime());
        verify(roundRankingRepository).saveAll(anyList());
        verify(notificationService).sendBroadcastNotification(anyList(), any(), eq(eventId), anyString(), anyString());
    }

    @Test
    void testPublishRoundRankings_NoRankingsComputed() {
        // Arrange
        when(roundRankingRepository.findByRound_RoundIdAndCategory_CategoryId(roundId, categoryId)).thenReturn(Collections.emptyList());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                rankingService.publishRoundRankings(roundId, categoryId, UUID.randomUUID(), 30));
        assertEquals("Rankings must be computed before publishing.", exception.getMessage());
    }

    @Test
    void testApproveRoundRankings_Success() {
        // Arrange
        RoundRanking ranking = new RoundRanking();
        ranking.setRound(round);
        ranking.setCategory(category);
        ranking.setTeam(team);
        ranking.setSubmission(submission);

        round.setAppealEndTime(LocalDateTime.now().minusMinutes(5)); // closed

        when(roundRankingRepository.findByRound_RoundIdAndCategory_CategoryId(roundId, categoryId)).thenReturn(List.of(ranking));
        when(appealRepository.existsByRound_RoundIdAndStatus(roundId, AppealStatus.PENDING)).thenReturn(false);

        SubmissionResponse subResponse = new SubmissionResponse();
        subResponse.setSubmissionId(submissionId);
        subResponse.setIsScoreApproved(true);
        when(submissionQueryService.getSubmissionsByRound(roundId)).thenReturn(List.of(subResponse));
        when(submissionDisqualificationService.getDisqualifiedSubmissions(roundId)).thenReturn(Collections.emptyList());

        // Act
        rankingService.approveRoundRankings(roundId, categoryId, UUID.randomUUID());

        // Assert
        assertTrue(ranking.getIsApproved());
        verify(roundRankingRepository).saveAll(anyList());
    }

    @Test
    void testApproveRoundRankings_AppealWindowOpen() {
        // Arrange
        RoundRanking ranking = new RoundRanking();
        ranking.setRound(round);

        round.setAppealEndTime(LocalDateTime.now().plusMinutes(5)); // open!

        when(roundRankingRepository.findByRound_RoundIdAndCategory_CategoryId(roundId, categoryId)).thenReturn(List.of(ranking));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                rankingService.approveRoundRankings(roundId, categoryId, UUID.randomUUID()));
        assertEquals("Cannot approve rankings while the appeal window is still open.", exception.getMessage());
    }

    @Test
    void testApproveRoundRankings_PendingAppeals() {
        // Arrange
        RoundRanking ranking = new RoundRanking();
        ranking.setRound(round);

        round.setAppealEndTime(LocalDateTime.now().minusMinutes(5)); // closed

        when(roundRankingRepository.findByRound_RoundIdAndCategory_CategoryId(roundId, categoryId)).thenReturn(List.of(ranking));
        when(appealRepository.existsByRound_RoundIdAndStatus(roundId, AppealStatus.PENDING)).thenReturn(true); // pending appeals!

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                rankingService.approveRoundRankings(roundId, categoryId, UUID.randomUUID()));
        assertEquals("Cannot approve rankings because there are still pending appeals for this round.", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testComputeCategoryEventRankings_Success() {
        // Arrange
        when(entityManager.find(Category.class, categoryId)).thenReturn(category);

        RoundResponse finalRoundResponse = RoundResponse.builder().roundId(roundId).build();
        when(roundService.getFinalRound(categoryId)).thenReturn(finalRoundResponse);

        TypedQuery<UUID> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(UUID.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(teamId));

        TypedQuery<Teams> teamsQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Teams.class))).thenReturn(teamsQuery);
        when(teamsQuery.setParameter(anyString(), any())).thenReturn(teamsQuery);
        when(teamsQuery.getResultList()).thenReturn(List.of(team));

        when(teamDisqualificationService.getDisqualifiedTeamsByCategory(categoryId)).thenReturn(Collections.emptyList());
        when(eventRankingRepository.findByEvent_EventIdAndCategory_CategoryId(eventId, categoryId)).thenReturn(Collections.emptyList());

        RoundRanking roundRanking = new RoundRanking();
        roundRanking.setRound(round);
        roundRanking.setTeam(team);
        roundRanking.setSubmission(submission);
        roundRanking.setTotalScore(BigDecimal.valueOf(8.5));
        roundRanking.setCategory(category);
        roundRanking.setIsApproved(true); // Must be approved

        when(roundRankingRepository.findByCategory_CategoryId(categoryId)).thenReturn(new ArrayList<>(List.of(roundRanking)));

        when(entityManager.getReference(Teams.class, teamId)).thenReturn(team);
        when(eventRankingRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<EventRankingDTO> result = rankingService.computeCategoryEventRankings(categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        EventRankingDTO dto = result.get(0);
        assertEquals(teamId, dto.getTeamId());
        assertEquals(BigDecimal.valueOf(8.5), dto.getFinalScore());
        assertEquals(1, dto.getRankPosition());
    }

    @Test
    void testComputeCategoryEventRankings_RoundsNotApproved() {
        // Arrange
        when(entityManager.find(Category.class, categoryId)).thenReturn(category);

        RoundResponse finalRoundResponse = RoundResponse.builder().roundId(roundId).build();
        when(roundService.getFinalRound(categoryId)).thenReturn(finalRoundResponse);

        TypedQuery<UUID> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(UUID.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(teamId));

        when(teamDisqualificationService.getDisqualifiedTeamsByCategory(categoryId)).thenReturn(Collections.emptyList());
        when(eventRankingRepository.findByEvent_EventIdAndCategory_CategoryId(eventId, categoryId)).thenReturn(Collections.emptyList());

        RoundRanking roundRanking = new RoundRanking();
        roundRanking.setRound(round);
        roundRanking.setTeam(team);
        roundRanking.setIsApproved(false); // NOT approved!

        when(roundRankingRepository.findByCategory_CategoryId(categoryId)).thenReturn(new ArrayList<>(List.of(roundRanking)));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                rankingService.computeCategoryEventRankings(categoryId));
        assertEquals("Cannot compute event rankings because not all round rankings in this category have been approved.", exception.getMessage());
    }

    @Test
    void testComputeRoundRankings_TiedScores() {
        // Arrange
        UUID teamId2 = UUID.randomUUID();
        UUID submissionId2 = UUID.randomUUID();

        Teams team2 = new Teams();
        team2.setTeamId(teamId2);
        team2.setTeamName("Beta Team");
        team2.setCategory(category);
        team2.setEvent(event);

        Submissions submission2 = new Submissions();
        submission2.setSubmissionId(submissionId2);
        submission2.setTeam(team2);

        // Tie-breaker: submission 1 is earlier, submission 2 is later
        submission.setLastUpdatedAt(LocalDateTime.now().minusHours(1));
        submission2.setLastUpdatedAt(LocalDateTime.now());

        SubmissionResponse subResponse1 = new SubmissionResponse();
        subResponse1.setSubmissionId(submissionId);
        subResponse1.setTeamId(teamId);
        subResponse1.setTeamName("Alpha Team");
        subResponse1.setIsScoreApproved(true);

        SubmissionResponse subResponse2 = new SubmissionResponse();
        subResponse2.setSubmissionId(submissionId2);
        subResponse2.setTeamId(teamId2);
        subResponse2.setTeamName("Beta Team");
        subResponse2.setIsScoreApproved(true);

        when(submissionQueryService.getSubmissionsByRound(roundId)).thenReturn(List.of(subResponse1, subResponse2));

        when(submissionDisqualificationService.getDisqualifiedSubmissions(roundId)).thenReturn(Collections.emptyList());
        when(teamDisqualificationService.getDisqualifiedTeams(roundId, categoryId)).thenReturn(Collections.emptyList());
        when(roundRankingRepository.findByRound_RoundIdAndCategory_CategoryId(roundId, categoryId)).thenReturn(Collections.emptyList());

        // Both get the same score (8.5)
        Judging judging1 = mock(Judging.class);
        Judging judging2 = mock(Judging.class);
        com.fpt.swp.sealhackathonbe.round.entity.RoundCriterion criterion = mock(com.fpt.swp.sealhackathonbe.round.entity.RoundCriterion.class);

        when(judging1.getScoreValue()).thenReturn(BigDecimal.valueOf(8.5));
        when(judging1.getIsCalibration()).thenReturn(false);
        when(judging1.getRoundCriterion()).thenReturn(criterion);

        when(judging2.getScoreValue()).thenReturn(BigDecimal.valueOf(8.5));
        when(judging2.getIsCalibration()).thenReturn(false);
        when(judging2.getRoundCriterion()).thenReturn(criterion);

        when(criterion.getWeight()).thenReturn(BigDecimal.ONE);

        Map<UUID, List<Judging>> mockJudgings = new HashMap<>();
        mockJudgings.put(submissionId, List.of(judging1));
        mockJudgings.put(submissionId2, List.of(judging2));
        when(judgingService.getJudgingsGroupedBySubmissionIds(anyList())).thenReturn(mockJudgings);

        when(entityManager.getReference(Teams.class, teamId)).thenReturn(team);
        when(entityManager.getReference(Teams.class, teamId2)).thenReturn(team2);
        when(entityManager.getReference(Submissions.class, submissionId)).thenReturn(submission);
        when(entityManager.getReference(Submissions.class, submissionId2)).thenReturn(submission2);

        when(roundService.getAdvancementTopN(roundId)).thenReturn(1); // Top 1 advances
        when(roundRankingRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<RoundRankingDTO> result = rankingService.computeRoundRankings(roundId, categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Sort output by rank to verify
        result.sort(Comparator.comparingInt(RoundRankingDTO::getRankPosition));

        RoundRankingDTO first = result.get(0);
        RoundRankingDTO second = result.get(1);

        // Alpha team should be rank 1 and advance (since it submitted earlier)
        assertEquals(teamId, first.getTeamId());
        assertEquals(1, first.getRankPosition());
        assertTrue(first.getIsAdvanced());

        // Beta team should be rank 2 and NOT advance
        assertEquals(teamId2, second.getTeamId());
        assertEquals(2, second.getRankPosition());
        assertFalse(second.getIsAdvanced());
    }

    @Test
    void testComputeRoundRankings_Disqualified() {
        // Arrange
        UUID teamId2 = UUID.randomUUID();
        UUID submissionId2 = UUID.randomUUID();

        Teams team2 = new Teams();
        team2.setTeamId(teamId2);
        team2.setTeamName("Beta Team");
        team2.setCategory(category);
        team2.setEvent(event);

        Submissions submission2 = new Submissions();
        submission2.setSubmissionId(submissionId2);
        submission2.setTeam(team2);
        submission2.setLastUpdatedAt(LocalDateTime.now());

        submission.setLastUpdatedAt(LocalDateTime.now().minusHours(1));

        SubmissionResponse subResponse1 = new SubmissionResponse();
        subResponse1.setSubmissionId(submissionId);
        subResponse1.setTeamId(teamId);
        subResponse1.setTeamName("Alpha Team");
        subResponse1.setIsScoreApproved(true);

        SubmissionResponse subResponse2 = new SubmissionResponse();
        subResponse2.setSubmissionId(submissionId2);
        subResponse2.setTeamId(teamId2);
        subResponse2.setTeamName("Beta Team");
        subResponse2.setIsScoreApproved(true);

        when(submissionQueryService.getSubmissionsByRound(roundId)).thenReturn(List.of(subResponse1, subResponse2));

        // Beta team's submission is disqualified!
        DisqualifiedSubmissionResponse disqualifiedSubmission = new DisqualifiedSubmissionResponse();
        disqualifiedSubmission.setSubmissionId(submissionId2);
        when(submissionDisqualificationService.getDisqualifiedSubmissions(roundId)).thenReturn(List.of(disqualifiedSubmission));
        when(teamDisqualificationService.getDisqualifiedTeams(roundId, categoryId)).thenReturn(Collections.emptyList());

        when(roundRankingRepository.findByRound_RoundIdAndCategory_CategoryId(roundId, categoryId)).thenReturn(Collections.emptyList());

        Judging judging1 = mock(Judging.class);
        Judging judging2 = mock(Judging.class);
        com.fpt.swp.sealhackathonbe.round.entity.RoundCriterion criterion = mock(com.fpt.swp.sealhackathonbe.round.entity.RoundCriterion.class);

        when(judging1.getScoreValue()).thenReturn(BigDecimal.valueOf(8.5));
        when(judging1.getIsCalibration()).thenReturn(false);
        when(judging1.getRoundCriterion()).thenReturn(criterion);



        when(criterion.getWeight()).thenReturn(BigDecimal.ONE);

        Map<UUID, List<Judging>> mockJudgings = new HashMap<>();
        mockJudgings.put(submissionId, List.of(judging1));
        mockJudgings.put(submissionId2, List.of(judging2));
        when(judgingService.getJudgingsGroupedBySubmissionIds(anyList())).thenReturn(mockJudgings);

        when(entityManager.getReference(Teams.class, teamId)).thenReturn(team);
        when(entityManager.getReference(Teams.class, teamId2)).thenReturn(team2);
        when(entityManager.getReference(Submissions.class, submissionId)).thenReturn(submission);
        when(entityManager.getReference(Submissions.class, submissionId2)).thenReturn(submission2);

        when(roundService.getAdvancementTopN(roundId)).thenReturn(1);
        when(roundRankingRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<RoundRankingDTO> result = rankingService.computeRoundRankings(roundId, categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        RoundRankingDTO alphaRanking = result.stream().filter(r -> r.getTeamId().equals(teamId)).findFirst().orElseThrow();
        RoundRankingDTO betaRanking = result.stream().filter(r -> r.getTeamId().equals(teamId2)).findFirst().orElseThrow();

        // Alpha team should be rank 1 and advance
        assertEquals(1, alphaRanking.getRankPosition());
        assertTrue(alphaRanking.getIsAdvanced());

        // Beta team is disqualified, so its rank is 0 and it does NOT advance
        assertEquals(0, betaRanking.getRankPosition());
        assertFalse(betaRanking.getIsAdvanced());
    }

    @Test
    void testComputeRoundRankings_TwoDisqualifiedOneValidLowerScore() {
        // Arrange
        UUID teamId2 = UUID.randomUUID();
        UUID submissionId2 = UUID.randomUUID();
        UUID teamId3 = UUID.randomUUID();
        UUID submissionId3 = UUID.randomUUID();

        // Team 2 (disqualified)
        Teams team2 = new Teams();
        team2.setTeamId(teamId2);
        team2.setTeamName("Beta Team");
        team2.setCategory(category);
        team2.setEvent(event);

        Submissions submission2 = new Submissions();
        submission2.setSubmissionId(submissionId2);
        submission2.setTeam(team2);
        submission2.setLastUpdatedAt(LocalDateTime.now());

        // Team 3 (valid, lower score)
        Teams team3 = new Teams();
        team3.setTeamId(teamId3);
        team3.setTeamName("Gamma Team");
        team3.setCategory(category);
        team3.setEvent(event);

        Submissions submission3 = new Submissions();
        submission3.setSubmissionId(submissionId3);
        submission3.setTeam(team3);
        submission3.setLastUpdatedAt(LocalDateTime.now().minusMinutes(30));

        // Team 1 (disqualified)
        submission.setLastUpdatedAt(LocalDateTime.now().minusHours(1));

        SubmissionResponse subResponse1 = new SubmissionResponse();
        subResponse1.setSubmissionId(submissionId);
        subResponse1.setTeamId(teamId);
        subResponse1.setTeamName("Alpha Team");
        subResponse1.setIsScoreApproved(true);

        SubmissionResponse subResponse2 = new SubmissionResponse();
        subResponse2.setSubmissionId(submissionId2);
        subResponse2.setTeamId(teamId2);
        subResponse2.setTeamName("Beta Team");
        subResponse2.setIsScoreApproved(true);

        SubmissionResponse subResponse3 = new SubmissionResponse();
        subResponse3.setSubmissionId(submissionId3);
        subResponse3.setTeamId(teamId3);
        subResponse3.setTeamName("Gamma Team");
        subResponse3.setIsScoreApproved(true);

        when(submissionQueryService.getSubmissionsByRound(roundId)).thenReturn(List.of(subResponse1, subResponse2, subResponse3));

        // Team 1 and Team 2 submissions are disqualified!
        DisqualifiedSubmissionResponse disSub1 = new DisqualifiedSubmissionResponse();
        disSub1.setSubmissionId(submissionId);
        DisqualifiedSubmissionResponse disSub2 = new DisqualifiedSubmissionResponse();
        disSub2.setSubmissionId(submissionId2);

        when(submissionDisqualificationService.getDisqualifiedSubmissions(roundId)).thenReturn(List.of(disSub1, disSub2));
        when(teamDisqualificationService.getDisqualifiedTeams(roundId, categoryId)).thenReturn(Collections.emptyList());

        when(roundRankingRepository.findByRound_RoundIdAndCategory_CategoryId(roundId, categoryId)).thenReturn(Collections.emptyList());

        // We only need to mock judging scores for Team 3 (since Team 1 and 2 are disqualified and their scores won't be queried)
        Judging judging3 = mock(Judging.class);
        com.fpt.swp.sealhackathonbe.round.entity.RoundCriterion criterion = mock(com.fpt.swp.sealhackathonbe.round.entity.RoundCriterion.class);

        when(judging3.getScoreValue()).thenReturn(BigDecimal.valueOf(5.0)); // Lower score than the hypothetical 9.0/9.5 of disqualified teams
        when(judging3.getIsCalibration()).thenReturn(false);
        when(judging3.getRoundCriterion()).thenReturn(criterion);
        when(criterion.getWeight()).thenReturn(BigDecimal.ONE);

        Map<UUID, List<Judging>> mockJudgings = new HashMap<>();
        mockJudgings.put(submissionId3, List.of(judging3));
        when(judgingService.getJudgingsGroupedBySubmissionIds(anyList())).thenReturn(mockJudgings);

        when(entityManager.getReference(Teams.class, teamId)).thenReturn(team);
        when(entityManager.getReference(Teams.class, teamId2)).thenReturn(team2);
        when(entityManager.getReference(Teams.class, teamId3)).thenReturn(team3);
        when(entityManager.getReference(Submissions.class, submissionId)).thenReturn(submission);
        when(entityManager.getReference(Submissions.class, submissionId2)).thenReturn(submission2);
        when(entityManager.getReference(Submissions.class, submissionId3)).thenReturn(submission3);

        when(roundService.getAdvancementTopN(roundId)).thenReturn(1);
        when(roundRankingRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<RoundRankingDTO> result = rankingService.computeRoundRankings(roundId, categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        RoundRankingDTO alphaRanking = result.stream().filter(r -> r.getTeamId().equals(teamId)).findFirst().orElseThrow();
        RoundRankingDTO betaRanking = result.stream().filter(r -> r.getTeamId().equals(teamId2)).findFirst().orElseThrow();
        RoundRankingDTO gammaRanking = result.stream().filter(r -> r.getTeamId().equals(teamId3)).findFirst().orElseThrow();

        // Gamma team should be rank 1 and advance, even though its score is lower than disqualified teams' hypothetical score
        assertEquals(1, gammaRanking.getRankPosition());
        assertTrue(gammaRanking.getIsAdvanced());

        // Alpha team is disqualified -> rank 0, no advance
        assertEquals(0, alphaRanking.getRankPosition());
        assertFalse(alphaRanking.getIsAdvanced());

        // Beta team is disqualified -> rank 0, no advance
        assertEquals(0, betaRanking.getRankPosition());
        assertFalse(betaRanking.getIsAdvanced());
    }

    @Test
    void testComputeRoundRankings_CalibrationRound_ThrowsIllegalStateException() {
        // Arrange
        Round calibrationRound = new Round();
        calibrationRound.setRoundId(roundId);
        calibrationRound.setIsCalibrationRound(true);

        when(entityManager.find(Round.class, roundId)).thenReturn(calibrationRound);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                rankingService.computeRoundRankings(roundId, categoryId));
        assertEquals("Cannot compute rankings for a calibration round.", exception.getMessage());
    }

    @Test
    void testComputeCategoryEventRankings_FinalRoundIsCalibration_ThrowsIllegalStateException() {
        // Arrange
        Category categoryRef = new Category();
        categoryRef.setCategoryId(categoryId);
        categoryRef.setCategoryName("AI Track");
        com.fpt.swp.sealhackathonbe.event.entity.Event eventRef = new com.fpt.swp.sealhackathonbe.event.entity.Event();
        eventRef.setEventId(eventId);
        categoryRef.setEvent(eventRef);

        when(entityManager.find(Category.class, categoryId)).thenReturn(categoryRef);

        RoundResponse finalRoundResponse = RoundResponse.builder()
                .roundId(roundId)
                .isCalibrationRound(true)
                .build();

        when(roundService.getFinalRound(categoryId)).thenReturn(finalRoundResponse);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                rankingService.computeCategoryEventRankings(categoryId));
        assertEquals("Cannot compute event rankings because the final round is a calibration round.", exception.getMessage());
    }
}
