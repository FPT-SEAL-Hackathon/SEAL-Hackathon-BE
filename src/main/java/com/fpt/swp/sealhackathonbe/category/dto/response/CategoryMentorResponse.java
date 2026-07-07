package com.fpt.swp.sealhackathonbe.category.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CategoryMentorResponse {
    private UUID categoryExpertId;

    private UUID categoryId;

    private UUID expertId;
    private String fullName;
    private String email;
    private String phone;
    private String expertName;
    private String expertEmail;

    private LocalDateTime assignedAt;
}
