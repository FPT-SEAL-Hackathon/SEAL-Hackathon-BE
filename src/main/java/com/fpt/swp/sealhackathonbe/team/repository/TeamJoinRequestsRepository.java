package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.TeamJoinRequests;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamJoinRequestsRepository extends JpaRepository<TeamJoinRequests, UUID> {

    // Kiểm tra user đã gửi request PENDING vào team này chưa để tránh tạo trùng.
    boolean existsByTeamIdAndUserIdAndRequestStatus(UUID teamId, UUID userId, String requestStatus);

    // Lấy đúng request còn PENDING để leader xử lý, tránh duyệt/từ chối lại request đã xử lý.
    @EntityGraph(attributePaths = {"team", "user"})
    Optional<TeamJoinRequests> findByRequestIdAndRequestStatus(UUID requestId, String requestStatus);

    // Lấy danh sách request PENDING của một team cho màn hình leader.
    @EntityGraph(attributePaths = "user")
    List<TeamJoinRequests> findByTeamIdAndRequestStatus(UUID teamId, String requestStatus);

    // Các request PENDING của một user trong cùng event (để tự hủy khi user đã vào team khác).
    List<TeamJoinRequests> findByUserIdAndRequestStatusAndTeam_EventId(UUID userId, String requestStatus, UUID eventId);

    // Các request PENDING của một user trên toàn hệ thống (cho màn hình "request của tôi").
    @EntityGraph(attributePaths = "team")
    List<TeamJoinRequests> findByUserIdAndRequestStatus(UUID userId, String requestStatus);

    long deleteByTeamId(UUID teamId);

    // Hard delete user: xóa toàn bộ join request của user.
    long deleteByUserId(UUID userId);

    // Hard delete user: gỡ tham chiếu "người phản hồi" (RespondedByID nullable)
    // trên request của các user khác trước khi xóa user.
    @Modifying
    @Query("UPDATE TeamJoinRequests r SET r.respondedById = NULL WHERE r.respondedById = :userId")
    int clearRespondedBy(@Param("userId") UUID userId);
}
