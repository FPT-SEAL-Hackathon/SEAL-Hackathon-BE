package com.fpt.swp.sealhackathonbe.consultation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMentorNoteResponse {
    private UUID teamId;
    private UUID mentorId;
    private String note;
    private LocalDateTime updatedAt;
}
