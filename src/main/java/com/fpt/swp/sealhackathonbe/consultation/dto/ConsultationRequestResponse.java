package com.fpt.swp.sealhackathonbe.consultation.dto;

import com.fpt.swp.sealhackathonbe.consultation.entity.ConsultationPriority;
import com.fpt.swp.sealhackathonbe.consultation.entity.ConsultationRequest;
import com.fpt.swp.sealhackathonbe.consultation.entity.ConsultationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ConsultationRequestResponse {
    private UUID id;
    private UUID eventId;
    private String eventName;
    private UUID categoryId;
    private String categoryName;
    private UUID teamId;
    private String teamName;
    private UUID mentorId;
    private String mentorName;
    private UUID createdByUserId;
    private String createdByName;
    private String title;
    private String description;
    private ConsultationPriority priority;
    private ConsultationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    private String lastMessagePreview;
    private Integer unreadCount;

    public static ConsultationRequestResponse from(ConsultationRequest request, String lastMessage, Integer unread) {
        return ConsultationRequestResponse.builder()
                .id(request.getRequestId())
                .eventId(request.getEvent().getEventId())
                .eventName(request.getEvent().getEventName())
                .categoryId(request.getCategory().getCategoryId())
                .categoryName(request.getCategory().getCategoryName())
                .teamId(request.getTeam().getTeamId())
                .teamName(request.getTeam().getTeamName())
                .mentorId(request.getMentor().getUserId())
                .mentorName(request.getMentor().getFullName())
                .createdByUserId(request.getCreatedBy().getUserId())
                .createdByName(request.getCreatedBy().getFullName())
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .closedAt(request.getClosedAt())
                .lastMessagePreview(lastMessage)
                .unreadCount(unread)
                .build();
    }
}
