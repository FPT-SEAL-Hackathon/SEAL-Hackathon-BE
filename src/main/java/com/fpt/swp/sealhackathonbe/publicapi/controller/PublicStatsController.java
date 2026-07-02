package com.fpt.swp.sealhackathonbe.publicapi.controller;

import com.fpt.swp.sealhackathonbe.publicapi.dto.PublicCountResponse;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.team.dto.TeamCountResponse;
import com.fpt.swp.sealhackathonbe.team.service.TeamService;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "Public Statistics", description = "Public APIs for system-wide statistics")
public class PublicStatsController {
    private static final List<String> JUDGE_TYPE_NAMES = List.of("Internal Judge", "Guest Judge");

    private final SubmissionsRepository submissionsRepository;
    private final TeamService teamService;
    private final UserRepository userRepository;

    @Operation(summary = "Count all submissions in the system")
    @GetMapping("/submissions/count")
    public ResponseEntity<PublicCountResponse> countAllSubmissions() {
        return ResponseEntity.ok(new PublicCountResponse(submissionsRepository.count()));
    }

    @Operation(summary = "Count all teams publicly")
    @GetMapping("/teams/count")
    public ResponseEntity<TeamCountResponse> countAllTeamsPublic() {
        return ResponseEntity.ok(new TeamCountResponse(teamService.countAllTeams()));
    }

    @Operation(summary = "Count all judges in the system")
    @GetMapping("/judges/count")
    public ResponseEntity<PublicCountResponse> countAllJudges() {
        Long count = userRepository.countActiveUsersByTypeNames(JUDGE_TYPE_NAMES);
        return ResponseEntity.ok(new PublicCountResponse(count));
    }
}
