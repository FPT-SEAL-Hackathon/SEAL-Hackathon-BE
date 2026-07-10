package com.fpt.swp.sealhackathonbe.settings.service.impl;

import com.fpt.swp.sealhackathonbe.settings.dto.SystemSettingsRequest;
import com.fpt.swp.sealhackathonbe.settings.dto.SystemSettingsResponse;
import com.fpt.swp.sealhackathonbe.settings.entity.SystemSetting;
import com.fpt.swp.sealhackathonbe.settings.repository.SystemSettingRepository;
import com.fpt.swp.sealhackathonbe.settings.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Lưu và đọc cài đặt hệ thống dưới dạng key-value.
 * Mỗi lần save sẽ upsert toàn bộ các key setting.
 */
@Service
@RequiredArgsConstructor
public class SystemSettingServiceImpl implements SystemSettingService {

    private static final String KEY_PLATFORM_NAME           = "platformName";
    private static final String KEY_MAX_TEAM_SIZE           = "maxTeamSize";
    private static final String KEY_MIN_TEAM_SIZE           = "minTeamSize";
    private static final String KEY_SUBMISSION_GRACE_PERIOD = "submissionGracePeriod";
    private static final String KEY_CONTACT_EMAIL           = "contactEmail";
    private static final String KEY_ALLOW_LATE_SUBMISSIONS  = "allowLateSubmissions";
    private static final String KEY_ENABLE_PUBLIC_LEADERBOARD = "enablePublicLeaderboard";
    private static final String KEY_REQUIRE_EMAIL_VERIFICATION = "requireEmailVerification";

    // Default values khi chưa có bản ghi trong DB
    private static final Map<String, String> DEFAULTS = Map.of(
            KEY_PLATFORM_NAME,             "SEAL FPT Hackathon Platform",
            KEY_MAX_TEAM_SIZE,             "5",
            KEY_MIN_TEAM_SIZE,             "2",
            KEY_SUBMISSION_GRACE_PERIOD,   "30",
            KEY_CONTACT_EMAIL,             "seal@fpt.edu.vn",
            KEY_ALLOW_LATE_SUBMISSIONS,    "true",
            KEY_ENABLE_PUBLIC_LEADERBOARD, "true",
            KEY_REQUIRE_EMAIL_VERIFICATION,"true"
    );

    private final SystemSettingRepository repository;

    /**
     * Đọc tất cả setting từ DB, nếu thiếu key thì trả về default.
     */
    @Override
    public SystemSettingsResponse getSettings() {
        Map<String, SystemSetting> map = new java.util.HashMap<>();
        repository.findAll().forEach(s -> map.put(s.getSettingKey(), s));

        return SystemSettingsResponse.builder()
                .platformName(getValue(map, KEY_PLATFORM_NAME))
                .maxTeamSize(getIntValue(map, KEY_MAX_TEAM_SIZE))
                .minTeamSize(getIntValue(map, KEY_MIN_TEAM_SIZE))
                .submissionGracePeriod(getIntValue(map, KEY_SUBMISSION_GRACE_PERIOD))
                .contactEmail(getValue(map, KEY_CONTACT_EMAIL))
                .allowLateSubmissions(getBoolValue(map, KEY_ALLOW_LATE_SUBMISSIONS))
                .enablePublicLeaderboard(getBoolValue(map, KEY_ENABLE_PUBLIC_LEADERBOARD))
                .requireEmailVerification(getBoolValue(map, KEY_REQUIRE_EMAIL_VERIFICATION))
                .build();
    }

    /**
     * Upsert tất cả key settings theo request.
     * Chỉ ORGANIZER mới được gọi (được enforce tại Controller).
     */
    @Override
    @Transactional
    public SystemSettingsResponse updateSettings(SystemSettingsRequest request) {
        if (request.getPlatformName() != null)
            upsert(KEY_PLATFORM_NAME, request.getPlatformName(), "STRING");
        if (request.getMaxTeamSize() != null)
            upsert(KEY_MAX_TEAM_SIZE, String.valueOf(request.getMaxTeamSize()), "INTEGER");
        if (request.getMinTeamSize() != null)
            upsert(KEY_MIN_TEAM_SIZE, String.valueOf(request.getMinTeamSize()), "INTEGER");
        if (request.getSubmissionGracePeriod() != null)
            upsert(KEY_SUBMISSION_GRACE_PERIOD, String.valueOf(request.getSubmissionGracePeriod()), "INTEGER");
        if (request.getContactEmail() != null)
            upsert(KEY_CONTACT_EMAIL, request.getContactEmail(), "STRING");
        if (request.getAllowLateSubmissions() != null)
            upsert(KEY_ALLOW_LATE_SUBMISSIONS, String.valueOf(request.getAllowLateSubmissions()), "BOOLEAN");
        if (request.getEnablePublicLeaderboard() != null)
            upsert(KEY_ENABLE_PUBLIC_LEADERBOARD, String.valueOf(request.getEnablePublicLeaderboard()), "BOOLEAN");
        if (request.getRequireEmailVerification() != null)
            upsert(KEY_REQUIRE_EMAIL_VERIFICATION, String.valueOf(request.getRequireEmailVerification()), "BOOLEAN");

        return getSettings();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void upsert(String key, String value, String type) {
        SystemSetting setting = repository.findById(key)
                .orElse(SystemSetting.builder()
                        .settingKey(key)
                        .settingType(type)
                        .build());
        setting.setSettingValue(value);
        repository.save(setting);
    }

    private String getValue(Map<String, SystemSetting> map, String key) {
        SystemSetting s = map.get(key);
        return s != null ? s.getSettingValue() : DEFAULTS.getOrDefault(key, "");
    }

    private Integer getIntValue(Map<String, SystemSetting> map, String key) {
        try {
            return Integer.parseInt(getValue(map, key));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean getBoolValue(Map<String, SystemSetting> map, String key) {
        return Boolean.parseBoolean(getValue(map, key));
    }
}
