package com.fpt.swp.sealhackathonbe.publicapi.controller;

import com.fpt.swp.sealhackathonbe.award.dto.AwardResponse;
import com.fpt.swp.sealhackathonbe.award.dto.EventPrizeTotalResponse;
import com.fpt.swp.sealhackathonbe.award.dto.HallOfFameResponse;
import com.fpt.swp.sealhackathonbe.award.dto.SystemAwardPrizeTotalResponse;
import com.fpt.swp.sealhackathonbe.award.service.AwardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Public Awards", description = "Public APIs for awards and hall of fame")
public class PublicAwardController {
    private final AwardService awardService;

    @Operation(summary = "Get awards by event", description = "Get all awards belonging to a specific event.")
    @GetMapping({"/api/v1/awards/events/{eventId}", "/api/v1/public/awards/events/{eventId}"})
    public ResponseEntity<List<AwardResponse>> getAwardsByEvent(@PathVariable UUID eventId) {
        return ResponseEntity.ok(awardService.getAwardsByEvent(eventId));
    }

    @Operation(summary = "Get total prize by event")
    @GetMapping({
            "/api/v1/awards/events/{eventId}/total-prize",
            "/api/v1/public/awards/events/{eventId}/total-prize"
    })
    public ResponseEntity<EventPrizeTotalResponse> getEventPrizeTotal(@PathVariable UUID eventId) {
        return ResponseEntity.ok(awardService.getEventPrizeTotal(eventId));
    }

    @Operation(summary = "Get total prize of all events")
    @GetMapping({"/api/v1/awards/events/total-prize", "/api/v1/public/awards/events/total-prize"})
    public ResponseEntity<SystemAwardPrizeTotalResponse> getSystemPrizeTotal() {
        return ResponseEntity.ok(awardService.getSystemPrizeTotal());
    }

    @Operation(summary = "Get hall of fame")
    @GetMapping("/api/v1/public/hall-of-fame")
    public ResponseEntity<List<HallOfFameResponse>> getHallOfFame() {
        return ResponseEntity.ok(awardService.getHallOfFameData());
    }
}
