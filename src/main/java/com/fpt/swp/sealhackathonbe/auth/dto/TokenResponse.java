package com.fpt.swp.sealhackathonbe.auth.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TokenResponse {
    private String accessToken;
}