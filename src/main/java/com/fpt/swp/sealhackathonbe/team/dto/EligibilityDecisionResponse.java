package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.Data;

@Data
public class EligibilityDecisionResponse {
    private Boolean approved;
    private String message;
    private TeamResponse team;
    private DisqualificationResponse disqualification;
}
