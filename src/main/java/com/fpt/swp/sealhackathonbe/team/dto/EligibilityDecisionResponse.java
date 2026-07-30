package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.Data;

@Data
public class EligibilityDecisionResponse {
    // Ket qua tra ve sau khi organizer approve/reject eligibility; reject co the kem ban ghi disqualification.
    private Boolean approved;
    private String message;
    private TeamResponse team;
    private DisqualificationResponse disqualification;
}
