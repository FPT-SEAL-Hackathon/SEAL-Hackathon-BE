package com.fpt.swp.sealhackathonbe.category.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CategoryMentorResponse {
    private UUID categoryMentorId;
    private UUID categoryId;
    private UUID mentorId;
    private String mentorName;
    private String mentorEmail;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedAt;
}
