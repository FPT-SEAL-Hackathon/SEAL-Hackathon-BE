package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamMembersRepository extends JpaRepository<TeamMembers, UUID> {
   // Tìm membership active hiện tại của user; dùng cho getMyTeam, createTeam, join request và removeMember.
   Optional<TeamMembers> findByUserIdAndActiveTrue(UUID userId);

   // Dư thừa hiện tại: chưa có service nào cần kiểm tra user trong một team cụ thể.
   Optional<TeamMembers> findByTeamIdAndUserIdAndActiveTrue(UUID teamId, UUID userId);

   // Lấy danh sách thành viên còn active để build TeamResponse.
   List<TeamMembers> findByTeamIdAndActiveTrue(UUID teamId);

   // Kiểm tra user đã thuộc team active nào chưa trước khi tạo team hoặc duyệt request.
   boolean existsByUserIdAndActiveTrue(UUID userId);

   // Đếm member active để validate MaxTeamSize/MinTeamSize của event.
   long countByTeamIdAndActiveTrue(UUID teamId);
}
