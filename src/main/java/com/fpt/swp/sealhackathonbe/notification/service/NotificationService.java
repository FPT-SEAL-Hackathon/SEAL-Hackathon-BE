package com.fpt.swp.sealhackathonbe.notification.service;

import com.fpt.swp.sealhackathonbe.notification.dto.NotificationResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    @Deprecated
    void sendNotification(String recipient, String subject, String message);

    @Deprecated
    void sendBoardcastNotification(List<UUID> recipientIds, String subject, String message);

    NotificationResponse sendNotification(
            UUID recipientUserId,
            UUID sentByUserId,
            UUID eventId,
            String title,
            String body
    );

    NotificationResponse sendNotificationByEmail(
            String recipientEmail,
            UUID sentByUserId,
            UUID eventId,
            String title,
            String body
    );

    List<NotificationResponse> sendBroadcastNotification(
            List<UUID> recipientIds,
            UUID sentByUserId,
            UUID eventId,
            String title,
            String body
    );

    Page<NotificationResponse> getNotification(UUID userId, int page, int size);

    NotificationResponse markAsRead(UUID notificationId, UUID userId);

    long markAllAsRead(UUID userId);

    long countUnread(UUID userId);

    void deleteNotification(UUID notificationId, UUID userId);

}
