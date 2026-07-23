package com.fpt.swp.sealhackathonbe.ai.service;

import com.fpt.swp.sealhackathonbe.ai.entity.AiKnowledgeBase;

import java.util.List;

public interface GeminiService {
    /**
     * Ask Gemini a question given a list of known facts (knowledge base).
     * @param question The student's question.
     * @param knowledgeBase List of FAQ patterns and standard answers.
     * @return The standard answer if matched, or "UNKNOWN".
     */
    String askAi(String question, List<AiKnowledgeBase> knowledgeBase);
}
