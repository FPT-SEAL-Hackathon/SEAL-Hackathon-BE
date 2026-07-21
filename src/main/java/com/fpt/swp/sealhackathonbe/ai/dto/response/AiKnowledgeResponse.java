package com.fpt.swp.sealhackathonbe.ai.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class AiKnowledgeResponse {
    private String id;
    private String eventId;
    private String categoryId;
    private String questionPattern;
    private String standardAnswer;
    private String mentorId;
    private String mentorName;
    private Date createdAt;
}
