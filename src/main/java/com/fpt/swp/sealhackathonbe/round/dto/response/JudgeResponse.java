package com.fpt.swp.sealhackathonbe.round.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class JudgeResponse {
    private UUID judgeId;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String roleName;
}
