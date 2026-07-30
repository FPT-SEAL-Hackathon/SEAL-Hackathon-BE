package com.fpt.swp.sealhackathonbe.settings.service.impl;

import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
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
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FptStudentCodePrefixServiceImpl implements FptStudentCodePrefixService {

    public static final String MSG_FPT_CODE =
            "FPT student code must start with an active FPT major prefix and be followed by 6 digits (e.g. SE123456).";

    private static final Pattern CODE_SHAPE = Pattern.compile("^[A-Z]{2}\\d{6}$");

    private final FptStudentCodePrefixRepository repository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FptStudentCodePrefixResponse> list(boolean includeInactive) {
        try {
            List<FptStudentCodePrefix> rows = includeInactive
                    ? repository.findAllByOrderByMajorGroupAscPrefixAsc()
                    : repository.findByIsActiveTrueOrderByMajorGroupAscPrefixAsc();
            if (rows == null) {
                return List.of();
            }
            return rows.stream()
                    .filter(java.util.Objects::nonNull)
                    .map(this::toResponse)
                    .toList();
        } catch (Exception ex) {
            log.error("Failed to fetch FPT student code prefixes: {}", ex.getMessage(), ex);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FptStudentCodePrefixResponse> listWithUsage(boolean includeInactive) {
        List<FptStudentCodePrefixResponse> rows = list(includeInactive);
        Map<String, Long> usage = loadUsageCounts();
        return rows.stream()
                .map(r -> r.toBuilder()
                        .usageCount(usage.getOrDefault(r.getPrefix(), 0L))
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public FptStudentCodePrefixResponse create(FptStudentCodePrefixRequest request) {
        String prefix = normalizePrefix(request.getPrefix());
        // Prefix là khoá chính. Trước đây dùng upsert nên "thêm" một prefix đã tồn tại sẽ GHI
        // ĐÈ âm thầm toàn bộ tên ngành/nhóm ngành cũ mà không báo gì — mất dữ liệu. Tạo mới
        // giờ phải báo lỗi rõ ràng; sửa thì dùng update().
        if (repository.existsById(prefix)) {
            throw new BusinessConflictException("Prefix " + prefix + " already exists. Edit it instead.");
        }
        FptStudentCodePrefix row = new FptStudentCodePrefix();
        row.setPrefix(prefix);
        applyRequest(row, request);
        return toResponse(repository.save(row));
    }

    @Override
    @Transactional
    public FptStudentCodePrefixResponse update(String prefix, FptStudentCodePrefixRequest request) {
        String normalized = normalizePrefix(prefix);
        FptStudentCodePrefix row = repository.findById(normalized)
                .orElseThrow(() -> new EntityNotFoundException("FPT student code prefix not found"));
        // Prefix là khoá chính nên KHÔNG cho đổi qua update (đổi = xoá + tạo mới, sẽ làm mọi
        // MSSV cũ mồ côi). Body có gửi prefix khác cũng bỏ qua, lấy theo path variable.
        applyRequest(row, request);
        return toResponse(repository.save(row));
    }

    private void applyRequest(FptStudentCodePrefix row, FptStudentCodePrefixRequest request) {
        row.setEnglishName(request.getEnglishName().trim());
        row.setVietnameseName(request.getVietnameseName().trim());
        row.setMajorGroup(request.getMajorGroup().trim());
        row.setMajorCode(trimToNull(request.getMajorCode()));
        row.setNote(trimToNull(request.getNote()));
        row.setIsActive(request.getActive() == null || request.getActive());
    }

    private Map<String, Long> loadUsageCounts() {
        try {
            return userRepository.countGroupedByFptStudentCodePrefix().stream()
                    .filter(r -> r != null && r.length == 2 && r[0] != null)
                    .collect(Collectors.toMap(
                            r -> String.valueOf(r[0]).toUpperCase(Locale.ROOT),
                            r -> ((Number) r[1]).longValue(),
                            (a, b) -> a + b
                    ));
        } catch (Exception ex) {
            // Số đếm chỉ mang tính tham khảo — hỏng thì vẫn phải trả được danh sách prefix.
            log.error("Failed to count FPT student code prefix usage: {}", ex.getMessage(), ex);
            return Map.of();
        }
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

    /**
     * CỐ Ý chỉ kiểm tra ĐỊNH DẠNG MSSV (ProfileValidation), KHÔNG kiểm tra prefix còn active.
     *
     * Tắt một prefix chỉ có nghĩa "không nhận tài khoản MỚI với prefix này" — các tài khoản đã
     * có được giữ nguyên. Trước đây hàm này gắn thêm MSG_FPT_CODE khi prefix bị tắt, khiến
     * sinh viên cũ dính banner "hồ sơ chưa hợp lệ" và lọt vào danh sách "Scan & remind" của
     * Organizer, trong khi họ KHÔNG THỂ sửa: MSSV thật của họ đúng là bắt đầu bằng prefix đó.
     * Việc chặn prefix đã tắt vẫn nằm ở các luồng ghi (đăng ký, hoàn thiện hồ sơ, đổi MSSV).
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> profileIssues(User user) {
        return new ArrayList<>(ProfileValidation.profileIssues(user));
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
        if (row == null) {
            return null;
        }
        return FptStudentCodePrefixResponse.builder()
                .prefix(row.getPrefix())
                .englishName(row.getEnglishName())
                .vietnameseName(row.getVietnameseName())
                .majorGroup(row.getMajorGroup())
                .majorCode(row.getMajorCode())
                .note(row.getNote())
                .active(Boolean.TRUE.equals(row.getIsActive()))
                .createdAt(row.getCreatedAt())
                .updatedAt(row.getUpdatedAt())
                .build();
    }
}
