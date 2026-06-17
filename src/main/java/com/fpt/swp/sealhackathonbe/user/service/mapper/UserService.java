package com.fpt.swp.sealhackathonbe.user.service.mapper;

import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;
import com.fpt.swp.sealhackathonbe.user.dto.ApproveUserRequest;

import java.util.UUID;

public interface UserService {
    UserResponse approveUser(UUID userId, ApproveUserRequest request);
}
