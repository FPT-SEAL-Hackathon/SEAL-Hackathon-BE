package com.fpt.swp.sealhackathonbe.ai.controller;

import com.fpt.swp.sealhackathonbe.ai.dto.request.CreateAiKnowledgeRequest;
import com.fpt.swp.sealhackathonbe.ai.dto.response.AiKnowledgeResponse;
import com.fpt.swp.sealhackathonbe.ai.service.AiKnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-knowledge")
@RequiredArgsConstructor
public class AiKnowledgeBaseController {

    private final AiKnowledgeService aiKnowledgeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MENTOR', 'EXPERT', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<AiKnowledgeResponse> createKnowledge(
            @Valid @RequestBody CreateAiKnowledgeRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(aiKnowledgeService.createKnowledge(request, email));
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('MENTOR', 'EXPERT', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<List<AiKnowledgeResponse>> getKnowledgeByEvent(@PathVariable String eventId) {
        return ResponseEntity.ok(aiKnowledgeService.getKnowledgeByEvent(eventId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MENTOR', 'EXPERT', 'ORGANIZER', 'ADMIN')")
    public ResponseEntity<Void> deleteKnowledge(@PathVariable String id, Authentication authentication) {
        aiKnowledgeService.deleteKnowledge(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
