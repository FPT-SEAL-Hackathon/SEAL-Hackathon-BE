package com.fpt.swp.sealhackathonbe.team.controller;

import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantResponse;
import com.fpt.swp.sealhackathonbe.team.dto.CreateTeamRequest;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualificationResponse;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualifyTeamRequest;
import com.fpt.swp.sealhackathonbe.team.dto.EligibilityDecisionRequest;
import com.fpt.swp.sealhackathonbe.team.dto.EligibilityDecisionResponse;
import com.fpt.swp.sealhackathonbe.team.dto.HandleJoinRequest;
import com.fpt.swp.sealhackathonbe.team.dto.JoinTeamRequestResponse;
import com.fpt.swp.sealhackathonbe.team.dto.RemoveTeamMemberRequest;
import com.fpt.swp.sealhackathonbe.team.dto.TeamEligibilityReviewResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamMemberDetailResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TransferTeamLeadershipRequest;
import com.fpt.swp.sealhackathonbe.team.service.TeamEventRegistrationService;
import com.fpt.swp.sealhackathonbe.team.service.TeamJoinRequestService;
import com.fpt.swp.sealhackathonbe.team.service.TeamDisqualificationService;
import com.fpt.swp.sealhackathonbe.team.service.TeamService;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@Tag(name = "Team Management", description = "APIs for managing teams, members, join requests and disqualifications")
public class TeamController {

    // Controller chi nhan request, lay user hien tai va chuyen xu ly nghiep vu cho service.
    private final TeamService teamService;
    private final TeamJoinRequestService teamJoinRequestService;
    private final TeamDisqualificationService teamDisqualificationService;
    private final TeamEventRegistrationService teamEventRegistrationService;
    private final UserRepository userRepository;

    // Quyen hien tai: moi tai khoan co JWT hop le deu co the tao team.
    // Chua gioi han theo UserType; service chi chan user da o team active trong cung event.
    // Seed test thanh cong: api.join.leader@seal.test / Test@123 (tai khoan chua co team).
    // Khong dung alpha/beta/green leader hoac member vi cac tai khoan nay da o team trong live event.
    @Operation(summary = "Create a team")
    @PostMapping("/teams")
    public ResponseEntity<TeamResponse> createTeam(
            @Valid @RequestBody CreateTeamRequest request,
            Authentication authentication
    ) {
        // Du lieu: request + user dang dang nhap -> TeamService -> TeamResponse.
        TeamResponse response = teamService.createTeam(request, currentUserId(authentication));
        return ResponseEntity.ok(response);
    }

