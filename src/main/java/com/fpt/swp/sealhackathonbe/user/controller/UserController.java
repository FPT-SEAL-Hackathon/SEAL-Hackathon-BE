package com.fpt.swp.sealhackathonbe.user.controller;

import com.fpt.swp.sealhackathonbe.auth.service.impl.AuthenticationServiceImpl;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.user.dto.CreateUserManagementRequest;
import com.fpt.swp.sealhackathonbe.user.dto.UpdateUserManagementRequest;
import com.fpt.swp.sealhackathonbe.user.dto.UpdateUserRoleRequest;
import com.fpt.swp.sealhackathonbe.user.dto.UpdateUserStatusRequest;
import com.fpt.swp.sealhackathonbe.user.dto.UserFacetsResponse;
import com.fpt.swp.sealhackathonbe.user.dto.UserManagementResponse;
import com.fpt.swp.sealhackathonbe.user.service.UserHardDeleteService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "User Management", description = "Admin APIs for managing user accounts")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
// Quan ly nguoi dung la quan tri HE THONG -> chuyen tu ORGANIZER sang ADMIN.
// Organizer chi van hanh cuoc thi (event/round/cham diem), khong dung vao tai khoan.
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
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
    private final UserHardDeleteService userHardDeleteService;
    private final AuthenticationServiceImpl authenticationService;

    @Operation(summary = "Search users (role/status nhận nhiều giá trị phân tách bằng dấu phẩy)")
    @GetMapping
    public ResponseEntity<Page<UserManagementResponse>> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String accountStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joinedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joinedTo,
            // Cho admin xem cả account đã soft-delete (hiển thị mờ, readonly);
            // default false để không đổi contract với client cũ.
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<UserManagementResponse> response = userManagementService.search(
                search,
                splitCsv(role),
                teamId,
                teamName,
                // Alias: một số client cũ gửi "accountStatus" thay vì "status".
                splitCsv(firstNonBlankParam(status, accountStatus)),
                joinedFrom,
                joinedTo,
                includeDeleted,
                toPageable(page, size, sortBy, sortDir)
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Facet counts cho panel filter: số lượng cạnh mỗi option (drill-down)
     * + total để preview "Show N users" trước khi xác nhận.
     */
    @Operation(summary = "User facet counts for the filter panel")
    @GetMapping("/facets")
    public ResponseEntity<UserFacetsResponse> facets(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String accountStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joinedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joinedTo
    ) {
        return ResponseEntity.ok(userManagementService.facets(
                search,
                splitCsv(role),
                teamId,
                teamName,
                splitCsv(firstNonBlankParam(status, accountStatus)),
                joinedFrom,
                joinedTo
        ));
    }

    // "FPT_STUDENT,ORGANIZER" -> [FPT_STUDENT, ORGANIZER]; giá trị đơn vẫn hợp lệ.
    private List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }

    private String firstNonBlankParam(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
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

    @Operation(summary = "Scan accounts with non-standard profile and notify them to update (no blocking)")
    @PostMapping("/notify-noncompliant")
    public ResponseEntity<Map<String, Object>> notifyNonCompliant() {
        int notified = userManagementService.notifyNonCompliantUsers(currentUserId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "notifiedCount", notified,
                "message", "Notified " + notified + " account(s) to update their profile."
        ));
    }

    @Operation(summary = "Deactivate user")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID userId) {
        com.fpt.swp.sealhackathonbe.user.dto.DeactivateUserResult result =
                userManagementService.delete(userId, currentUserId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deactivated successfully",
                "transferredTeams", result.getTransferredTeams(),
                "warnings", result.getWarnings()
        ));
    }

    @Operation(summary = "Hard delete ALL accounts with this email (dev tool): "
            + "removes personal data permanently, keeps collective data, "
            + "notifies remaining team members; email is reusable immediately")
    @DeleteMapping("/hard-delete")
    public ResponseEntity<Map<String, Object>> hardDelete(
            @RequestParam String email,
            @RequestParam(required = false) String reason
    ) {
        int deletedAccounts = userHardDeleteService.hardDeleteByEmail(email, currentUserId(), reason);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User permanently deleted",
                "deletedAccounts", deletedAccounts
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
