package com.fpt.swp.sealhackathonbe.core.exception;

import lombok.Getter;

import java.util.List;

/**
 * Hồ sơ trùng với tài khoản khác (email/phone/student code).
 * Trả 409 PROFILE_CONFLICT kèm danh sách field trùng; không auto-merge.
 */
@Getter
public class ProfileConflictException extends RuntimeException {

    private final List<String> conflictFields;
    private final boolean canLinkAccount;

    public ProfileConflictException(String message, List<String> conflictFields, boolean canLinkAccount) {
        super(message);
        this.conflictFields = conflictFields;
        this.canLinkAccount = canLinkAccount;
    }
}
