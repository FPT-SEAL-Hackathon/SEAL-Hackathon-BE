package com.fpt.swp.sealhackathonbe.ai.service.impl;

import com.fpt.swp.sealhackathonbe.ai.dto.request.CreateAiKnowledgeRequest;
import com.fpt.swp.sealhackathonbe.ai.dto.response.AiKnowledgeResponse;
import com.fpt.swp.sealhackathonbe.ai.entity.AiKnowledgeBase;
import com.fpt.swp.sealhackathonbe.ai.repository.AiKnowledgeBaseRepository;
import com.fpt.swp.sealhackathonbe.ai.service.AiKnowledgeService;
import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiKnowledgeServiceImpl implements AiKnowledgeService {

    private final AiKnowledgeBaseRepository knowledgeRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public AiKnowledgeResponse createKnowledge(CreateAiKnowledgeRequest request, String mentorEmail) {
        User mentor = userRepository.findByEmail(mentorEmail);
        if (mentor == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mentor not found");
        }

        Event event = eventRepository.findById(java.util.UUID.fromString(request.getEventId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        Category category = null;
        if (request.getCategoryId() != null && !request.getCategoryId().isEmpty()) {
            category = categoryRepository.findById(java.util.UUID.fromString(request.getCategoryId()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        }

        AiKnowledgeBase knowledge = AiKnowledgeBase.builder()
                .event(event)
                .category(category)
                .questionPattern(request.getQuestionPattern())
                .standardAnswer(request.getStandardAnswer())
                .mentor(mentor)
                .build();

        knowledge = knowledgeRepository.save(knowledge);
        return mapToResponse(knowledge);
    }

    @Override
    public List<AiKnowledgeResponse> getKnowledgeByEvent(String eventId) {
        List<AiKnowledgeBase> list = knowledgeRepository.findByEvent_EventId(java.util.UUID.fromString(eventId));
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteKnowledge(String id, String mentorEmail) {
        // Simple permission check could be added here
        if (!knowledgeRepository.existsById(java.util.UUID.fromString(id))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Knowledge not found");
        }
        knowledgeRepository.deleteById(java.util.UUID.fromString(id));
    }

    private AiKnowledgeResponse mapToResponse(AiKnowledgeBase kb) {
        return AiKnowledgeResponse.builder()
                .id(kb.getId().toString())
                .eventId(kb.getEvent().getEventId().toString())
                .categoryId(kb.getCategory() != null ? kb.getCategory().getCategoryId().toString() : null)
                .questionPattern(kb.getQuestionPattern())
                .standardAnswer(kb.getStandardAnswer())
                .mentorId(kb.getMentor().getUserId().toString())
                .mentorName(kb.getMentor().getFullName())
                .createdAt(kb.getCreatedAt())
                .build();
    }
}
