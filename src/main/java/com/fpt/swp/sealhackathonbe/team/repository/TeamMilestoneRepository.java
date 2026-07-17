package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.TeamMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamMilestoneRepository extends JpaRepository<TeamMilestone, UUID> {

    /** Lấy tất cả milestones của một team, sắp xếp theo thứ tự. */
    List<TeamMilestone> findByTeamIdOrderBySortOrderAscCreatedAtAsc(UUID teamId);

    /** Lấy milestones của một team do một mentor cụ thể tạo. */
    List<TeamMilestone> findByTeamIdAndMentorUserIdOrderBySortOrderAscCreatedAtAsc(UUID teamId, UUID mentorUserId);

    // Hard delete user: user còn là mentor của milestone (MentorUserID NOT NULL) → chặn xóa.
    boolean existsByMentorUserId(UUID mentorUserId);
}
