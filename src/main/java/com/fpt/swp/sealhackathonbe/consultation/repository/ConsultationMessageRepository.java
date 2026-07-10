package com.fpt.swp.sealhackathonbe.consultation.repository;

import com.fpt.swp.sealhackathonbe.consultation.entity.ConsultationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultationMessageRepository extends JpaRepository<ConsultationMessage, UUID> {
    List<ConsultationMessage> findByRequest_RequestIdOrderByCreatedAtAsc(UUID requestId);
}
