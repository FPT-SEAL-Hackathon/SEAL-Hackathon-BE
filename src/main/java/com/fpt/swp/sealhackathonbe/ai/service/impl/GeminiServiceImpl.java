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
        contextBuilder.append("Bạn là AI Mentor hỗ trợ giải đáp thắc mắc cho sinh viên tham gia sự kiện.\n");
        contextBuilder.append("Nhiệm vụ DUY NHẤT của bạn là đối chiếu câu hỏi của sinh viên với CUỐN SỔ TAY dưới đây.\n");
        contextBuilder.append("Nếu câu hỏi khớp với ý nghĩa của một FAQ trong sổ tay, HÃY TRẢ LỜI ĐÚNG NHƯ CÂU TRẢ LỜI CHUẨN.\n");
        contextBuilder.append("Nếu câu hỏi KHÔNG LIÊN QUAN đến bất kỳ FAQ nào trong sổ, BẠN PHẢI TRẢ LỜI ĐÚNG 1 CHỮ: UNKNOWN.\n");
        contextBuilder.append("Tuyệt đối không suy luận, không bịa thông tin, không giải thích dài dòng.\n\n");
        contextBuilder.append("--- CUỐN SỔ TAY ---\n");

        for (int i = 0; i < knowledgeBase.size(); i++) {
            AiKnowledgeBase kb = knowledgeBase.get(i);
            contextBuilder.append("FAQ ").append(i + 1).append(":\n");
            contextBuilder.append("- Câu hỏi mẫu: ").append(kb.getQuestionPattern()).append("\n");
            contextBuilder.append("- Câu trả lời chuẩn: ").append(kb.getStandardAnswer()).append("\n\n");
        }

        contextBuilder.append("--- HẾT SỔ TAY ---\n\n");
        contextBuilder.append("Câu hỏi của sinh viên: \"").append(question).append("\"\n");

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
