package com.fpt.swp.sealhackathonbe.core.constant;

import java.util.UUID;

/**
 * UUID của các UserType seed sẵn trong bảng UserType.
 * Tên hằng phải khớp typeName thật trong DB — trước đây hai hằng này bị đặt
 * nhầm là ROLE_ADMIN/ROLE_USER dù UUID trỏ tới FPT Student/External Student.
 */
public final class UserRoleConstants {
    public static final UUID ROLE_FPT_STUDENT = UUID.fromString("10000000-0000-0000-0000-000000000001");
    public static final UUID ROLE_EXTERNAL_STUDENT = UUID.fromString("10000000-0000-0000-0000-000000000002");

    private UserRoleConstants() {}
}
