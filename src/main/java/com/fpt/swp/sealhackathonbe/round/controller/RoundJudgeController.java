package com.fpt.swp.sealhackathonbe.round.controller;

import com.fpt.swp.sealhackathonbe.round.dto.request.AssignJudgesRequest;
import com.fpt.swp.sealhackathonbe.round.dto.response.JudgeResponse;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundJudgeResponse;
import com.fpt.swp.sealhackathonbe.round.service.RoundJudgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/rounds/judges")
@RequiredArgsConstructor
public class RoundJudgeController {
    private final RoundJudgeService roundJudgeService;

    @PostMapping("/{roundId}")
    public List<RoundJudgeResponse> assignJudges(
            @PathVariable UUID roundId,
            @Valid @RequestBody AssignJudgesRequest request
            ) {
        return roundJudgeService.assignJudges(roundId, request);
    }

    @GetMapping("/{roundId}")
    public List<JudgeResponse> getJudgesByRound(@PathVariable UUID roundId) {
        return roundJudgeService.getJudgesByRound(roundId);
    }

    @DeleteMapping("/{id}")
    public void removeJudge(@PathVariable UUID id) {
        roundJudgeService.removeJudge(id);
    }

}
