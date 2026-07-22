package com.fpt.swp.sealhackathonbe.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAiKnowledgeRequest {
    @NotNull
    private String eventId;
    private String categoryId;

    @NotBlank
    private String questionPattern;

    @NotBlank
    private String standardAnswer;
}
