package com.fpt.swp.sealhackathonbe.team.controller;

import com.fpt.swp.sealhackathonbe.team.dto.CreateTeamRequest;
import com.fpt.swp.sealhackathonbe.team.dto.HandleJoinRequest;
import com.fpt.swp.sealhackathonbe.team.dto.JoinTeamRequestResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamMemberDetailResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamResponse;
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

    // Controller chi nhan request, lay user hien tai va chuyen xu ly nghiep vu cho service.
    private final TeamService teamService;
    private final TeamJoinRequestService teamJoinRequestService;
    private final UserRepository userRepository;

    @PostMapping("/teams")
    public ResponseEntity<TeamResponse> createTeam(
            @Valid @RequestBody CreateTeamRequest request,
            Authentication authentication
    ) {
        // Du lieu: request + user dang dang nhap -> TeamService -> TeamResponse.
        TeamResponse response = teamService.createTeam(request, currentUserId(authentication));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teams/my-team")
    public ResponseEntity<TeamResponse> getMyTeam(Authentication authentication) {
        // Tim membership active cua user hien tai de tra ve team ma user dang tham gia.
        TeamResponse response = teamService.getMyTeam(currentUserId(authentication));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teams/{teamId}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable UUID teamId) {
        // Lay thong tin team va danh sach member active theo teamId.
        TeamResponse response = teamService.getById(teamId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teams/{teamId}/members/{userId}")
    public ResponseEntity<TeamMemberDetailResponse> getTeamMemberDetail(
            @PathVariable UUID teamId,
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        // Chi member active cua cung team moi duoc xem thong tin chi tiet thanh vien.
        TeamMemberDetailResponse response = teamService.getTeamMemberDetail(
                teamId,
                userId,
                currentUserId(authentication)
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/teams/{teamId}/join")
    public ResponseEntity<JoinTeamRequestResponse> requestToJoinTeam(
            @PathVariable UUID teamId,
            Authentication authentication
    ) {
        // User hien tai gui don PENDING vao team duoc chon.
        JoinTeamRequestResponse response =
                teamJoinRequestService.requestToJoinTeam(teamId, currentUserId(authentication));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/teams/{teamId}/requests")
    public ResponseEntity<List<JoinTeamRequestResponse>> getPendingJoinRequests(
            @PathVariable UUID teamId,
            Authentication authentication
    ) {
        // Chi leader cua team moi duoc xem danh sach don dang cho xu ly.
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
        // Leader duyet hoac tu choi don; service chiu trach nhiem kiem tra suc chua team.
        JoinTeamRequestResponse response =
                teamJoinRequestService.handleJoinRequest(requestId, request, currentUserId(authentication));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/teams/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        // Service phan biet leader kick member va member tu roi team.
        teamService.removeMember(userId, currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId(Authentication authentication) {
        // JWT filter dat email vao Authentication; controller doi email thanh userId cho service.
        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new RuntimeException("Authenticated user not found");
        }

        return user.getUserId();
    }
}
