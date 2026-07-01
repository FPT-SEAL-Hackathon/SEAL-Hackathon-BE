package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.auth.entity.AuditLog;
import com.fpt.swp.sealhackathonbe.auth.repository.AuditLogRepository;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.user.dto.CreateUserManagementRequest;
import com.fpt.swp.sealhackathonbe.user.dto.UpdateUserManagementRequest;
import com.fpt.swp.sealhackathonbe.user.dto.UpdateUserRoleRequest;
import com.fpt.swp.sealhackathonbe.user.dto.UserManagementResponse;
import com.fpt.swp.sealhackathonbe.user.entity.AccountStatus;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserType;
import com.fpt.swp.sealhackathonbe.user.repository.AccountStatusRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    private static final String STATUS_ACTIVE = "Active";
    private static final String STATUS_UNVERIFIED = "Unverified";
    private static final String STATUS_PENDING_APPROVAL = "Pending Approval";
    private static final String ROLE_FPT_STUDENT = "FPT Student";
    private static final String ROLE_EXTERNAL_STUDENT = "External Student";
    private static final String ROLE_ORGANIZER = "Organizer";
    private static final String ROLE_INTERNAL_JUDGE = "Internal Judge";
    private static final String ROLE_GUEST_JUDGE = "Guest Judge";
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final AccountStatusRepository accountStatusRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserService userService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Transactional(readOnly = true)
    public Page<UserManagementResponse> search(
            String search,
            String role,
            UUID teamId,
            String teamName,
            String status,
            LocalDate joinedFrom,
            LocalDate joinedTo,
            Pageable pageable
    ) {
        if (joinedFrom != null && joinedTo != null && joinedFrom.isAfter(joinedTo)) {
            throw new BadRequestException("joinedFrom must be before or equal to joinedTo");
        }

        String roleName = role == null || role.isBlank() ? null : resolveUserType(role).getTypeName();
        String statusName = status == null || status.isBlank() ? null : resolveAccountStatus(status, true).getStatusName();
        LocalDateTime from = joinedFrom == null ? null : joinedFrom.atStartOfDay();
        LocalDateTime to = joinedTo == null ? null : joinedTo.plusDays(1).atStartOfDay().minusNanos(1);

        return userRepository.searchForManagement(
                        trimToNull(search),
                        roleName,
                        teamId,
                        trimToNull(teamName),
                        statusName,
                        from,
                        to,
                        pageable
                )
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UserManagementResponse getById(UUID userId) {
        return toResponse(getUser(userId));
    }

    @Transactional
    public UserManagementResponse create(CreateUserManagementRequest request, UUID actorUserId) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new BusinessConflictException("Email already exists.");
        }
        validatePhone(request.getPhone());
        validatePassword(request.getPassword());

        UserType userType = resolveUserType(request.getRole());
        String requestedStatus = firstNonBlank(request.getAccountStatus(), request.getStatus());
        AccountStatus accountStatus = requestedStatus == null
                ? resolveAccountStatus(STATUS_UNVERIFIED, false)
                : resolveAccountStatus(requestedStatus, true);

        User user = new User();
        user.setEmail(email);
        user.setFullName(request.getFullName().trim());
        user.setPhone(trimToNull(request.getPhone()));
        user.setUserType(userType);
        user.setAccountStatus(accountStatus);
        user.setPasswordHash(encoder.encode(request.getPassword()));
        user.setAccountExpiresAt(request.getAccountExpiresAt());
        applyRoleSpecificFields(
                user,
                userType,
                request.getFptStudentCode(),
                request.getExternalStudentCode(),
                request.getUniversityName()
        );
        user.setIsDeleted(false);

        User savedUser = userRepository.save(user);
        if (isUnverified(accountStatus)) {
            userService.createAndSendVerificationToken(savedUser);
        }

        writeAudit("USER_CREATED", savedUser.getUserId(), actorUserId, null,
                "{\"email\":\"" + savedUser.getEmail() + "\",\"role\":\"" + userType.getTypeName() + "\"}");
        return toResponse(savedUser);
    }

    @Transactional
    public UserManagementResponse update(UUID userId, UpdateUserManagementRequest request, UUID actorUserId) {
        User user = getUser(userId);
        String oldValue = snapshot(user);

        if (request.getFullName() != null) {
            String fullName = request.getFullName().trim();
            if (fullName.isBlank()) {
                throw new BadRequestException("Full name must not be blank");
            }
            user.setFullName(fullName);
        }
        if (request.getPhone() != null) {
            validatePhone(request.getPhone());
            user.setPhone(trimToNull(request.getPhone()));
        }
        String requestedFptStudentCode = request.getFptStudentCode() != null
                ? request.getFptStudentCode()
                : user.getFptStudentCode();
        String requestedExternalStudentCode = request.getExternalStudentCode() != null
                ? request.getExternalStudentCode()
                : user.getExternalStudentCode();
        String requestedUniversityName = request.getUniversityName() != null
                ? request.getUniversityName()
                : user.getUniversityName();
        if (request.getRole() != null && !request.getRole().isBlank()) {
            UserType newType = resolveUserType(request.getRole());
            user.setUserType(newType);
            applyRoleSpecificFields(
                    user,
                    newType,
                    requestedFptStudentCode,
                    requestedExternalStudentCode,
                    requestedUniversityName
            );
        } else if (request.getFptStudentCode() != null
                || request.getExternalStudentCode() != null
                || request.getUniversityName() != null) {
            applyRoleSpecificFields(
                    user,
                    user.getUserType(),
                    requestedFptStudentCode,
                    requestedExternalStudentCode,
                    requestedUniversityName
            );
        }
        String requestedStatus = firstNonBlank(request.getAccountStatus(), request.getStatus());
        if (requestedStatus != null) {
            AccountStatus status = resolveAccountStatus(requestedStatus, true);
            validateNotSelfDisable(user, status, actorUserId);
            user.setAccountStatus(status);
        }
        if (request.getAccountExpiresAt() != null) {
            user.setAccountExpiresAt(request.getAccountExpiresAt());
        }

        User savedUser = userRepository.save(user);
        writeAudit("USER_UPDATED", savedUser.getUserId(), actorUserId, oldValue, snapshot(savedUser));
        return toResponse(savedUser);
    }

    @Transactional
    public UserManagementResponse updateStatus(UUID userId, String statusName, UUID actorUserId) {
        User user = getUser(userId);
        String oldValue = snapshot(user);
        AccountStatus status = resolveAccountStatus(statusName, true);
        validateNotSelfDisable(user, status, actorUserId);
        user.setAccountStatus(status);

        User savedUser = userRepository.save(user);
        writeAudit("USER_STATUS_CHANGED", savedUser.getUserId(), actorUserId, oldValue, snapshot(savedUser));
        return toResponse(savedUser);
    }

    @Transactional
    public UserManagementResponse updateRole(UUID userId, UpdateUserRoleRequest request, UUID actorUserId) {
        User user = getUser(userId);
        String oldValue = snapshot(user);
        UserType newType = resolveUserType(request.getRole());
        user.setUserType(newType);
        applyRoleSpecificFields(
                user,
                newType,
                request.getFptStudentCode() != null ? request.getFptStudentCode() : user.getFptStudentCode(),
                request.getExternalStudentCode() != null ? request.getExternalStudentCode() : user.getExternalStudentCode(),
                request.getUniversityName() != null ? request.getUniversityName() : user.getUniversityName()
        );

        User savedUser = userRepository.save(user);
        writeAudit("USER_ROLE_CHANGED", savedUser.getUserId(), actorUserId, oldValue, snapshot(savedUser));
        return toResponse(savedUser);
    }

    @Transactional
    public void delete(UUID userId, UUID actorUserId) {
        User user = getUser(userId);
        if (user.getUserId().equals(actorUserId)) {
            throw new AccessDeniedException("Organizer cannot delete their own account");
        }

        String oldValue = snapshot(user);
        user.setIsDeleted(true);
        accountStatusRepository.findByStatusNameIgnoreCase("Suspended")
                .ifPresent(user::setAccountStatus);
        userRepository.save(user);
        writeAudit("USER_DEACTIVATED", user.getUserId(), actorUserId, oldValue, snapshot(user));
    }

    private User getUser(UUID userId) {
        return userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private UserType resolveUserType(String roleName) {
        String normalized = normalizeLookup(roleName);
        if (normalized.isBlank()) {
            throw new BadRequestException("Invalid user role.");
        }
        return userTypeRepository.findAll().stream()
                .filter(userType -> normalizeLookup(userType.getTypeName()).equals(normalized))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Invalid user role."));
    }

    private AccountStatus resolveAccountStatus(String statusName, boolean rejectApprovalStatus) {
        String normalized = normalizeLookup(statusName);
        if (normalized.isBlank()) {
            throw new BadRequestException("Invalid account status.");
        }
        AccountStatus status = accountStatusRepository.findAll().stream()
                .filter(accountStatus -> normalizeLookup(accountStatus.getStatusName()).equals(normalized))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Invalid account status."));

        if (rejectApprovalStatus && normalizeLookup(STATUS_PENDING_APPROVAL).equals(normalizeLookup(status.getStatusName()))) {
            throw new BadRequestException("User account approval is obsolete; use email verification or another account status");
        }

        return status;
    }

    private void validateNotSelfDisable(User user, AccountStatus newStatus, UUID actorUserId) {
        if (!user.getUserId().equals(actorUserId)) {
            return;
        }
        if (!STATUS_ACTIVE.equalsIgnoreCase(newStatus.getStatusName())) {
            throw new AccessDeniedException("Organizer cannot disable their own account");
        }
    }

    private UserManagementResponse toResponse(User user) {
        Optional<TeamMembers> currentMembership =
                teamMembersRepository.findFirstByUserIdAndActiveTrueOrderByJoinedAtDesc(user.getUserId());
        String roleName = user.getUserType() != null ? user.getUserType().getTypeName() : null;
        String statusName = user.getAccountStatus() != null ? user.getAccountStatus().getStatusName() : null;

        return UserManagementResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(toApiName(roleName))
                .roleName(roleName)
                .teamId(currentMembership.map(TeamMembers::getTeamId).orElse(null))
                .teamName(currentMembership.map(TeamMembers::getTeam).map(team -> team.getTeamName()).orElse(null))
                .teamStatus(currentMembership
                        .map(TeamMembers::getTeam)
                        .map(team -> team.getTeamStatus() != null ? team.getTeamStatus().getStatusName() : null)
                        .orElse(null))
                .accountStatus(toApiName(statusName))
                .accountStatusName(statusName)
                .fptStudentCode(user.getFptStudentCode())
                .externalStudentCode(user.getExternalStudentCode())
                .universityName(user.getUniversityName())
                .accountExpiresAt(user.getAccountExpiresAt())
                .emailVerified(STATUS_ACTIVE.equalsIgnoreCase(statusName))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private boolean isUnverified(AccountStatus status) {
        return STATUS_UNVERIFIED.equalsIgnoreCase(status.getStatusName());
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String firstNonBlank(String first, String second) {
        String normalizedFirst = trimToNull(first);
        return normalizedFirst != null ? normalizedFirst : trimToNull(second);
    }

    private String normalizeLookup(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replace("-", "_")
                .replace(" ", "_")
                .toUpperCase(Locale.ROOT);
    }

    private String toApiName(String value) {
        if (value == null) {
            return null;
        }
        return normalizeLookup(value);
    }

    private String snapshot(User user) {
        return "{\"email\":\"" + user.getEmail()
                + "\",\"role\":\"" + (user.getUserType() != null ? user.getUserType().getTypeName() : null)
                + "\",\"status\":\"" + (user.getAccountStatus() != null ? user.getAccountStatus().getStatusName() : null)
                + "\",\"isDeleted\":" + user.getIsDeleted() + "}";
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Password is required.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new BadRequestException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
    }

    private void validatePhone(String phone) {
        String normalizedPhone = trimToNull(phone);
        if (normalizedPhone == null) {
            return;
        }
        if (!normalizedPhone.matches("^[0-9+()\\-\\s]{7,20}$")) {
            throw new BadRequestException("Invalid phone number.");
        }
    }

    private void applyRoleSpecificFields(
            User user,
            UserType userType,
            String fptStudentCode,
            String externalStudentCode,
            String universityName
    ) {
        String typeName = userType != null ? userType.getTypeName() : null;
        if (ROLE_FPT_STUDENT.equalsIgnoreCase(typeName)) {
            String requiredFptStudentCode = trimToNull(fptStudentCode);
            if (requiredFptStudentCode == null) {
                throw new BadRequestException("FPT student code is required for FPT Student users.");
            }
            user.setFptStudentCode(requiredFptStudentCode);
            user.setExternalStudentCode(null);
            user.setUniversityName(null);
            return;
        }

        if (ROLE_EXTERNAL_STUDENT.equalsIgnoreCase(typeName)) {
            String requiredExternalStudentCode = trimToNull(externalStudentCode);
            String requiredUniversityName = trimToNull(universityName);
            if (requiredExternalStudentCode == null || requiredUniversityName == null) {
                throw new BadRequestException("External student code and university name are required for External Student users.");
            }
            user.setFptStudentCode(null);
            user.setExternalStudentCode(requiredExternalStudentCode);
            user.setUniversityName(requiredUniversityName);
            return;
        }

        if (ROLE_ORGANIZER.equalsIgnoreCase(typeName)
                || ROLE_INTERNAL_JUDGE.equalsIgnoreCase(typeName)
                || ROLE_GUEST_JUDGE.equalsIgnoreCase(typeName)) {
            user.setFptStudentCode(null);
            user.setExternalStudentCode(null);
            user.setUniversityName(null);
            return;
        }

        throw new BadRequestException("Invalid user role.");
    }

    private void writeAudit(String action, UUID userId, UUID actorUserId, String oldValue, String newValue) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActionType(action);
        auditLog.setEntityType("Users");
        auditLog.setEntityId(userId);
        auditLog.setActorUserId(actorUserId);
        auditLog.setOldValueJson(oldValue);
        auditLog.setNewValueJson(newValue);
        auditLog.setOccurredAt(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }
}
