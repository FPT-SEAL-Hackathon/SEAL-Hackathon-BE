package com.fpt.swp.sealhackathonbe.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Code trao đổi một lần do backend phát sau khi OAuth thành công.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2ExchangeRequest {

    @NotBlank(message = "Code is required")
    private String code;
}
