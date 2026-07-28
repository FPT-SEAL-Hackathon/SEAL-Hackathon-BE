package com.fpt.swp.sealhackathonbe.category.controller;

import com.fpt.swp.sealhackathonbe.category.dto.response.MentorDashboardSummaryResponse;
import com.fpt.swp.sealhackathonbe.category.service.MentorDashboardService;
import com.fpt.swp.sealhackathonbe.user.entity.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cung cấp dữ liệu tổng hợp cho Mentor Dashboard.
 * Thay thế 50+ API call rải rác từ FE bằng 1 endpoint duy nhất.
 */
@RestController
@RequestMapping("/api/v1/mentor")
@RequiredArgsConstructor
@Tag(name = "Mentor Dashboard", description = "Aggregate APIs for Mentor Dashboard")
public class MentorDashboardController {

    private final MentorDashboardService mentorDashboardService;

    @Operation(summary = "Get mentor dashboard summary",
            description = "Returns all assigned categories and associated teams in a single request. " +
                          "Replaces the N+1 waterfall API calls previously made from the frontend.")
    @GetMapping("/dashboard-summary")
    @PreAuthorize("hasAnyAuthority('ROLE_MENTOR', 'ROLE_EXPERT')")
    public ResponseEntity<MentorDashboardSummaryResponse> getDashboardSummary(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                mentorDashboardService.getDashboardSummary(principal.getUser().getUserId())
        );
    }
}
