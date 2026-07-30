package com.fpt.swp.sealhackathonbe.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PublicCountResponse {
    // DTO dem so luong public, dung lai cho cac endpoint thong ke don gian.
    private Long count;
}
