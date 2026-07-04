package com.fpt.swp.sealhackathonbe.consultation.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageRequest {
    @jakarta.validation.constraints.NotBlank(message = "Content must not be empty")
    @Size(max = 3000)
    private String content;

    @Size(max = 500)
    private String attachmentUrl;
}
