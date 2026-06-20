package com.fpt.swp.sealhackathonbe.judging.repository;

import com.fpt.swp.sealhackathonbe.judging.entity.EvaluationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvaluationAuditLogRepository extends JpaRepository<EvaluationAuditLog, UUID> {
    List<EvaluationAuditLog> findByEvent_EventIdOrderByCreatedAtDesc(UUID eventId);
}
