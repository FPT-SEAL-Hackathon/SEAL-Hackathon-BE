package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class JoinTeamRequestResponse {
    // DTO trả trạng thái đơn xin vào team cho cả luồng tạo request, xem pending và xử lý request.
    private UUID requestId;
    private UUID teamId;
    private UUID userId;
    private String fullName;
    private String universityName;
    private String requestStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
    private UUID respondedById;
    private String responseNote;
}
