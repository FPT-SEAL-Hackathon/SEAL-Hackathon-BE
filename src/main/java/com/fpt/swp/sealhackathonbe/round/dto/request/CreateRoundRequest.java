package com.fpt.swp.sealhackathonbe.round.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CreateRoundRequest {
    private String roundName;
    private String description;

    /**
     * CHỈ ĐỂ TƯƠNG THÍCH NGƯỢC — service BỎ QUA giá trị này và luôn gán max(roundOrder)+1
     * trong cùng category, để thứ tự vòng không bao giờ trùng/thủng. Client không cần gửi.
     */
    @Min(value = 1, message = "Round order must be at least 1")
    private Integer roundOrder;
    private UUID roundStatusId;
    private LocalDateTime submissionDeadline;
    private LocalDateTime judgingDeadline;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime appealStartTime;
    private LocalDateTime appealEndTime;
    // Số đội đi tiếp. Không chặn trần theo số đội hiện có: round thường được tạo lúc setup
    // event khi chưa đội nào đăng ký (trần sẽ là 0). Ràng buộc thực tế do RankingServiceImpl
    // xử lý lúc tính rank — nó chỉ cho đi tiếp trong số đội thực sự có mặt.
    @Min(value = 1, message = "Advancement Top N must be at least 1")
    private Integer advancementTopN;
    private Boolean isCalibrationRound;
}
