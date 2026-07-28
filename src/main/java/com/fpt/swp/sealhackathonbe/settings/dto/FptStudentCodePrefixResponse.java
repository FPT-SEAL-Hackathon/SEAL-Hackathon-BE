package com.fpt.swp.sealhackathonbe.settings.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FptStudentCodePrefixResponse {
    private String prefix;
    private String englishName;
    private String vietnameseName;
    private String majorGroup;
    private String majorCode;
    private String note;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
