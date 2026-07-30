package com.fpt.swp.sealhackathonbe.round.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class UpdateRoundRequest {
    private String roundName;
    private String description;

    // null = giữ nguyên thứ tự hiện tại. Giá trị gửi lên phải >= 1 và không trùng round khác
    // trong cùng category (kiểm tra ở RoundServiceImpl.update).
    @Min(value = 1, message = "Round order must be at least 1")
    private Integer roundOrder;
    private UUID roundStatusId;
    private LocalDateTime submissionDeadline;
    private LocalDateTime judgingDeadline;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime appealStartTime;
    private LocalDateTime appealEndTime;
    // Xem ghi chú ở CreateRoundRequest: chỉ chặn sàn, không chặn trần theo số đội.
    @Min(value = 1, message = "Advancement Top N must be at least 1")
    private Integer advancementTopN;
    private Boolean isCalibrationRound;
}
