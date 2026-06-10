package com.fpt.swp.sealhackathonbe.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserResponse {

    private UUID id;

    private String email;

    private String fullName;
}