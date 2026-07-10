package com.fpt.swp.sealhackathonbe.consultation.dto;

import com.fpt.swp.sealhackathonbe.consultation.entity.ConsultationPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateConsultationRequestRequest {
    @NotBlank
    @Size(max = 150)
    private String title;

    private java.util.UUID mentorId;

    @NotBlank
    @Size(max = 3000)
    private String description;

    @NotNull
    private ConsultationPriority priority;

    private String attachmentUrl;
}
