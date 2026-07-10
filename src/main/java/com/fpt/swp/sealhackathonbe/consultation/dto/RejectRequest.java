package com.fpt.swp.sealhackathonbe.consultation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectRequest {
    @NotBlank
    private String reason;
}
