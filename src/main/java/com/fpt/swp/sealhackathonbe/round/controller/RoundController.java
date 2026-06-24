package com.fpt.swp.sealhackathonbe.round.controller;

import com.fpt.swp.sealhackathonbe.round.dto.request.CreateRoundRequest;
import com.fpt.swp.sealhackathonbe.round.dto.request.UpdateRoundRequest;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundResponse;
import com.fpt.swp.sealhackathonbe.round.service.RoundService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class RoundController {
    private final RoundService roundService;

    @PostMapping("/round/{categoryId}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public RoundResponse create(@PathVariable UUID categoryId,
                                @Valid @RequestBody CreateRoundRequest request) {
        return roundService.create(categoryId, request);
    }

    @GetMapping("/round/{id}")
    public RoundResponse getById(@PathVariable UUID id) {
        return roundService.getById(id);
    }

    @GetMapping("/rounds/{categoryId}")
    public List<RoundResponse> getByCategory(@PathVariable UUID categoryId) {
        return roundService.getByCategory(categoryId);
    }

    @PutMapping("/round/{id}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public RoundResponse update(@PathVariable UUID id,
                                @Valid @RequestBody UpdateRoundRequest request) {
        return roundService.update(id, request);
    }

    @DeleteMapping("/round/{id}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public void delete(@PathVariable UUID id) {
        roundService.delete(id);
    }

    @GetMapping("/round/final/{categoryId}")
    public RoundResponse getFinalRound(@PathVariable UUID categoryId) {
        return roundService.getFinalRound(categoryId);
    }

}
