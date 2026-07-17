package com.fpt.swp.sealhackathonbe.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Facet counts cho màn User Management (kiểu drill-down):
 * count của mỗi option trong một nhóm được tính với mọi filter đang áp
 * TRỪ chính nhóm đó — con số phản ánh "chọn thêm option này thì ra bao nhiêu".
 * total = tổng kết quả với TOÀN BỘ filter (hiển thị preview trên nút CTA).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFacetsResponse {

    private long total;
    private List<FacetOption> roles;
    private List<FacetOption> statuses;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FacetOption {
        /** Mã canonical dùng trong URL/query (vd FPT_STUDENT, ACTIVE). */
        private String code;
        /** Nhãn hiển thị từ database (vd "FPT Student", "Active"). */
        private String name;
        private long count;
    }
}
