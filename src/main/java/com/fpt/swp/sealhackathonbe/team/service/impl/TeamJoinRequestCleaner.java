package com.fpt.swp.sealhackathonbe.team.service.impl;

import com.fpt.swp.sealhackathonbe.team.entity.TeamJoinRequests;
import com.fpt.swp.sealhackathonbe.team.repository.TeamJoinRequestsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Dọn join request PENDING để không còn request "treo" vô chủ:
 * - Team rời trạng thái FORMING (đăng ký event, bị reject, bị disqualify)
 *   thì mọi request PENDING của team phải được đóng lại — roster đã khóa,
 *   leader không thể xử lý chúng được nữa.
 * - User đã vào một team (được duyệt hoặc tự tạo team) thì các request PENDING
 *   của user ở các team khác cùng event phải tự hủy — leader team khác
 *   không phải nhìn thấy request chết (duyệt kiểu gì cũng 409).
 * Chạy trong transaction của caller.
 */
@Component
@RequiredArgsConstructor
public class TeamJoinRequestCleaner {

    public static final String REQUEST_STATUS_PENDING = "PENDING";
    public static final String REQUEST_STATUS_REJECTED = "REJECTED";
    public static final String REQUEST_STATUS_CANCELLED = "CANCELLED";

    private final TeamJoinRequestsRepository teamJoinRequestsRepository;

    /**
     * Đóng mọi request PENDING của một team (khi team rời FORMING).
     * actorUserId có thể là leader hoặc organizer thực hiện thao tác gây khóa roster.
     */
    public void rejectPendingRequestsForTeam(UUID teamId, UUID actorUserId, String note) {
        List<TeamJoinRequests> pending =
                teamJoinRequestsRepository.findByTeamIdAndRequestStatus(teamId, REQUEST_STATUS_PENDING);
        if (pending.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        pending.forEach(request -> {
            request.setRequestStatus(REQUEST_STATUS_REJECTED);
            request.setRespondedAt(now);
            request.setRespondedById(actorUserId);
            request.setResponseNote(note);
        });
        teamJoinRequestsRepository.saveAll(pending);
    }

    /**
     * Hủy các request PENDING khác của user trong cùng event
     * (gọi ngay khi user được duyệt vào một team hoặc tự tạo team).
     */
    public void cancelOtherPendingRequestsForUser(UUID userId, UUID eventId, UUID joinedTeamId) {
        List<TeamJoinRequests> pending = teamJoinRequestsRepository
                .findByUserIdAndRequestStatusAndTeam_EventId(userId, REQUEST_STATUS_PENDING, eventId);
        if (pending.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<TeamJoinRequests> others = pending.stream()
                .filter(request -> joinedTeamId == null || !joinedTeamId.equals(request.getTeamId()))
                .toList();
        others.forEach(request -> {
            request.setRequestStatus(REQUEST_STATUS_CANCELLED);
            request.setRespondedAt(now);
            request.setResponseNote("User joined another team in this event");
        });
        teamJoinRequestsRepository.saveAll(others);
    }
}