    // Quyen hien tai: moi tai khoan co JWT hop le, khong can la member cua team.
    // Seed test: dung bat ky tai khoan seed nao co JWT hop le.
    // Team mau: Alpha = E1000000-0000-0000-0000-000000000001,
    // Beta = E1000000-0000-0000-0000-000000000002.
    @Operation(summary = "Get team details")
    @GetMapping("/teams/{teamId}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable UUID teamId) {
        // Lay thong tin team va danh sach member active theo teamId.
        TeamResponse response = teamService.getById(teamId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get teams by event")
    @GetMapping("/events/{eventId}/teams")
    public ResponseEntity<List<TeamResponse>> getTeamsByEvent(@PathVariable UUID eventId) {
        List<TeamResponse> response = teamService.getByEventId(eventId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Review team eligibility by event",
            description = "Organizer reviews team size and member profile information before competition."
    )
    @GetMapping("/admin/events/{eventId}/teams/eligibility-review")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<List<TeamEligibilityReviewResponse>> reviewTeamsEligibility(
            @PathVariable UUID eventId
    ) {
        List<TeamEligibilityReviewResponse> response = teamService.reviewTeamsEligibility(eventId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Decide team eligibility",
            description = "Organizer approves an eligible team for competition or rejects it with a disqualification reason."
    )
    @PostMapping("/admin/teams/{teamId}/eligibility-decision")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<EligibilityDecisionResponse> decideTeamEligibility(
            @PathVariable UUID teamId,
            @Valid @RequestBody EligibilityDecisionRequest request,
            Authentication authentication
    ) {
        EligibilityDecisionResponse response = new EligibilityDecisionResponse();
        response.setApproved(request.getApproved());

        if (Boolean.TRUE.equals(request.getApproved())) {
            TeamResponse team = teamService.activateTeam(teamId, request.getNote(), currentUserId(authentication));
            // Duyệt team = duyệt luôn toàn bộ EventParticipant PENDING của thành viên.
            teamEventRegistrationService.applyTeamDecision(teamId, true, request.getNote(), currentUserId(authentication));
            response.setTeam(team);
            response.setMessage("Team approved for competition");
        } else {
            if (request.getNote() == null || request.getNote().trim().isEmpty()) {
                throw new RuntimeException("Rejection reason is required");
            }

            TeamResponse team = teamService.rejectTeam(teamId, request.getNote(), currentUserId(authentication));
            // Từ chối team = từ chối toàn bộ participant PENDING của thành viên.
            teamEventRegistrationService.applyTeamDecision(teamId, false, request.getNote(), currentUserId(authentication));
            response.setTeam(team);
            response.setMessage("Team registration request rejected");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Team-first: leader đăng ký cả team vào sự kiện của team.
     * Tạo EventParticipant PENDING cho mọi thành viên, chờ organizer duyệt theo team.
     */
    @Operation(summary = "Register the whole team for its event (leader only)")
    @PostMapping("/teams/{teamId}/register-event")
    public ResponseEntity<List<EventParticipantResponse>> registerTeamForEvent(
            @PathVariable UUID teamId,
            Authentication authentication
    ) {
        List<EventParticipantResponse> response =
                teamEventRegistrationService.registerTeam(teamId, currentUserId(authentication));
        return ResponseEntity.ok(response);
    }

    /**
     * Leader rút đăng ký khi organizer chưa xử lý (toàn bộ còn PENDING)
     * để chỉnh đội hình rồi đăng ký lại.
     */
    @Operation(summary = "Withdraw the team's event registration while still pending (leader only)")
    @DeleteMapping("/teams/{teamId}/register-event")
    public ResponseEntity<java.util.Map<String, Object>> withdrawTeamRegistration(
            @PathVariable UUID teamId,
            Authentication authentication
    ) {
        teamEventRegistrationService.withdrawTeamRegistration(teamId, currentUserId(authentication));
        return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "message", "Team registration withdrawn"
        ));
    }

    // Quyen hien tai: chi tai khoan dang la member active cua teamId.
    // userId duoc xem cung phai la member active cua chinh team do.
    // Seed test Alpha: dang nhap api.alpha.leader@seal.test hoac api.alpha.member@seal.test.
    // userId hop le: leader A1000000-0000-0000-0000-000000000010,
    // member A1000000-0000-0000-0000-000000000011. Mat khau: Test@123.
    @Operation(summary = "Get team member details")
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

    // Quyen hien tai: moi tai khoan co JWT hop le chua o team active trong cung event.
    // Team dich phai con nhan thanh vien va user chua co request PENDING trung lap.
    // Seed test thanh cong: api.join.member@seal.test, api.join.leader@seal.test
    // hoac api.join.guest@seal.test / Test@123; gui vao Alpha hoac Beta.
    // Moi tai khoan chi nen dung cho mot join request khi test, de tranh request PENDING trung lap.
    @Operation(summary = "Request to join a team")
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

    // Quyen hien tai: chi tai khoan la leader cua teamId.
    // Seed test Alpha: api.alpha.leader@seal.test / Test@123.
    // Alpha da co request PENDING cua api.applicant@seal.test trong seed.
    // Seed test Beta: api.beta.leader@seal.test sau khi mot tai khoan api.join.* gui request vao Beta.
    @Operation(summary = "Get pending join requests")
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

    // Quyen hien tai: chi tai khoan la leader cua team nhan request.
    // Seed test co san: dang nhap api.alpha.leader@seal.test / Test@123 va xu ly
    // request E3000000-0000-0000-0000-000000000001 cua api.applicant@seal.test.
    // Body mau: {"action":"APPROVED","responseNote":"Accepted"} hoac action REJECTED.
    @Operation(summary = "Approve or reject a join request")
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

    // Nguoi xin tu huy request PENDING cua chinh minh (khong cho huy cua nguoi khac).
    @Operation(summary = "Cancel my own pending join request")
    @DeleteMapping("/teams/requests/{requestId}")
    public ResponseEntity<JoinTeamRequestResponse> cancelJoinRequest(
            @PathVariable UUID requestId,
            Authentication authentication
    ) {
        JoinTeamRequestResponse response =
                teamJoinRequestService.cancelJoinRequest(requestId, currentUserId(authentication));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all teams the current user belongs to")
    @GetMapping("/teams/mine")
    public ResponseEntity<List<TeamResponse>> getMyTeams(Authentication authentication) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(teamService.getTeamsByUserId(userId));
    }

    // Nguoi xin xem cac request PENDING cua minh de biet trang thai va co the huy.
    @Operation(summary = "List my pending join requests")
    @GetMapping("/teams/requests/mine")
    public ResponseEntity<List<JoinTeamRequestResponse>> getMyPendingJoinRequests(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                teamJoinRequestService.getMyPendingJoinRequests(currentUserId(authentication))
        );
    }

    // Leader giai tan team dang FORMING trong mot thao tac (thay vi kick tung nguoi):
    // go dang ky event PENDING cua tung thanh vien roi xoa team + request + membership.
    @Operation(summary = "Disband a forming team (leader only)")
    @PostMapping("/teams/{teamId}/disband")
    public ResponseEntity<Void> disbandTeam(
            @PathVariable UUID teamId,
            Authentication authentication
    ) {
        teamService.disbandTeam(teamId, currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    // Leader duoc kick member hoac tu roi; member duoc tu roi team.
    // Neu leader roi, service tu chuyen quyen hoac xoa team FORMING neu khong con ai.
    @Operation(summary = "Remove a member or leave a team")
    @DeleteMapping("/teams/{teamId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID teamId,
            @PathVariable UUID userId,
            @RequestBody(required = false) RemoveTeamMemberRequest request,
            Authentication authentication
    ) {
        // Service phan biet leader kick member va member tu roi team.
        teamService.removeMember(
                teamId,
                userId,
                currentUserId(authentication),
                request != null ? request.getReason() : null
        );
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Transfer team leadership",
            description = "The current leader transfers leadership to another active member of the same team."
    )
    @PutMapping("/teams/{teamId}/leader")
    public ResponseEntity<TeamResponse> transferLeadership(
            @PathVariable UUID teamId,
            @Valid @RequestBody TransferTeamLeadershipRequest request,
            Authentication authentication
    ) {
        TeamResponse response = teamService.transferLeadership(
                teamId,
                request.getNewLeaderUserId(),
                currentUserId(authentication)
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Disqualify a team",
            description = "Mark a team as disqualified and record the reason. Use an organizer account."
    )
    @PostMapping("/admin/teams/{teamId}/disqualify")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<DisqualificationResponse> disqualifyTeam(
            @PathVariable UUID teamId,
            @Valid @RequestBody DisqualifyTeamRequest request,
            Authentication authentication
    ) {
        DisqualificationResponse response = teamDisqualificationService.disqualifyTeam(
                teamId,
                request,
                currentUserId(authentication)
        );
        return ResponseEntity.ok(response);
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
