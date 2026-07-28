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
    Page<ConsultationRequest> findByCategory_CategoryIdIn(List<UUID> categoryIds, Pageable pageable);
    Page<ConsultationRequest> findByTeam_TeamId(UUID teamId, Pageable pageable);
    
    // You can add more complex specifications or queries here for filtering by status, priority, category, team
    Page<ConsultationRequest> findByCategory_CategoryIdInAndStatus(List<UUID> categoryIds, ConsultationStatus status, Pageable pageable);
    Page<ConsultationRequest> findByCategory_CategoryId(UUID categoryId, Pageable pageable);
    
    List<ConsultationRequest> findByCategory_CategoryIdAndStatusIn(UUID categoryId, List<ConsultationStatus> statuses);

    // Đếm số request mở (PENDING/ACCEPTED/IN_PROGRESS) trong 1 category
    long countByCategory_CategoryIdAndStatusIn(UUID categoryId, List<ConsultationStatus> statuses);

    // Đếm tổng request của 1 team
    long countByTeam_TeamId(UUID teamId);

    // Hard delete user: xóa các consultation do user tạo
    // (ConsultationMessages xóa theo nhờ FK ON DELETE CASCADE trong DB).
    long deleteByCreatedBy_UserId(UUID userId);
}
