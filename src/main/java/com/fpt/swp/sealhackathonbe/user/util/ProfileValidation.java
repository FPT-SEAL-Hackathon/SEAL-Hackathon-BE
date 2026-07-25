package com.fpt.swp.sealhackathonbe.user.util;

import com.fpt.swp.sealhackathonbe.user.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Quy tắc chuẩn hóa hồ sơ dùng chung cho mọi luồng validate + phát hiện account chưa chuẩn.
 * - FPT student code: SE/SS/SA + 6 số (vd SE123456).
 * - External student code: linh hoạt (trường ngoài đa dạng) — alphanumeric + . _ - , dài 3–50.
 * - SĐT Việt Nam: 0 hoặc +84, đầu số 3/5/7/8/9, tổng 10 số (sau khi bỏ khoảng trắng/ký tự phân tách).
 * Dữ liệu cũ KHÔNG bị chặn tự động; chỉ dùng để phát hiện + nhắc, và enforce khi user SỬA field.
 */
public final class ProfileValidation {

    private ProfileValidation() {}

    private static final Pattern FPT_CODE = Pattern.compile("^(SE|SS|SA)\\d{6}$");
    private static final Pattern EXTERNAL_CODE = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]{2,49}$");
    private static final Pattern VN_PHONE = Pattern.compile("^(?:\\+84|0)(?:3|5|7|8|9)\\d{8}$");

    private static final String ROLE_FPT_STUDENT = "FPT Student";
    private static final String ROLE_EXTERNAL_STUDENT = "External Student";

    public static final String MSG_FPT_CODE = "Mã sinh viên FPT phải có dạng SE/SS/SA + 6 số (ví dụ SE123456).";
    public static final String MSG_EXTERNAL_CODE = "Mã sinh viên (ngoài trường) chưa hợp lệ (3–50 ký tự chữ/số, cho . _ -).";
    public static final String MSG_UNIVERSITY = "Sinh viên ngoài trường cần điền tên trường đại học.";
    public static final String MSG_PHONE = "Số điện thoại phải là số di động Việt Nam hợp lệ (vd 0912345678).";

    public static boolean isValidFptStudentCode(String value) {
        return value != null && FPT_CODE.matcher(value.trim()).matches();
    }

    public static boolean isValidExternalStudentCode(String value) {
        return value != null && EXTERNAL_CODE.matcher(value.trim()).matches();
    }

    public static boolean isValidVietnamesePhone(String value) {
        if (value == null) return false;
        String normalized = value.trim().replaceAll("[\\s.()\\-]", "");
        return VN_PHONE.matcher(normalized).matches();
    }

    /**
     * Danh sách lỗi chuẩn hóa của một user (rỗng = đã chuẩn). Dùng cho banner + scan nhắc.
     * SĐT rỗng KHÔNG bị coi là lỗi (không bắt buộc); chỉ lỗi khi có SĐT nhưng sai định dạng VN.
     */
    public static List<String> profileIssues(User user) {
        List<String> issues = new ArrayList<>();
        String role = user.getUserType() != null ? user.getUserType().getTypeName() : null;

        if (ROLE_FPT_STUDENT.equalsIgnoreCase(role)) {
            if (!isValidFptStudentCode(user.getFptStudentCode())) {
                issues.add(MSG_FPT_CODE);
            }
        } else if (ROLE_EXTERNAL_STUDENT.equalsIgnoreCase(role)) {
            if (!isValidExternalStudentCode(user.getExternalStudentCode())) {
                issues.add(MSG_EXTERNAL_CODE);
            }
            if (isBlank(user.getUniversityName())) {
                issues.add(MSG_UNIVERSITY);
            }
        }

        String phone = user.getPhone();
        if (!isBlank(phone) && !isValidVietnamesePhone(phone)) {
            issues.add(MSG_PHONE);
        }
        return issues;
    }

    public static boolean isCompliant(User user) {
        return profileIssues(user).isEmpty();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
