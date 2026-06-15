package com.fpt.swp.sealhackathonbe.notification.controller;

import com.fpt.swp.sealhackathonbe.notification.dto.BroadcastNotificationRequest;
import com.fpt.swp.sealhackathonbe.notification.dto.CreateNotificationRequest;
import com.fpt.swp.sealhackathonbe.notification.dto.NotificationResponse;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationRealtimeService;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "APIs for sending and managing notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationRealtimeService notificationRealtimeService;
    private final UserRepository userRepository;

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, int statusCode) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("statusCode", statusCode);
        response.put("data", null);

        return new ResponseEntity<>(response, HttpStatus.valueOf(statusCode));
    }

    private ResponseEntity<Map<String, Object>> buildSuccessResponse(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("statusCode", 200);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
    private UUID currentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Unauthenticated user");
        }

        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new RuntimeException("Authenticated user not found");
        }

        return user.getUserId();
    }

    @Operation(
            summary = "Get my notifications",
            description = "Get a paginated list of notifications for the authenticated user.",
            operationId = "getMyNotifications"
    )
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        try {
            UUID userId = currentUserId(authentication);
            Page<NotificationResponse> notification = notificationService.getNotification(userId, page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("data", notification.getContent());
            response.put("totalPages", notification.getTotalPages());
            response.put("totalElements", notification.getTotalElements());
            response.put("currentPage", notification.getNumber());
            response.put("message", "Notifications retrieved successfully");
            response.put("statusCode", 200);

            return ResponseEntity.ok((response));
        } catch (Exception e) {
            return  buildErrorResponse(e.getMessage(), 400);
        }
    }

    @Operation(
            summary = "Get unread notification count",
            description = "Count unread notifications for the authenticated user.",
            operationId = "getUnreadNotificationCount"
    )
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadNotificationCount(Authentication authentication) {
        try {
            UUID userId = currentUserId(authentication);
            long unreadCount = notificationService.countUnread(userId);

            return buildSuccessResponse(unreadCount, "Unread notifications counted successfully");
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage(), 400);
        }
    }

    @Operation(
            summary = "Stream real-time notifications",
            description = "Open a server-sent events stream for notifications of the authenticated user.",
            operationId = "streamNotifications"
    )
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(Authentication authentication) {
        UUID userId = currentUserId(authentication);
        return notificationRealtimeService.subscribe(userId);
    }

    @Operation(
            summary = "Send notification to a user",
            description = "Create and send a notification to a specific user.",
            operationId = "sendNotificationToUser"
    )
    @PostMapping
    public ResponseEntity<Map<String, Object>> sendNotificationToUser(
            @Valid @RequestBody CreateNotificationRequest request,
            Authentication authentication
    ) {
        try {
            UUID senderId = currentUserId(authentication);
            NotificationResponse notification = notificationService.sendNotification(
                    request.getRecipientUserId(),
                    senderId,
                    request.getEventId(),
                    request.getTitle(),
                    request.getBody()
            );

            return buildSuccessResponse(notification, "Notification sent successfully");
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage(), 400);
        }
    }

    @Operation(
            summary = "Send broadcast notification",
            description = "Create and send the same notification to multiple users.",
            operationId = "sendBroadcastNotification"
    )
    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, Object>> sendBroadcastNotification(
            @Valid @RequestBody BroadcastNotificationRequest request,
            Authentication authentication
    ) {
        try {
            UUID senderId = currentUserId(authentication);
            List<NotificationResponse> notifications = notificationService.sendBroadcastNotification(
                    request.getRecipientUserIds(),
                    senderId,
                    request.getEventId(),
                    request.getTitle(),
                    request.getBody()
            );

            return buildSuccessResponse(notifications, "Broadcast notification sent successfully");
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage(), 400);
        }
    }

    @Operation(
            summary = "Mark notification as read",
            description = "Mark a specific notification as read for the authenticated user.",
            operationId = "markNotificationAsRead"
    )
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> markNotificationAsRead(
            @PathVariable UUID notificationId,
            Authentication authentication
    ) {
        try {
            UUID userId = currentUserId(authentication);
            NotificationResponse notification = notificationService.markAsRead(notificationId, userId);

            return buildSuccessResponse(notification, "Notification marked as read");
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage(), 400);
        }
    }

    @Operation(
            summary = "Mark all notifications as read",
            description = "Mark every notification as read for the authenticated user.",
            operationId = "markAllNotificationsAsRead"
    )
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllNotificationsAsRead(Authentication authentication) {
        try {
            UUID userId = currentUserId(authentication);
            long updatedCount = notificationService.markAllAsRead(userId);

            return buildSuccessResponse(updatedCount, "Notifications marked as read");
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage(), 400);
        }
    }

    @Operation(
            summary = "Delete notification",
            description = "Delete a specific notification belonging to the authenticated user.",
            operationId = "deleteNotification"
    )
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @PathVariable UUID notificationId,
            Authentication authentication
    ) {
        try {
            UUID userId = currentUserId(authentication);
            notificationService.deleteNotification(notificationId, userId);

            return buildSuccessResponse(null, "Notification deleted successfully");
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage(), 400);
        }
    }
}
