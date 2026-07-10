package com.fpt.swp.sealhackathonbe.consultation.repository;

import com.fpt.swp.sealhackathonbe.consultation.entity.ConsultationRequest;
import com.fpt.swp.sealhackathonbe.consultation.entity.ConsultationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest, UUID> {
    Page<ConsultationRequest> findByMentor_UserId(UUID mentorId, Pageable pageable);
    Page<ConsultationRequest> findByTeam_TeamId(UUID teamId, Pageable pageable);
    
    // You can add more complex specifications or queries here for filtering by status, priority, category, team
    Page<ConsultationRequest> findByMentor_UserIdAndStatus(UUID mentorId, ConsultationStatus status, Pageable pageable);
    Page<ConsultationRequest> findByMentor_UserIdAndCategory_CategoryId(UUID mentorId, UUID categoryId, Pageable pageable);
    
    List<ConsultationRequest> findByCategory_CategoryIdAndStatusIn(UUID categoryId, List<ConsultationStatus> statuses);

    // Đếm số request mở (PENDING/ACCEPTED/IN_PROGRESS) của mentor trong 1 category
    long countByMentor_UserIdAndCategory_CategoryIdAndStatusIn(UUID mentorId, UUID categoryId, List<ConsultationStatus> statuses);

    // Đếm tổng request của 1 team
    long countByTeam_TeamId(UUID teamId);
}
