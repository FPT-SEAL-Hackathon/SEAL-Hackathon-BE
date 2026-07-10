package com.fpt.swp.sealhackathonbe.settings.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Request body để lưu toàn bộ system settings từ admin dashboard.
 */
@Getter
@Setter
public class SystemSettingsRequest {
    private String platformName;
    private Integer maxTeamSize;
    private Integer minTeamSize;
    private Integer submissionGracePeriod;
    private String contactEmail;
    private Boolean allowLateSubmissions;
    private Boolean enablePublicLeaderboard;
    private Boolean requireEmailVerification;
}
