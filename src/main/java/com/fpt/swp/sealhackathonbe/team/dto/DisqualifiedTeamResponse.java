package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DisqualifiedTeamResponse {
    // DTO cho man hinh admin: ghep thong tin team voi ly do va nguoi thuc hien loai.
    private UUID disqualificationId;
    private TeamResponse team;
    private String reason;
    private UUID disqualifiedById;
    private LocalDateTime disqualifiedAt;
}
