package com.fpt.swp.sealhackathonbe.notification.service.Impl;

import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.notification.Repository.NotificationRepository;
import com.fpt.swp.sealhackathonbe.notification.dto.NotificationPushEvent;
import com.fpt.swp.sealhackathonbe.notification.dto.NotificationResponse;
import com.fpt.swp.sealhackathonbe.notification.entity.Notification;
import com.fpt.swp.sealhackathonbe.notification.service.EmailService;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationRealtimeService;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final NotificationRealtimeService notificationRealtimeService;
    private final EmailService emailService;


    @Override
    @Deprecated
    @Transactional
    public void sendNotification(String recipient, String subject, String message) {
        throw new UnsupportedOperationException("sentByUserId is required to create an auditable notification");
    }

    @Override
    @Deprecated
    @Transactional
    public void sendBoardcastNotification(List<UUID> recipientIds, String subject, String message) {
        throw new UnsupportedOperationException("sentByUserId is required to create auditable notifications");
    }

    @Override
    @Transactional
    public NotificationResponse sendNotification(
            UUID recipientUserId,
            UUID sentByUserId,
            UUID eventId,
            String title,
            String body
    ) {
        User recipient = getUser(recipientUserId, "Recipient user not found");
        User sender = getUser(sentByUserId, "Sender user not found");
        Event event = getEvent(eventId);

        Notification notification = createNotification(recipient, sender, event, title, body);
        return NotificationResponse.from(notification);
    }

    @Override
    @Transactional
    public List<NotificationResponse> sendBroadcastNotification(
            List<UUID> recipientIds,
            UUID sentByUserId,
            UUID eventId,
            String title,
            String body
    ) {
        if (recipientIds == null || recipientIds.isEmpty()) {
            return List.of();
        }

        User sender = getUser(sentByUserId, "Sender user not found");
        Event event = getEvent(eventId);

        List<Notification> notifications = recipientIds.stream()
                .distinct()
                .map(recipientId -> createNotificationEntity(
                        getUser(recipientId, "Recipient user not found"),
                        sender,
                        event,
                        title,
                        body
                ))
                .collect(Collectors.toList());

        return notificationRepository.saveAll(notifications).stream()
                .map(this::dispatchAndConvert)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotification(UUID userId, int page, int size) {
        int pageIndex = Math.max(page, 0);
        int pageSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);

        User user = getUser(userId, "User not found");

        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        Page<Notification> notifications = notificationRepository.findByRecipientUserIDOrderBySentAtDesc(user, pageable);

        return notifications.map(NotificationResponse::from);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID userId) {
        Notification notification = getUserNotification(notificationId, userId);
        notification.setIsRead(true);

        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public long markAllAsRead(UUID userId) {
        User user = getUser(userId, "User not found");
        List<Notification> unreadNotifications = notificationRepository.findByRecipientUserIDAndIsReadFalse(user);

        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);

        return unreadNotifications.size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        User user = getUser(userId, "User not found");
        return notificationRepository.countByRecipientUserIDAndIsReadFalse(user);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        Notification notification = getUserNotification(notificationId, userId);
        notificationRepository.delete(notification);
    }

    private Notification createNotification(
            User recipient,
            User sender,
            Event event,
            String title,
            String body
    ) {
        Notification notification = notificationRepository.save(createNotificationEntity(recipient, sender, event, title, body));
        dispatchNotification(notification);
        return notification;
    }

    private Notification createNotificationEntity(
            User recipient,
            User sender,
            Event event,
            String title,
            String body
    ) {
        validateContent(title, body);

        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setRecipientUserID(recipient);
        notification.setSentByUserID(sender);
        notification.setEventID(event);
        notification.setTitle(title.trim());
        notification.setBody(body.trim());
        notification.setSentAt(Instant.now());
        notification.setIsRead(false);

        return notification;
    }

    private void validateContent(String title, String body) {
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("Notification title is required");
        }
        if (title.trim().length() > 300) {
            throw new RuntimeException("Notification title must not exceed 300 characters");
        }
        if (body == null || body.trim().isEmpty()) {
            throw new RuntimeException("Notification body is required");
        }
    }

    private User getUser(UUID userId, String errorMessage) {
        if (userId == null) {
            throw new RuntimeException(errorMessage);
        }
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException(errorMessage));
    }

    private Event getEvent(UUID eventId) {
        if (eventId == null) {
            return null;
        }
        return eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
    }

    private Notification getUserNotification(UUID notificationId, UUID userId) {
        if (notificationId == null) {
            throw new RuntimeException("Notification id is required");
        }

        User user = getUser(userId, "User not found");
        return notificationRepository.findByIdAndRecipientUserID(notificationId, user)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    private NotificationResponse dispatchAndConvert(Notification notification) {
        dispatchNotification(notification);
        return NotificationResponse.from(notification);
    }

    private void dispatchNotification(Notification notification) {
        User recipient = notification.getRecipientUserID();
        if (recipient == null) {
            return;
        }

        long unreadCount = notificationRepository.countByRecipientUserIDAndIsReadFalse(recipient);
        NotificationPushEvent event = new NotificationPushEvent(NotificationResponse.from(notification), unreadCount);
        notificationRealtimeService.publish(recipient.getUserId(), event);
        emailService.sendEmail(recipient.getEmail(), notification.getTitle(), notification.getBody());
    }
}
