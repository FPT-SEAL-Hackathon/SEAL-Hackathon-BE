package com.fpt.swp.sealhackathonbe.user.controller;

import com.fpt.swp.sealhackathonbe.auth.dto.LoginResponse;
import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;
import com.fpt.swp.sealhackathonbe.auth.service.impl.AuthenticationServiceImpl;
import com.fpt.swp.sealhackathonbe.user.dto.CompleteProfileRequest;
import com.fpt.swp.sealhackathonbe.user.dto.LinkCandidateResponse;
import com.fpt.swp.sealhackathonbe.user.dto.LinkLocalAccountRequest;
import com.fpt.swp.sealhackathonbe.user.dto.SetPasswordRequest;
import com.fpt.swp.sealhackathonbe.user.service.UserLinkService;
import com.fpt.swp.sealhackathonbe.user.service.UserPasswordService;
import com.fpt.swp.sealhackathonbe.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * API tự phục vụ cho user hiện tại (bao gồm user OAuth TEMPORARY):
 * xem hồ sơ, hoàn thiện hồ sơ, liên kết tài khoản local, đặt mật khẩu local.
 * Mapping "/api/v1/users/me" là exact-match nên không đụng
 * "/api/v1/users/{userId}" (chỉ dành cho ORGANIZER).
 */
@Tag(name = "Current User", description = "Self-service APIs for the authenticated user")
@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final AuthenticationServiceImpl authenticationService;
    private final UserProfileService userProfileService;
    private final UserLinkService userLinkService;
    private final UserPasswordService userPasswordService;

    @Operation(summary = "Get current user")
    @GetMapping
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(
                userProfileService.getCurrentUser(authenticationService.getCurrentUser())
        );
    }

    @Operation(summary = "Complete profile (TEMPORARY OAuth user chooses student role)")
    @PutMapping("/complete-profile")
    public ResponseEntity<UserResponse> completeProfile(
            @Valid @RequestBody CompleteProfileRequest request
    ) {
        return ResponseEntity.ok(
                userProfileService.completeProfile(authenticationService.getCurrentUser(), request)
        );
    }

    @Operation(summary = "Update profile (for existing user)")
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody com.fpt.swp.sealhackathonbe.user.dto.UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(
                userProfileService.updateProfile(authenticationService.getCurrentUser(), request)
        );
    }

    @Operation(summary = "List local accounts that can be linked (matching email)")
    @GetMapping("/link-candidates")
    public ResponseEntity<List<LinkCandidateResponse>> linkCandidates() {
        return ResponseEntity.ok(
                userLinkService.linkCandidates(authenticationService.getCurrentUser())
        );
    }

    @Operation(summary = "Link this OAuth account into an existing local account (password required)")
    @PostMapping("/link-local-account")
    public ResponseEntity<LoginResponse> linkLocalAccount(
            @Valid @RequestBody LinkLocalAccountRequest request
    ) {
        return ResponseEntity.ok(
                userLinkService.linkLocalAccount(authenticationService.getCurrentUser(), request)
        );
    }

    @Operation(summary = "Set local password (first-time for OAuth-only accounts)")
    @PostMapping("/set-password")
    public ResponseEntity<Map<String, Object>> setPassword(
            @Valid @RequestBody SetPasswordRequest request
    ) {
        userPasswordService.setPassword(authenticationService.getCurrentUser(), request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password updated successfully"
        ));
    }
}
