package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamMembersRepository extends JpaRepository<TeamMembers, UUID> {

   // Tim membership active cua user, dung cho chuc nang xem team hien tai va roi team.
   Optional<TeamMembers> findByUserIdAndActiveTrue(UUID userId);

   // Xac nhan user dang la member active cua dung team truoc khi tra thong tin chi tiet.
   Optional<TeamMembers> findByTeamIdAndUserIdAndActiveTrue(UUID teamId, UUID userId);

   // Lay cac member con hoat dong de tao TeamResponse.
   List<TeamMembers> findByTeamIdAndActiveTrue(UUID teamId);

   // Kiem tra nhanh user co membership active hay khong.
   boolean existsByUserIdAndActiveTrue(UUID userId);

   // Dam bao mot user khong tham gia hai team active trong cung event.
   boolean existsByUserIdAndTeam_EventIdAndActiveTrue(UUID userId, UUID eventId);

   @Query("SELECT CASE WHEN COUNT(tm) > 0 THEN true ELSE false END FROM TeamMembers tm WHERE tm.userId = :userId AND tm.active = true AND tm.team.eventId = :eventId AND tm.team.categoryId = :categoryId")
   boolean existsActiveMemberInEventCategory(
           @Param("userId") UUID userId,
           @Param("eventId") UUID eventId,
           @Param("categoryId") UUID categoryId
   );

   // Dem member active de kiem tra MinTeamSize va MaxTeamSize.
   long countByTeamIdAndActiveTrue(UUID teamId);
}
