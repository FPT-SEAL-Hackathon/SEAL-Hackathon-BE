package com.fpt.swp.sealhackathonbe.settings.service;

import com.fpt.swp.sealhackathonbe.settings.dto.FptStudentCodePrefixRequest;
import com.fpt.swp.sealhackathonbe.settings.dto.FptStudentCodePrefixResponse;
import com.fpt.swp.sealhackathonbe.user.entity.User;

import java.util.List;

public interface FptStudentCodePrefixService {
    List<FptStudentCodePrefixResponse> list(boolean includeInactive);

    /** Danh sách cho màn Admin, kèm số tài khoản đang dùng từng prefix. */
    List<FptStudentCodePrefixResponse> listWithUsage(boolean includeInactive);

    /** Tạo mới. Ném BusinessConflictException nếu prefix đã tồn tại (không ghi đè). */
    FptStudentCodePrefixResponse create(FptStudentCodePrefixRequest request);

    /** Cập nhật prefix đã có. Ném EntityNotFoundException nếu chưa tồn tại. */
    FptStudentCodePrefixResponse update(String prefix, FptStudentCodePrefixRequest request);

    FptStudentCodePrefixResponse setActive(String prefix, boolean active);
    boolean isValidActiveFptStudentCode(String value);
    String normalizeFptStudentCode(String value);
    List<String> profileIssues(User user);
}
