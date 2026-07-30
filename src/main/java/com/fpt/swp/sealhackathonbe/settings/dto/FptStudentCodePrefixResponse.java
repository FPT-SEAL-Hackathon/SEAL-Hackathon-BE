package com.fpt.swp.sealhackathonbe.settings.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class FptStudentCodePrefixResponse {
    private String prefix;
    private String englishName;
    private String vietnameseName;
    private String majorGroup;
    private String majorCode;
    private String note;
    private Boolean active;

    /**
     * Số tài khoản (chưa xoá) đang mang MSSV bắt đầu bằng prefix này. Chỉ được điền ở danh
     * sách Admin, để admin thấy được ảnh hưởng TRƯỚC khi tắt một prefix — tắt prefix không
     * đụng gì tới các tài khoản đã có, nhưng vẫn nên biết đang ảnh hưởng bao nhiêu người.
     */
    private Long usageCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
