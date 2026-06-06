package com.fpt.swp.sealhackathonbe.team.controller;

import com.fpt.swp.sealhackathonbe.team.dto.*;
import com.fpt.swp.sealhackathonbe.team.service.TeamDisqualificationService;
import com.fpt.swp.sealhackathonbe.team.service.TeamJoinRequestService;
import com.fpt.swp.sealhackathonbe.team.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class TeamController {
    private static final UUID CURRENT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final TeamService teamService;
    private final TeamJoinRequestService teamJoinRequestService;
    private final TeamDisqualificationService teamDisqualificationService;

    public TeamController(
            TeamService teamService,
            TeamJoinRequestService teamJoinRequestService,
            TeamDisqualificationService teamDisqualificationService
    ) {
        this.teamService = teamService;
        this.teamJoinRequestService = teamJoinRequestService;
        this.teamDisqualificationService = teamDisqualificationService;
    }

    @PostMapping("/teams")
    public ResponseEntity<TeamResponse> createTeam(
            @Valid @RequestBody CreateTeamRequest request
    ) {
        TeamResponse response = teamService.createTeam(request, CURRENT_USER_ID);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teams/my-team")
    public ResponseEntity<TeamResponse> getMyTeam() {
        TeamResponse response = teamService.getMyTeam(CURRENT_USER_ID);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/teams/{teamId}/join")
    public ResponseEntity<JoinTeamRequestResponse> requestToJoinTeam(
            @PathVariable UUID teamId
    ) {
        JoinTeamRequestResponse response = teamJoinRequestService.requestToJoinTeam(teamId, CURRENT_USER_ID);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teams/{teamId}/requests")
    public ResponseEntity<List<JoinTeamRequestResponse>> getPendingJoinRequests(
            @PathVariable UUID teamId
    ) {
        List<JoinTeamRequestResponse> response =
                teamJoinRequestService.getPendingJoinRequests(teamId, CURRENT_USER_ID);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/teams/requests/{requestId}")
    public ResponseEntity<JoinTeamRequestResponse> handleJoinRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody HandleJoinRequest request
    ) {
        JoinTeamRequestResponse response =
                teamJoinRequestService.handleJoinRequest(requestId, request, CURRENT_USER_ID);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/teams/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID userId
    ) {
        teamService.removeMember(userId, CURRENT_USER_ID);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/teams/{teamId}/disqualify")
    public ResponseEntity<DisqualificationResponse> disqualifyTeam(
            @PathVariable UUID teamId,
            @Valid @RequestBody DisqualifyTeamRequest request
    ) {
        DisqualificationResponse response =
                teamDisqualificationService.disqualifyTeam(teamId, request, CURRENT_USER_ID);
        return ResponseEntity.ok(response);
    }
}
