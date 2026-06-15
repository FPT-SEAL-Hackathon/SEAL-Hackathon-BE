package com.fpt.swp.sealhackathonbe.user.controller;

import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;
import com.fpt.swp.sealhackathonbe.user.dto.ApproveUserRequest;
import com.fpt.swp.sealhackathonbe.user.service.mapper.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PatchMapping("/{userId}/approve")
    public ResponseEntity<UserResponse> approveUser(
            @PathVariable UUID userId,
            @Valid @RequestBody ApproveUserRequest request
    ) {
        return ResponseEntity.ok(userService.approveUser(userId, request));
    }
}
