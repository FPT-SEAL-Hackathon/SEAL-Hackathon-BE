package com.fpt.swp.sealhackathonbe.appeal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AppealRequestDTO {
    @NotNull(message = "Event ID is required")
    private UUID eventId;
    
    @NotNull(message = "Category ID is required")
    private UUID categoryId;
    
    @NotNull(message = "Round ID is required")
    private UUID roundId;
    
    @NotNull(message = "Team ID is required")
    private UUID teamId;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Reason is required")
    private String reason;

    @NotNull(message = "Appeal type is required")
    private com.fpt.swp.sealhackathonbe.appeal.entity.AppealType appealType;
}
