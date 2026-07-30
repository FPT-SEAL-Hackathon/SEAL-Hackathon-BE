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
import org.springframework.web.bind.annotation.PutMapping;
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

    // Danh sách Admin kèm usageCount (số tài khoản đang dùng từng prefix) để admin biết ảnh
    // hưởng trước khi tắt một prefix.
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<FptStudentCodePrefixResponse>> adminList(
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return ResponseEntity.ok(service.listWithUsage(includeInactive));
    }

    // POST = TẠO MỚI (409 nếu prefix đã tồn tại). Trước đây đây là upsert nên thêm trùng sẽ
    // ghi đè âm thầm bản ghi cũ; sửa prefix giờ phải gọi PUT bên dưới.
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<FptStudentCodePrefixResponse> create(
            @Valid @RequestBody FptStudentCodePrefixRequest request
    ) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{prefix}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<FptStudentCodePrefixResponse> update(
            @PathVariable String prefix,
            @Valid @RequestBody FptStudentCodePrefixRequest request
    ) {
        return ResponseEntity.ok(service.update(prefix, request));
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
