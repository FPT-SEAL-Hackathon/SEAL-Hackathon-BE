package com.fpt.swp.sealhackathonbe.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BroadcastNotificationRequest {
    @NotEmpty
    private List<UUID> recipientUserIds;

    private UUID eventId;

    @NotBlank
    @Size(max = 300)
    private String title;

    @NotBlank
    private String body;
}
