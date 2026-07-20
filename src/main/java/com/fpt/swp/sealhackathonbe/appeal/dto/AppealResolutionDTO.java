package com.fpt.swp.sealhackathonbe.appeal.dto;

import com.fpt.swp.sealhackathonbe.appeal.entity.AppealStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppealResolutionDTO {
    @NotNull(message = "Status is required")
    private AppealStatus status;
    
    @NotBlank(message = "Resolution note is required")
    private String resolutionNote;
}
