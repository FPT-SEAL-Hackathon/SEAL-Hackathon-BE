package com.fpt.swp.sealhackathonbe.team.controller;

import com.fpt.swp.sealhackathonbe.team.dto.TeamCountResponse;
import com.fpt.swp.sealhackathonbe.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "Public Teams", description = "Public APIs for team statistics")
public class PublicTeamController {
    // Public endpoint khong can JWT, chi expose so lieu tong hop khong nhay cam.

    private final TeamService teamService;

    @Operation(summary = "Count all teams publicly")
    @GetMapping("/teams/count")
    public ResponseEntity<TeamCountResponse> countAllTeamsPublic() {
        return ResponseEntity.ok(new TeamCountResponse(teamService.countAllTeams()));
    }
}
