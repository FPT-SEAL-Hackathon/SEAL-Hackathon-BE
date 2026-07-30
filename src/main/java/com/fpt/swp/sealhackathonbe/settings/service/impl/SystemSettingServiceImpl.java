package com.fpt.swp.sealhackathonbe.settings.service.impl;

// Jackson 3 bo JsonProcessingException; moi loi serialize/deserialize nem
// JacksonException va no la UNCHECKED (extends RuntimeException).
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.fpt.swp.sealhackathonbe.settings.dto.LandingPageSettingsDto;
import com.fpt.swp.sealhackathonbe.settings.dto.SystemSettingsRequest;
import com.fpt.swp.sealhackathonbe.settings.dto.SystemSettingsResponse;
import com.fpt.swp.sealhackathonbe.settings.entity.SystemSetting;
import com.fpt.swp.sealhackathonbe.settings.repository.SystemSettingRepository;
import com.fpt.swp.sealhackathonbe.settings.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Lưu và đọc cài đặt hệ thống dưới dạng key-value.
 * Mỗi lần save sẽ upsert toàn bộ các key setting.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemSettingServiceImpl implements SystemSettingService {

    private static final String KEY_PLATFORM_NAME           = "platformName";
    private static final String KEY_MAX_TEAM_SIZE           = "maxTeamSize";
    private static final String KEY_MIN_TEAM_SIZE           = "minTeamSize";
    private static final String KEY_SUBMISSION_GRACE_PERIOD = "submissionGracePeriod";
    private static final String KEY_CONTACT_EMAIL           = "contactEmail";
    private static final String KEY_ALLOW_LATE_SUBMISSIONS  = "allowLateSubmissions";
    private static final String KEY_ENABLE_PUBLIC_LEADERBOARD = "enablePublicLeaderboard";
    private static final String KEY_REQUIRE_EMAIL_VERIFICATION = "requireEmailVerification";

    private static final String KEY_LANDING_GALLERY         = "landing_gallery";
    private static final String KEY_LANDING_FOOTER          = "landing_footer";

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
    private final ObjectMapper objectMapper;

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

    @Override
    public LandingPageSettingsDto getLandingSettings() {
        SystemSetting gallerySetting = repository.findById(KEY_LANDING_GALLERY).orElse(null);
        SystemSetting footerSetting = repository.findById(KEY_LANDING_FOOTER).orElse(null);

        List<LandingPageSettingsDto.LandingGalleryItemDto> gallery = null;
        if (gallerySetting != null && gallerySetting.getSettingValue() != null && !gallerySetting.getSettingValue().isBlank()) {
            try {
                gallery = objectMapper.readValue(gallerySetting.getSettingValue(), new TypeReference<List<LandingPageSettingsDto.LandingGalleryItemDto>>() {});
            } catch (Exception e) {
                log.error("Failed to parse landing gallery JSON from DB", e);
            }
        }

        LandingPageSettingsDto.LandingFooterDto footer = null;
        if (footerSetting != null && footerSetting.getSettingValue() != null && !footerSetting.getSettingValue().isBlank()) {
            try {
                footer = objectMapper.readValue(footerSetting.getSettingValue(), LandingPageSettingsDto.LandingFooterDto.class);
            } catch (Exception e) {
                log.error("Failed to parse landing footer JSON from DB", e);
            }
        }

        return LandingPageSettingsDto.builder()
                .gallery(gallery)
                .footer(footer)
                .build();
    }

    @Override
    @Transactional
    public LandingPageSettingsDto updateLandingSettings(LandingPageSettingsDto request) {
        if (request.getGallery() != null) {
            try {
                String galleryJson = objectMapper.writeValueAsString(request.getGallery());
                upsert(KEY_LANDING_GALLERY, galleryJson, "JSON");
            } catch (JacksonException e) {
                log.error("Failed to serialize landing gallery to JSON", e);
            }
        }

        if (request.getFooter() != null) {
            try {
                String footerJson = objectMapper.writeValueAsString(request.getFooter());
                upsert(KEY_LANDING_FOOTER, footerJson, "JSON");
            } catch (JacksonException e) {
                log.error("Failed to serialize landing footer to JSON", e);
            }
        }

        return getLandingSettings();
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
