package com.fpt.swp.sealhackathonbe.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationPushEvent {
    private NotificationResponse notification;
    private long unreadCount;
}
