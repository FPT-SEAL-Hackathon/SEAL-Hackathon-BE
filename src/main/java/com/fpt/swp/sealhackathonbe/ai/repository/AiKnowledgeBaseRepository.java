package com.fpt.swp.sealhackathonbe.ai.repository;

import com.fpt.swp.sealhackathonbe.ai.entity.AiKnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiKnowledgeBaseRepository extends JpaRepository<AiKnowledgeBase, UUID> {
    List<AiKnowledgeBase> findByEvent_EventId(UUID eventId);
    List<AiKnowledgeBase> findByCategory_CategoryId(UUID categoryId);
}
