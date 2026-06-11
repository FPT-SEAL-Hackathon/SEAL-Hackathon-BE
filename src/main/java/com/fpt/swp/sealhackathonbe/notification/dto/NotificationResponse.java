package com.fpt.swp.sealhackathonbe.notification.dto;

import com.fpt.swp.sealhackathonbe.notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private UUID id;
    private UUID eventId;
    private UUID recipientUserId;
    private String title;
    private String body;
    private Instant sentAt;
    private UUID sentByUserId;
    private Boolean isRead;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .eventId(notification.getEventID() != null ? notification.getEventID().getEventId() : null)
                .recipientUserId(notification.getRecipientUserID() != null ? notification.getRecipientUserID().getUserId() : null)
                .title(notification.getTitle())
                .body(notification.getBody())
                .sentAt(notification.getSentAt())
                .sentByUserId(notification.getSentByUserID() != null ? notification.getSentByUserID().getUserId() : null)
                .isRead(notification.getIsRead())
                .build();
    }
}
