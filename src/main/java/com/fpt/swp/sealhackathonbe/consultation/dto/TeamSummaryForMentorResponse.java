package com.fpt.swp.sealhackathonbe.consultation.dto;

import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TeamSummaryForMentorResponse {
    private UUID teamId;
    private String teamName;
    private UUID categoryId;
    private String categoryName;
    private UUID eventId;
    private String eventName;
    private String teamStatus;
    private int memberCount;
    private long openRequestCount; // số request đang mở (PENDING/ACCEPTED/IN_PROGRESS)
    private long totalRequestCount;

    public static TeamSummaryForMentorResponse from(Teams team, int memberCount, long openRequests, long totalRequests) {
        return TeamSummaryForMentorResponse.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .categoryId(team.getCategory().getCategoryId())
                .categoryName(team.getCategory().getCategoryName())
                .eventId(team.getEvent().getEventId())
                .eventName(team.getEvent().getEventName())
                .teamStatus(team.getTeamStatus() != null ? team.getTeamStatus().getStatusName() : "Unknown")
                .memberCount(memberCount)
                .openRequestCount(openRequests)
                .totalRequestCount(totalRequests)
                .build();
    }
}
