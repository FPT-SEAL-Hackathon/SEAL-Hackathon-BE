package com.fpt.swp.sealhackathonbe.settings.controller;

import com.fpt.swp.sealhackathonbe.settings.dto.SystemSettingsRequest;
import com.fpt.swp.sealhackathonbe.settings.dto.SystemSettingsResponse;
import com.fpt.swp.sealhackathonbe.settings.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST API để admin đọc và cập nhật system settings.
 * GET  /api/v1/settings  — đọc settings hiện tại (ORGANIZER only)
 * PUT  /api/v1/settings  — cập nhật settings (ORGANIZER only)
 */
@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<SystemSettingsResponse> getSettings() {
        return ResponseEntity.ok(systemSettingService.getSettings());
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<SystemSettingsResponse> updateSettings(
            @RequestBody SystemSettingsRequest request) {
        return ResponseEntity.ok(systemSettingService.updateSettings(request));
    }
}
