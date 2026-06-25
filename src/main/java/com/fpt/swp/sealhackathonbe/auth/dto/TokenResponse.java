package com.fpt.swp.sealhackathonbe.auth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Kết quả trả về khi cấp mới access token.
 */
@Builder
@Data
public class TokenResponse {
    private String accessToken;
}
