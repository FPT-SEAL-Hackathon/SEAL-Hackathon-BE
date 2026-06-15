package com.fpt.swp.sealhackathonbe.auth.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;

    private String email;

    private String fullName;

    private String phone;

    private String userType;

    private String accountStatus;

    private String studentCode;

    private String universityName;

    private LocalDateTime createdAt;
}