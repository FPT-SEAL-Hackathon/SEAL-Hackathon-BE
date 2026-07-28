package com.fpt.swp.sealhackathonbe.category.service;

import com.fpt.swp.sealhackathonbe.category.dto.response.MentorDashboardSummaryResponse;

import java.util.UUID;

/**
 * Service cung cấp dữ liệu tổng hợp (aggregate) cho Mentor Dashboard.
 * Thay thế vòng lặp 50+ HTTP request từ FE bằng 1 API call duy nhất.
 */
public interface MentorDashboardService {

    /**
     * Trả về danh sách category được assign cho mentor + teams tương ứng.
     *
     * @param mentorId UUID của mentor hiện tại (lấy từ JWT principal)
     */
    MentorDashboardSummaryResponse getDashboardSummary(UUID mentorId);
}
