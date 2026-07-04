package com.fpt.swp.sealhackathonbe.category.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class MentorResponse {
    private UUID mentorId;
    private String fullName;
    private String email;
    private String phone;
}