package com.fpt.swp.sealhackathonbe.round.controller;

import com.fpt.swp.sealhackathonbe.round.dto.request.AssignJudgesRequest;
import com.fpt.swp.sealhackathonbe.round.dto.response.JudgeResponse;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundJudgeResponse;
import com.fpt.swp.sealhackathonbe.round.service.RoundJudgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/rounds")
@RequiredArgsConstructor
public class RoundJudgeController {
    private final RoundJudgeService roundJudgeService;

    @PostMapping("/judges/{roundId}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public List<RoundJudgeResponse> assignJudges(
            @PathVariable UUID roundId,
            @Valid @RequestBody AssignJudgesRequest request
            ) {
        return roundJudgeService.assignJudges(roundId, request);
    }

    @GetMapping("/judges/{roundId}")
    public List<JudgeResponse> getJudgesByRound(@PathVariable UUID roundId) {
        return roundJudgeService.getJudgesByRound(roundId);
    }

    @DeleteMapping("/judge/{id}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public void removeJudge(@PathVariable UUID id) {
        roundJudgeService.removeJudge(id);
    }

}
