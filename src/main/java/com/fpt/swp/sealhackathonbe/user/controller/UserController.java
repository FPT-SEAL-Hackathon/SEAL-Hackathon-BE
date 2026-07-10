package com.fpt.swp.sealhackathonbe.user.controller;

import com.fpt.swp.sealhackathonbe.auth.service.impl.AuthenticationServiceImpl;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.user.dto.CreateUserManagementRequest;
import com.fpt.swp.sealhackathonbe.user.dto.UpdateUserManagementRequest;
import com.fpt.swp.sealhackathonbe.user.dto.UpdateUserRoleRequest;
import com.fpt.swp.sealhackathonbe.user.dto.UpdateUserStatusRequest;
import com.fpt.swp.sealhackathonbe.user.dto.UserManagementResponse;
import com.fpt.swp.sealhackathonbe.user.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Tag(name = "User Management", description = "Organizer APIs for managing user accounts")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
public class UserController {
    private static final Map<String, String> SORT_FIELDS = Map.of(
            "fullName", "fullName",
            "email", "email",
            "role", "userType.typeName",
            "status", "accountStatus.statusName",
            "createdAt", "createdAt",
            "joinedAt", "createdAt",
            "updatedAt", "updatedAt"
    );

    private final UserManagementService userManagementService;
    private final AuthenticationServiceImpl authenticationService;

    @Operation(summary = "Search users")
    @GetMapping
    public ResponseEntity<Page<UserManagementResponse>> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joinedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joinedTo,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<UserManagementResponse> response = userManagementService.search(
                search,
                role,
                teamId,
                teamName,
                status,
                joinedFrom,
                joinedTo,
                toPageable(page, size, sortBy, sortDir)
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{userId}")
    public ResponseEntity<UserManagementResponse> getById(@PathVariable UUID userId) {
        return ResponseEntity.ok(userManagementService.getById(userId));
    }

    @Operation(summary = "Create user")
    @PostMapping
    public ResponseEntity<UserManagementResponse> create(
            @Valid @RequestBody CreateUserManagementRequest request
    ) {
        UserManagementResponse response = userManagementService.create(request, currentUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user profile")
    @PutMapping("/{userId}")
    public ResponseEntity<UserManagementResponse> update(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserManagementRequest request
    ) {
        UserManagementResponse response = userManagementService.update(userId, request, currentUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user account status")
    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserManagementResponse> updateStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        UserManagementResponse response =
                userManagementService.updateStatus(userId, request.getStatus(), currentUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user role")
    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserManagementResponse> updateRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        UserManagementResponse response =
                userManagementService.updateRole(userId, request, currentUserId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deactivate user")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID userId) {
        userManagementService.delete(userId, currentUserId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deactivated successfully"
        ));
    }

    private Pageable toPageable(int page, int size, String sortBy, String sortDir) {
        if (!SORT_FIELDS.containsKey(sortBy)) {
            throw new BadRequestException("Invalid sort field.");
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));
        Sort.Direction direction;
        if ("asc".equalsIgnoreCase(sortDir)) {
            direction = Sort.Direction.ASC;
        } else if ("desc".equalsIgnoreCase(sortDir)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new BadRequestException("sortDir must be asc or desc");
        }

        return PageRequest.of(safePage, safeSize, Sort.by(direction, SORT_FIELDS.get(sortBy)));
    }

    private UUID currentUserId() {
        return authenticationService.getCurrentUser().getUserId();
    }
}
