package com.fpt.swp.sealhackathonbe.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.swp.sealhackathonbe.ai.entity.AiKnowledgeBase;
import com.fpt.swp.sealhackathonbe.ai.service.GeminiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiServiceImpl implements GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String askAi(String question, List<AiKnowledgeBase> knowledgeBase) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Gemini API key is not configured. Returning UNKNOWN.");
            return "UNKNOWN";
        }
        if (knowledgeBase == null || knowledgeBase.isEmpty()) {
            return "UNKNOWN";
        }

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("You are an AI Mentor assisting students participating in the event.\n");
        contextBuilder.append("Your ONLY task is to match the student's question with the KNOWLEDGE BASE below.\n");
        contextBuilder.append("If the question matches the meaning of a FAQ in the knowledge base, YOU MUST REPLY EXACTLY WITH THE STANDARD ANSWER.\n");
        contextBuilder.append("If the question is NOT RELATED to any FAQ in the knowledge base, YOU MUST REPLY WITH EXACTLY 1 WORD: UNKNOWN.\n");
        contextBuilder.append("Absolutely no inferring, no fabricating information, and no long explanations.\n\n");
        contextBuilder.append("--- KNOWLEDGE BASE ---\n");

        for (int i = 0; i < knowledgeBase.size(); i++) {
            AiKnowledgeBase kb = knowledgeBase.get(i);
            contextBuilder.append("FAQ ").append(i + 1).append(":\n");
            contextBuilder.append("- Sample question: ").append(kb.getQuestionPattern()).append("\n");
            contextBuilder.append("- Standard answer: ").append(kb.getStandardAnswer()).append("\n\n");
        }

        contextBuilder.append("--- END OF KNOWLEDGE BASE ---\n\n");
        contextBuilder.append("Student's question: \"").append(question).append("\"\n");

        try {
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", contextBuilder.toString());
            
            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(part));
            
            requestBody.put("contents", List.of(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String fullUrl = apiUrl + "?key=" + apiKey;
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String aiResponse = root.path("candidates")
                        .path(0)
                        .path("content")
                        .path("parts")
                        .path(0)
                        .path("text")
                        .asText();
                return aiResponse.trim();
            }
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            return "API_ERROR: " + e.getMessage();
        }

        return "UNKNOWN";
    }
}
