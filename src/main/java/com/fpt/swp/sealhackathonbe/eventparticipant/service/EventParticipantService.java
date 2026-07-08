package com.fpt.swp.sealhackathonbe.eventparticipant.service;

import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantBulkStatusUpdateRequest;
import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantResponse;
import com.fpt.swp.sealhackathonbe.eventparticipant.dto.EventParticipantStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface EventParticipantService {
    EventParticipantResponse register(UUID eventId, UUID currentUserId);

    EventParticipantResponse getOwnStatus(UUID eventId, UUID currentUserId);

    List<EventParticipantResponse> getOwnParticipations(UUID currentUserId);

    Page<EventParticipantResponse> search(
            UUID eventId,
            UUID categoryId,
            String status,
            String keyword,
            String university,
            UUID requesterUserId,
            Pageable pageable
    );

    EventParticipantResponse updateStatus(UUID participantId, EventParticipantStatusUpdateRequest request, UUID organizerUserId);

    List<EventParticipantResponse> updateStatuses(EventParticipantBulkStatusUpdateRequest request, UUID organizerUserId);

    void assertActiveParticipant(UUID eventId, UUID userId);

    /**
     * Guard cho luồng team-first: chỉ cần là student ACTIVE với hồ sơ đầy đủ,
     * KHÔNG yêu cầu đã là EventParticipant.
     */
    void assertEligibleStudent(UUID userId);

    /**
     * User đã có bản ghi participant cho event (dùng để khóa đội hình sau khi
     * team đăng ký — solo đã tắt nên participant chỉ sinh từ luồng team).
     */
    boolean hasRegistration(UUID eventId, UUID userId);

    /**
     * Xoa registration PENDING cua member khi roster team pending thay doi.
     * Neu member chua co registration thi khong lam gi; neu da duoc xu ly thi giu nguyen lich su.
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
