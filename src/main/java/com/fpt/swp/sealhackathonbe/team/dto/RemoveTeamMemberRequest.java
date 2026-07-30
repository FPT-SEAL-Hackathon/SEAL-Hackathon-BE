package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.Data;

@Data
public class RemoveTeamMemberRequest {
    // Optional reason khi leader kick member hoac member tu roi team.
    private String reason;
}
