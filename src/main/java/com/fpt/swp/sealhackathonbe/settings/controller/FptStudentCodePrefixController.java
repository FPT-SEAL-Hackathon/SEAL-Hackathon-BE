package com.fpt.swp.sealhackathonbe.settings.controller;

import com.fpt.swp.sealhackathonbe.settings.dto.FptStudentCodePrefixRequest;
import com.fpt.swp.sealhackathonbe.settings.dto.FptStudentCodePrefixResponse;
import com.fpt.swp.sealhackathonbe.settings.service.FptStudentCodePrefixService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fpt-student-code-prefixes")
@RequiredArgsConstructor
public class FptStudentCodePrefixController {

    private final FptStudentCodePrefixService service;

    @GetMapping
    public ResponseEntity<List<FptStudentCodePrefixResponse>> list() {
        return ResponseEntity.ok(service.list(false));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<FptStudentCodePrefixResponse>> adminList(
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return ResponseEntity.ok(service.list(includeInactive));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<FptStudentCodePrefixResponse> upsert(
            @Valid @RequestBody FptStudentCodePrefixRequest request
    ) {
        return ResponseEntity.ok(service.upsert(request));
    }

    @PatchMapping("/{prefix}/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<FptStudentCodePrefixResponse> setActive(
            @PathVariable String prefix,
            @RequestParam boolean active
    ) {
        return ResponseEntity.ok(service.setActive(prefix, active));
    }
}
