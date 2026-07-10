package com.fpt.swp.sealhackathonbe.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Gợi ý tài khoản local có thể liên kết (thường là trùng email).
 * Không trả dữ liệu nhạy cảm — chỉ đủ để user nhận ra tài khoản của mình.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkCandidateResponse {

    private String email;
    private String fullName;
    private String role;
    private String roleName;
    private String matchedBy;
}
