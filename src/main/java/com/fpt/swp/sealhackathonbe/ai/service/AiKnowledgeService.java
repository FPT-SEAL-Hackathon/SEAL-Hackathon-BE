package com.fpt.swp.sealhackathonbe.ai.service;

import com.fpt.swp.sealhackathonbe.ai.dto.request.CreateAiKnowledgeRequest;
import com.fpt.swp.sealhackathonbe.ai.dto.response.AiKnowledgeResponse;

import java.util.List;

public interface AiKnowledgeService {
    AiKnowledgeResponse createKnowledge(CreateAiKnowledgeRequest request, String mentorEmail);
    List<AiKnowledgeResponse> getKnowledgeByEvent(String eventId);
    void deleteKnowledge(String id, String mentorEmail);
}
