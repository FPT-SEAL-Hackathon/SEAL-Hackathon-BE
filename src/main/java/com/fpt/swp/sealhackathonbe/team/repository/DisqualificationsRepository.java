package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DisqualificationsRepository extends JpaRepository<Disqualifications, UUID> {
    // Dư thừa hiện tại trong package team: service chỉ cần save bản ghi loại team.
    // Nếu sau này cần lịch sử loại theo team thì mở lại method này.
     List<Disqualifications> findByTeamId(UUID teamId);

     // Lay lich su loai cua mot submission de kiem tra ban ghi active truoc khi tao moi.
     List<Disqualifications> findBySubmissionId(UUID submissionId);

     // Lay cac bai nop dang bi loai va sap xep moi nhat truoc cho man hinh admin.
     @Query("""
             SELECT d FROM Disqualifications d
             WHERE d.submissionId IS NOT NULL AND d.reversed = false
             ORDER BY d.disqualifiedAt DESC
             """)
     List<Disqualifications> findActiveSubmissionDisqualifications();

}
