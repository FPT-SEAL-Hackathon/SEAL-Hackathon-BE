package com.fpt.swp.sealhackathonbe.user.service.impl;

import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;
import com.fpt.swp.sealhackathonbe.user.entity.AccountStatus;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserPrincipal;
import com.fpt.swp.sealhackathonbe.user.dto.ApproveUserRequest;
import com.fpt.swp.sealhackathonbe.user.repository.AccountStatusRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import com.fpt.swp.sealhackathonbe.user.service.mapper.UserService;
import com.fpt.swp.sealhackathonbe.core.exception.ApiException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private static final UUID ORGANIZER_ID = UUID.fromString(
            "10000000-0000-0000-0000-000000000003"
    );
    private static final UUID ACCOUNT_STATUS_ACTIVE_ID = UUID.fromString(
            "20000000-0000-0000-0000-000000000002"
    );

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AccountStatusRepository accountStatusRepo;

    @Override
    @Transactional
    public UserResponse approveUser(UUID userId, ApproveUserRequest request) {
        if (!ORGANIZER_ID.equals(request.getUserTypeId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only organizer can approve accounts");
        }

        User approver = getCurrentUser();
        if (approver.getUserType() == null || !ORGANIZER_ID.equals(approver.getUserType().getUserTypeId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only organizer can approve accounts");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        AccountStatus activeStatus = accountStatusRepo
                .findById(ACCOUNT_STATUS_ACTIVE_ID)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Account status not found"));

        user.setAccountStatus(activeStatus);
        user.setApprovedAt(LocalDateTime.now());
        user.setApprovedBy(approver);

        return toUserResponse(userRepo.save(user));
    }

    private UserResponse toUserResponse(User user) {
        String studentCode = user.getFptStudentCode() != null
                ? user.getFptStudentCode()
                : user.getExternalStudentCode();

        return UserResponse.builder()
                .id(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .userTypeId(user.getUserType().getUserTypeId())
                .userType(user.getUserType().getTypeName())
                .studentCode(studentCode)
                .universityName(user.getUniversityName())
                .phone(user.getPhone())
                .accountStatusId(user.getAccountStatus().getStatusId())
                .accountStatus(user.getAccountStatus().getStatusName())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        return userPrincipal.getUser();
    }
}
