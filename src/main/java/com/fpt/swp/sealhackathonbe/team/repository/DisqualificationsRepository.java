package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DisqualificationsRepository extends JpaRepository<Disqualifications, UUID> {
    // Dư thừa hiện tại trong package team: service chỉ cần save bản ghi loại team.
    // Nếu sau này cần lịch sử loại theo team thì mở lại method này.
     List<Disqualifications> findByTeamId(UUID teamId);

     List<Disqualifications> findBySubmissionId(UUID submissionId);

     // Team khong chua RoundID; loc round qua submission cua team va loc category truc tiep tren team.
     @Query("""
             SELECT d FROM Disqualifications d
             WHERE d.team.categoryId = :categoryId
               AND d.reversed = false
               AND EXISTS (
                   SELECT s FROM Submissions s
                   WHERE s.teamId = d.teamId AND s.roundId = :roundId
               )
             ORDER BY d.disqualifiedAt DESC
             """)
     List<Disqualifications> findActiveTeamDisqualifications(
             @Param("roundId") UUID roundId,
             @Param("categoryId") UUID categoryId
     );

}
