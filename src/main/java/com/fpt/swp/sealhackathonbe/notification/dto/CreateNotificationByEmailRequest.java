package com.fpt.swp.sealhackathonbe.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateNotificationByEmailRequest {
    @NotBlank
    @Email
    private String recipientEmail;

    private UUID eventId;

    @NotBlank
    @Size(max = 300)
    private String title;

    @NotBlank
    private String body;
}
