package com.fpt.swp.sealhackathonbe.settings.service;

import com.fpt.swp.sealhackathonbe.settings.dto.SystemSettingsRequest;
import com.fpt.swp.sealhackathonbe.settings.dto.SystemSettingsResponse;

public interface SystemSettingService {
    SystemSettingsResponse getSettings();
    SystemSettingsResponse updateSettings(SystemSettingsRequest request);
}
