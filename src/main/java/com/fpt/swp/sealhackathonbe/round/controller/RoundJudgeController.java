package com.fpt.swp.sealhackathonbe.round.controller;

import com.fpt.swp.sealhackathonbe.round.dto.request.AssignJudgesRequest;
import com.fpt.swp.sealhackathonbe.round.dto.response.JudgeResponse;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundJudgeResponse;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundResponse;
import com.fpt.swp.sealhackathonbe.round.service.RoundJudgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class RoundJudgeController {
    private final RoundJudgeService roundJudgeService;

    @PostMapping("/round/judges/{roundId}")
    // RBAC:
    // Chỉ ORGANIZER được phân công judge cho round.
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public List<RoundJudgeResponse> assignJudges(
            @PathVariable UUID roundId,
            @Valid @RequestBody AssignJudgesRequest request
            ) {
        return roundJudgeService.assignJudges(roundId, request);
    }

    @GetMapping("/round/judges/{roundId}")
    public List<JudgeResponse> getJudgesByRound(@PathVariable UUID roundId) {
        return roundJudgeService.getJudgesByRound(roundId);
    }

    @GetMapping("/judge/rounds/{judgeId}")
    // RBAC:
    // Cho phép ORGANIZER hoặc chính judge xem round được phân công.
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER') or #judgeId == principal.user.userId")
    public ResponseEntity<List<RoundResponse>> getRoundsByJudge(@PathVariable UUID judgeId) {
        return ResponseEntity.ok(roundJudgeService.getRoundsByJudge(judgeId));
    }

    @DeleteMapping("/round/judge/{id}")
    // RBAC:
    // Chỉ ORGANIZER được gỡ judge khỏi round.
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public void removeJudge(@PathVariable UUID id) {
        roundJudgeService.removeJudge(id);
    }

}
