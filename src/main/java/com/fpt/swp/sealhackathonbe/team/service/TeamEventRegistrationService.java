package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantResponse;

import java.util.List;
import java.util.UUID;

/**
 * Đăng ký/duyệt sự kiện theo TEAM (thuộc module team).
 * Chỉ dùng repository của module eventparticipant, không gọi qua
 * EventParticipantService/Controller — giữ ranh giới module rõ ràng.
 */
public interface TeamEventRegistrationService {

    /**
     * Guard cho luồng team-first: chỉ cần là student ACTIVE với hồ sơ đầy đủ,
     * KHÔNG yêu cầu đã là EventParticipant.
     */
    void assertEligibleStudent(UUID userId);

    /**
     * User đã có bản ghi participant cho event (dùng để khóa đội hình sau khi
     * team đăng ký).
     */
    boolean hasRegistration(UUID eventId, UUID userId);

    /**
     * Xóa registration PENDING của member khi roster team pending thay đổi.
     * Nếu member chưa có registration thì không làm gì; nếu đã được xử lý thì giữ nguyên lịch sử.
     */
    void removePendingRegistration(UUID eventId, UUID userId);

    /**
     * Leader đăng ký cả team vào event: tạo participant PENDING cho mọi thành viên.
     */
    List<EventParticipantResponse> registerTeam(UUID teamId, UUID currentUserId);

    /**
     * Leader hủy đăng ký khi toàn bộ participant của team còn PENDING.
     */
    void withdrawTeamRegistration(UUID teamId, UUID currentUserId);

    /**
     * Duyệt theo team: approve/reject toàn bộ participant PENDING của thành viên.
     * Team chưa đăng ký (không có participant) thì không làm gì.
     */
    void applyTeamDecision(UUID teamId, boolean approved, String note, UUID organizerUserId);
}
