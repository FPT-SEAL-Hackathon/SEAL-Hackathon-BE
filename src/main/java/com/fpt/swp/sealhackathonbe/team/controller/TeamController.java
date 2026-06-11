package com.fpt.swp.sealhackathonbe.team.controller;

import com.fpt.swp.sealhackathonbe.team.dto.CreateTeamRequest;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualificationResponse;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualifiedTeamResponse;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualifyTeamRequest;
import com.fpt.swp.sealhackathonbe.team.dto.HandleJoinRequest;
import com.fpt.swp.sealhackathonbe.team.dto.JoinTeamRequestResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamMemberDetailResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamResponse;
import com.fpt.swp.sealhackathonbe.team.service.TeamDisqualificationService;
import com.fpt.swp.sealhackathonbe.team.service.TeamJoinRequestService;
import com.fpt.swp.sealhackathonbe.team.service.TeamService;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final TeamJoinRequestService teamJoinRequestService;
    private final TeamDisqualificationService teamDisqualificationService;
    private final UserRepository userRepository;

    @PostMapping("/teams")
    public ResponseEntity<TeamResponse> createTeam(
            @Valid @RequestBody CreateTeamRequest request,
            Authentication authentication
    ) {
        TeamResponse response = teamService.createTeam(request, currentUserId(authentication));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teams/my-team")
    public ResponseEntity<TeamResponse> getMyTeam(Authentication authentication) {
        TeamResponse response = teamService.getMyTeam(currentUserId(authentication));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teams/{teamId}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable UUID teamId) {
        TeamResponse response = teamService.getById(teamId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teams/{teamId}/members/{userId}")
    public ResponseEntity<TeamMemberDetailResponse> getTeamMemberDetail(
            @PathVariable UUID teamId,
            @PathVariable UUID userId
    ) {
        TeamMemberDetailResponse response = teamService.getTeamMemberDetail(teamId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/teams/{teamId}/join")
    public ResponseEntity<JoinTeamRequestResponse> requestToJoinTeam(
            @PathVariable UUID teamId,
            Authentication authentication
    ) {
        JoinTeamRequestResponse response =
                teamJoinRequestService.requestToJoinTeam(teamId, currentUserId(authentication));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teams/{teamId}/requests")
    public ResponseEntity<List<JoinTeamRequestResponse>> getPendingJoinRequests(
            @PathVariable UUID teamId,
            Authentication authentication
    ) {
        List<JoinTeamRequestResponse> response =
                teamJoinRequestService.getPendingJoinRequests(teamId, currentUserId(authentication));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/teams/requests/{requestId}")
    public ResponseEntity<JoinTeamRequestResponse> handleJoinRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody HandleJoinRequest request,
            Authentication authentication
    ) {
        JoinTeamRequestResponse response =
                teamJoinRequestService.handleJoinRequest(requestId, request, currentUserId(authentication));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/teams/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        teamService.removeMember(userId, currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/teams/{teamId}/disqualify")
    public ResponseEntity<DisqualificationResponse> disqualifyTeam(
            @PathVariable UUID teamId,
            @Valid @RequestBody DisqualifyTeamRequest request,
            Authentication authentication
    ) {
        DisqualificationResponse response =
                teamDisqualificationService.disqualifyTeam(teamId, request, currentUserId(authentication));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/teams/disqualified")
    public ResponseEntity<List<DisqualifiedTeamResponse>> getDisqualifiedTeams() {
        return ResponseEntity.ok(teamDisqualificationService.getDisqualifiedTeams());
    }

    private UUID currentUserId(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new RuntimeException("Authenticated user not found");
        }

        return user.getUserId();
    }
}
