package com.fpt.swp.sealhackathonbe.ranking.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fpt.swp.sealhackathonbe.ranking.dto.EventRankingDTO;
import com.fpt.swp.sealhackathonbe.ranking.dto.RoundRankingDTO;
import com.fpt.swp.sealhackathonbe.ranking.service.RankingService;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import org.springframework.security.core.Authentication;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Ranking Controller", description = "APIs for computing rankings and fetching leaderboards")
public class RankingController {

    private final RankingService rankingService;
    private final UserRepository userRepository;

    @Autowired
    public RankingController(RankingService rankingService, UserRepository userRepository) {
        this.rankingService = rankingService;
        this.userRepository = userRepository;
    }

    private UUID currentUserId(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new RuntimeException("Authenticated user not found");
        }
        return user.getUserId();
    }

    /**
     * Compute rankings for an event.
     */
    @PostMapping("/admin/events/{id}/compute-rankings")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    @Operation(summary = "Compute rankings for an event", description = "Calculates the final rankings for all submissions in an event")
    public ResponseEntity<List<EventRankingDTO>> computeEventRankings(
            @PathVariable("id") UUID eventId) {

        List<EventRankingDTO> rankings = rankingService.computeEventRankings(eventId);
        return ResponseEntity.ok(rankings);
    }

    @PostMapping("/admin/rounds/{roundId}/compute-rankings")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    @Operation(summary = "Compute rankings for a round", description = "Calculates the rankings for submissions in a specific round and category")
    public ResponseEntity<List<RoundRankingDTO>> computeRoundRankings(
            @PathVariable("roundId") UUID roundId,
            @RequestParam UUID categoryId
    ){
        List<RoundRankingDTO> rankings = rankingService.computeRoundRankings(roundId,categoryId);
        return ResponseEntity.ok(rankings);
    }
    @PostMapping("/admin/rounds/{roundId}/publish-rankings")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    @Operation(summary = "Publish rankings for a round", description = "Publishes the computed rankings for a specific round and category")
    public ResponseEntity<Void> publishRoundRankings(
            @PathVariable("roundId") UUID roundId,
            @RequestParam UUID categoryId,
            @RequestParam(required = false) Integer appealDurationMinutes,
            Authentication authentication
    ){
        rankingService.publishRoundRankings(roundId, categoryId, currentUserId(authentication), appealDurationMinutes);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/rounds/{roundId}/approve-rankings")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    @Operation(summary = "Approve rankings for a round", description = "Approves and locks the computed rankings for a specific round and category")
    public ResponseEntity<Void> approveRoundRankings(
            @PathVariable("roundId") UUID roundId,
            @RequestParam UUID categoryId,
            Authentication authentication
    ){
        rankingService.approveRoundRankings(roundId, categoryId, currentUserId(authentication));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/events/{eventId}/publish-rankings")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    @Operation(summary = "Publish rankings for an event", description = "Publishes the computed final rankings for a specific event and category")
    public ResponseEntity<Void> publishEventRankings(
            @PathVariable("eventId") UUID eventId,
            @RequestParam(required = false) UUID categoryId
    ){
        rankingService.publishEventRankings(eventId, categoryId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/categories/{categoryId}/compute-rankings")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    @Operation(summary = "Compute rankings for a category", description = "Calculates the final rankings for all submissions in a category")
    public ResponseEntity<List<EventRankingDTO>> computeCategoryEventRankings(
            @PathVariable("categoryId") UUID categoryId) {

        List<EventRankingDTO> rankings = rankingService.computeCategoryEventRankings(categoryId);
        return ResponseEntity.ok(rankings);
    }

    @PostMapping("/admin/categories/{categoryId}/publish-rankings")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    @Operation(summary = "Publish rankings for a category", description = "Publishes the computed final rankings for a specific category")
    public ResponseEntity<Void> publishCategoryEventRankings(
            @PathVariable("categoryId") UUID categoryId,
            Authentication authentication
    ){
        rankingService.publishCategoryEventRankings(categoryId, currentUserId(authentication));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/categories/{categoryId}/approve-rankings")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    @Operation(summary = "Approve rankings for a category", description = "Approves and locks the computed final rankings for a specific category")
    public ResponseEntity<Void> approveCategoryEventRankings(
            @PathVariable("categoryId") UUID categoryId,
            Authentication authentication
    ){
        rankingService.approveCategoryEventRankings(categoryId, currentUserId(authentication));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/rounds/{roundId}/rankings")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    @Operation(summary = "Get rankings for a round", description = "Fetches the existing rankings for a specific round and category without computing")
    public ResponseEntity<List<RoundRankingDTO>> getRoundRankings(
            @PathVariable("roundId") UUID roundId,
            @RequestParam UUID categoryId) {

        List<RoundRankingDTO> rankings = rankingService.getRoundRankings(roundId, categoryId);
        return ResponseEntity.ok(rankings);
    }

    @GetMapping("/admin/events/{eventId}/categories/{categoryId}/rankings")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    @Operation(summary = "Get rankings for a category", description = "Fetches the existing final rankings for a specific category without computing")
    public ResponseEntity<List<EventRankingDTO>> getCategoryRankings(
            @PathVariable("eventId") UUID eventId,
            @PathVariable("categoryId") UUID categoryId) {

        List<EventRankingDTO> rankings = rankingService.getCategoryLeaderboard(eventId, categoryId);
        return ResponseEntity.ok(rankings);
    }

    @GetMapping("/admin/events/{eventId}/rankings")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    @Operation(summary = "Get rankings for an event", description = "Fetches the existing final rankings for a specific event without computing")
    public ResponseEntity<List<EventRankingDTO>> getEventRankings(
            @PathVariable("eventId") UUID eventId) {

        List<EventRankingDTO> rankings = rankingService.getAdminEventRankings(eventId);
        return ResponseEntity.ok(rankings);
    }
}
