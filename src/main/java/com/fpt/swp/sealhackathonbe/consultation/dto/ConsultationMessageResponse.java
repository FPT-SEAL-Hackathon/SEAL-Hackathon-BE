package com.fpt.swp.sealhackathonbe.consultation.dto;

import com.fpt.swp.sealhackathonbe.consultation.entity.ConsultationMessage;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ConsultationMessageResponse {
    private UUID id;
    private UUID requestId;
    private UUID senderId;
    private String senderName;
    private String senderRole;
    private String content;
    private String attachmentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime seenAt;

    public static ConsultationMessageResponse from(ConsultationMessage message) {
        return ConsultationMessageResponse.builder()
                .id(message.getMessageId())
                .requestId(message.getRequest().getRequestId())
                .senderId(message.getSender().getUserId())
                .senderName(message.getSender().getFullName())
                .senderRole(message.getSender().getUserType() != null ? message.getSender().getUserType().getTypeName() : null)
                .content(message.getContent())
                .attachmentUrl(message.getAttachmentUrl())
                .createdAt(message.getCreatedAt())
                .seenAt(message.getSeenAt())
                .build();
    }
}
