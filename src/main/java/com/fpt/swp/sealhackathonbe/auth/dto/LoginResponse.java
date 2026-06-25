package com.fpt.swp.sealhackathonbe.auth.dto;

import lombok.*;
import org.hibernate.validator.constraints.UUID;

/**
 * Kết quả đăng nhập gồm JWT và thông tin hồ sơ an toàn.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;

    private String refreshToken;

    private UserResponse user;


}
