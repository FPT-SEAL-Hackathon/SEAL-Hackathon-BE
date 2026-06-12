package com.fpt.swp.sealhackathonbe.round.controller;

import com.fpt.swp.sealhackathonbe.round.dto.request.CreateRoundRequest;
import com.fpt.swp.sealhackathonbe.round.dto.request.UpdateRoundRequest;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundResponse;
import com.fpt.swp.sealhackathonbe.round.service.RoundService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/rounds")
@RequiredArgsConstructor
public class RoundController {
    private final RoundService roundService;

    @PostMapping("/{categoryId}")
    public RoundResponse create(@PathVariable UUID categoryId,
                                @Valid @RequestBody CreateRoundRequest request) {
        return roundService.create(request);
    }

    @GetMapping("/{id}")
    public RoundResponse getById(@PathVariable UUID id) {
        return roundService.getById(id);
    }

    @GetMapping("/{categoryId}")
    public List<RoundResponse> getByCategory(@PathVariable UUID categoryId) {
        return roundService.getByCategory(categoryId);
    }

    @PutMapping("/{id}")
    public RoundResponse update(@PathVariable UUID id,
                                @Valid @RequestBody UpdateRoundRequest request) {
        return roundService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        roundService.delete(id);
    }

    @GetMapping("/final/{categoryId}")
    public RoundResponse getFinalRound(@PathVariable UUID categoryId) {
        return roundService.getFinalRound(categoryId);
    }

}
