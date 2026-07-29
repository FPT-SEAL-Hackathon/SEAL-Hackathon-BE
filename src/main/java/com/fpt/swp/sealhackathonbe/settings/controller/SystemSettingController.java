package com.fpt.swp.sealhackathonbe.settings.controller;

import com.fpt.swp.sealhackathonbe.settings.dto.LandingPageSettingsDto;
import com.fpt.swp.sealhackathonbe.settings.dto.SystemSettingsRequest;
import com.fpt.swp.sealhackathonbe.settings.dto.SystemSettingsResponse;
import com.fpt.swp.sealhackathonbe.settings.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST API để admin đọc và cập nhật system settings.
 * GET  /api/v1/settings  — đọc settings hiện tại (ORGANIZER/ADMIN)
 * PUT  /api/v1/settings  — cập nhật settings (ORGANIZER/ADMIN)
 * GET  /api/v1/settings/landing — đọc landing page settings (Public)
 * PUT  /api/v1/settings/landing — cập nhật landing page settings (ADMIN)
 */
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemSettingsResponse> getSettings() {
        return ResponseEntity.ok(systemSettingService.getSettings());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemSettingsResponse> updateSettings(
            @RequestBody SystemSettingsRequest request) {
        return ResponseEntity.ok(systemSettingService.updateSettings(request));
    }

    @GetMapping("/landing")
    public ResponseEntity<LandingPageSettingsDto> getLandingSettings() {
        return ResponseEntity.ok(systemSettingService.getLandingSettings());
    }

    @PutMapping("/landing")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LandingPageSettingsDto> updateLandingSettings(
            @RequestBody LandingPageSettingsDto request) {
        return ResponseEntity.ok(systemSettingService.updateLandingSettings(request));
    }
}
