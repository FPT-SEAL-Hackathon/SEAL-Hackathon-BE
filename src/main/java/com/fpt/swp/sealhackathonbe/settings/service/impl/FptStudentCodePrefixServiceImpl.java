package com.fpt.swp.sealhackathonbe.settings.service.impl;

import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.settings.dto.FptStudentCodePrefixRequest;
import com.fpt.swp.sealhackathonbe.settings.dto.FptStudentCodePrefixResponse;
import com.fpt.swp.sealhackathonbe.settings.entity.FptStudentCodePrefix;
import com.fpt.swp.sealhackathonbe.settings.repository.FptStudentCodePrefixRepository;
import com.fpt.swp.sealhackathonbe.settings.service.FptStudentCodePrefixService;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.util.ProfileValidation;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class FptStudentCodePrefixServiceImpl implements FptStudentCodePrefixService {

    public static final String MSG_FPT_CODE =
            "FPT student code must start with an active FPT major prefix and be followed by 6 digits (e.g. SE123456).";

    private static final Pattern CODE_SHAPE = Pattern.compile("^[A-Z]{2}\\d{6}$");

    private final FptStudentCodePrefixRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<FptStudentCodePrefixResponse> list(boolean includeInactive) {
        List<FptStudentCodePrefix> rows = includeInactive
                ? repository.findAllByOrderByMajorGroupAscPrefixAsc()
                : repository.findByIsActiveTrueOrderByMajorGroupAscPrefixAsc();
        return rows.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public FptStudentCodePrefixResponse upsert(FptStudentCodePrefixRequest request) {
        String prefix = normalizePrefix(request.getPrefix());
        FptStudentCodePrefix row = repository.findById(prefix).orElseGet(FptStudentCodePrefix::new);
        row.setPrefix(prefix);
        row.setEnglishName(request.getEnglishName().trim());
        row.setVietnameseName(request.getVietnameseName().trim());
        row.setMajorGroup(request.getMajorGroup().trim());
        row.setMajorCode(trimToNull(request.getMajorCode()));
        row.setNote(trimToNull(request.getNote()));
        row.setIsActive(request.getActive() == null || request.getActive());
        return toResponse(repository.save(row));
    }

    @Override
    @Transactional
    public FptStudentCodePrefixResponse setActive(String prefix, boolean active) {
        FptStudentCodePrefix row = repository.findById(normalizePrefix(prefix))
                .orElseThrow(() -> new EntityNotFoundException("FPT student code prefix not found"));
        row.setIsActive(active);
        return toResponse(repository.save(row));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidActiveFptStudentCode(String value) {
        String normalized = normalizeFptStudentCode(value);
        if (!CODE_SHAPE.matcher(normalized).matches()) {
            return false;
        }
        return repository.existsByPrefixIgnoreCaseAndIsActiveTrue(normalized.substring(0, 2));
    }

    @Override
    public String normalizeFptStudentCode(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> profileIssues(User user) {
        List<String> issues = new ArrayList<>(ProfileValidation.profileIssues(user));
        String role = user.getUserType() != null ? user.getUserType().getTypeName() : null;
        if ("FPT Student".equalsIgnoreCase(role)
                && ProfileValidation.isValidFptStudentCode(user.getFptStudentCode())
                && !isValidActiveFptStudentCode(user.getFptStudentCode())
                && !issues.contains(MSG_FPT_CODE)) {
            issues.add(MSG_FPT_CODE);
        }
        return issues;
    }

    private String normalizePrefix(String value) {
        String prefix = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!prefix.matches("^[A-Z]{2}$")) {
            throw new BadRequestException("Prefix must contain exactly 2 letters.");
        }
        return prefix;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private FptStudentCodePrefixResponse toResponse(FptStudentCodePrefix row) {
        return FptStudentCodePrefixResponse.builder()
                .prefix(row.getPrefix())
                .englishName(row.getEnglishName())
                .vietnameseName(row.getVietnameseName())
                .majorGroup(row.getMajorGroup())
                .majorCode(row.getMajorCode())
                .note(row.getNote())
                .active(row.getIsActive())
                .createdAt(row.getCreatedAt())
                .updatedAt(row.getUpdatedAt())
                .build();
    }
}
