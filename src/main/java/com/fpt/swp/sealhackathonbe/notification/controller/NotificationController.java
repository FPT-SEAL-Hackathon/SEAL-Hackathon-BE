package com.fpt.swp.sealhackathonbe.notification.controller;

import com.fpt.swp.sealhackathonbe.notification.dto.BroadcastNotificationRequest;
import com.fpt.swp.sealhackathonbe.notification.dto.CreateNotificationByEmailRequest;
import com.fpt.swp.sealhackathonbe.notification.dto.CreateNotificationRequest;
import com.fpt.swp.sealhackathonbe.notification.dto.NotificationResponse;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationRealtimeService;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "APIs for sending and managing notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationRealtimeService notificationRealtimeService;
    private final UserRepository userRepository;

    private ResponseEntity<Map<String, Object>> buildSuccessResponse(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("statusCode", 200);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    // Permission:
    // Luôn lấy userId từ Authentication để user chỉ thao tác trên dữ liệu của mình.
    // Lỗi được để nổi lên GlobalExceptionHandler (log + đúng HTTP status +
    // đồng nhất ErrorResponse) thay vì tự nuốt thành 400 không log.
    private UUID currentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BadCredentialsException("Unauthenticated user");
        }

        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new EntityNotFoundException("Authenticated user not found");
        }

        return user.getUserId();
    }

    @Operation(
            summary = "Get my notifications",
            description = "Get a paginated list of notifications for the authenticated user.",
            operationId = "getMyNotifications"
    )
    @GetMapping("/getMyNotifications")
    public ResponseEntity<Map<String, Object>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        UUID userId = currentUserId(authentication);
        Page<NotificationResponse> notification = notificationService.getNotification(userId, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("data", notification.getContent());
        response.put("totalPages", notification.getTotalPages());
        response.put("totalElements", notification.getTotalElements());
        response.put("currentPage", notification.getNumber());
        response.put("unreadCount", notificationService.countUnread(userId));
        response.put("message", "Notifications retrieved successfully");
        response.put("statusCode", 200);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get unread notification count",
            description = "Count unread notifications for the authenticated user.",
            operationId = "getUnreadNotificationCount"
    )
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadNotificationCount(Authentication authentication) {
        UUID userId = currentUserId(authentication);
        long unreadCount = notificationService.countUnread(userId);

        return buildSuccessResponse(unreadCount, "Unread notifications counted successfully");
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
    @PostMapping("/sendNotificationToUser")
    // RBAC:
    // Chỉ ORGANIZER được gửi thông báo trực tiếp để tránh spam giữa user.
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<Map<String, Object>> sendNotificationToUser(
            @Valid @RequestBody CreateNotificationRequest request,
            Authentication authentication
    ) {
        UUID senderId = currentUserId(authentication);
        NotificationResponse notification = notificationService.sendNotification(
                request.getRecipientUserId(),
                senderId,
                request.getEventId(),
                request.getTitle(),
                request.getBody()
        );

        return buildSuccessResponse(notification, "Notification sent successfully");
    }

    @Operation(
            summary = "Send notification to a user by email",
            description = "Create and send an in-app notification and email to a specific user by email.",
            operationId = "sendNotificationToEmail"
    )
    @PostMapping("/sendNotificationToEmail")
    // RBAC:
    // Chỉ ORGANIZER được gửi thông báo qua email để bảo vệ người nhận.
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<Map<String, Object>> sendNotificationToEmail(
            @Valid @RequestBody CreateNotificationByEmailRequest request,
            Authentication authentication
    ) {
        UUID senderId = currentUserId(authentication);
        NotificationResponse notification = notificationService.sendNotificationByEmail(
                request.getRecipientEmail(),
                senderId,
                request.getEventId(),
                request.getTitle(),
                request.getBody()
        );

        return buildSuccessResponse(notification, "Notification sent successfully");
    }

    @Operation(
            summary = "Send broadcast notification",
            description = "Create and send the same notification to multiple users.",
            operationId = "sendBroadcastNotification"
    )
    @PostMapping("/sendBroadcastNotification")
    // RBAC:
    // Chỉ ORGANIZER được broadcast vì ảnh hưởng nhiều người dùng.
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<Map<String, Object>> sendBroadcastNotification(
            @Valid @RequestBody BroadcastNotificationRequest request,
            Authentication authentication
    ) {
        UUID senderId = currentUserId(authentication);
        List<NotificationResponse> notifications = notificationService.sendBroadcastNotification(
                request.getRecipientUserIds(),
                senderId,
                request.getEventId(),
                request.getTitle(),
                request.getBody()
        );

        return buildSuccessResponse(notifications, "Broadcast notification sent successfully");
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
        UUID userId = currentUserId(authentication);
        NotificationResponse notification = notificationService.markAsRead(notificationId, userId);

        return buildSuccessResponse(notification, "Notification marked as read");
    }

    @Operation(
            summary = "Mark all notifications as read",
            description = "Mark every notification as read for the authenticated user.",
            operationId = "markAllNotificationsAsRead"
    )
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllNotificationsAsRead(Authentication authentication) {
        UUID userId = currentUserId(authentication);
        long updatedCount = notificationService.markAllAsRead(userId);

        return buildSuccessResponse(updatedCount, "Notifications marked as read");
    }

    @Operation(
            summary = "Delete notification",
            description = "Delete a specific notification belonging to the authenticated user.",
            operationId = "deleteNotification"
    )
    @DeleteMapping("/deleteNotification/{notificationId}")
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @PathVariable UUID notificationId,
            Authentication authentication
    ) {
        UUID userId = currentUserId(authentication);
        notificationService.deleteNotification(notificationId, userId);

        return buildSuccessResponse(null, "Notification deleted successfully");
    }
}
