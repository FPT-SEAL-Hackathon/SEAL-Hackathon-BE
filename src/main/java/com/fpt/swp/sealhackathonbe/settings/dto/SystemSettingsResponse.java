package com.fpt.swp.sealhackathonbe.settings.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Response trả về toàn bộ system settings hiện tại.
 */
@Getter
@Setter
@Builder
public class SystemSettingsResponse {
    private String platformName;
    private Integer maxTeamSize;
    private Integer minTeamSize;
    private Integer submissionGracePeriod;
    private String contactEmail;
    private Boolean allowLateSubmissions;
    private Boolean enablePublicLeaderboard;
    private Boolean requireEmailVerification;
}
