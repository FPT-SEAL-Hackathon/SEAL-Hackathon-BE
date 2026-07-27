package com.fpt.swp.sealhackathonbe.category.dto.response;

import lombok.*;
import java.util.List;

/**
 * Aggregate response cho Mentor Dashboard.
 * Thay thế 50+ API request rải rác bằng 1 request duy nhất.
 * Bao gồm: danh sách category được assign cho mentor hiện tại + teams tương ứng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorDashboardSummaryResponse {

    /** Danh sách category mà mentor này được assign */
    private List<AssignedCategoryDto> assignedCategories;

    /** Toàn bộ teams thuộc các event có category được assign */
    private List<TeamResponse> teams;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssignedCategoryDto {
        private String categoryId;
        private String categoryName;
        private String description;
        private String eventId;
        private String eventName;
        private int teamCount;
        private boolean isActive;
    }


}
