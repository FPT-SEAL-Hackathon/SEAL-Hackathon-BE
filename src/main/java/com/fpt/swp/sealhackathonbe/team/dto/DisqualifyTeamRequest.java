package com.fpt.swp.sealhackathonbe.team.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DisqualifyTeamRequest {
    // Request admin gửi lên khi loại team; reason sẽ được lưu vào bảng Disqualifications.
    @NotBlank(message = "Reason is required")
    private String reason;
}
