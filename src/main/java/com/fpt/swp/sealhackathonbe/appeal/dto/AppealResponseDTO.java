package com.fpt.swp.sealhackathonbe.appeal.dto;

import com.fpt.swp.sealhackathonbe.appeal.entity.AppealStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AppealResponseDTO {
    private UUID appealId;
    private UUID teamId;
    private String teamName;
    private UUID eventId;
    private String eventName;
    private UUID categoryId;
    private String categoryName;
    private String title;
    private String reason;
    private com.fpt.swp.sealhackathonbe.appeal.entity.AppealType appealType;
    private AppealStatus status;
    private String resolutionNote;
    private UUID resolvedBy;
    private String resolvedByName;
    private UUID createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
