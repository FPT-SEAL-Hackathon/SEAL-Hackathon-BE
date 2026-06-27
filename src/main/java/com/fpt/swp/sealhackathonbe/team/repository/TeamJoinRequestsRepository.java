package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.TeamJoinRequests;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
