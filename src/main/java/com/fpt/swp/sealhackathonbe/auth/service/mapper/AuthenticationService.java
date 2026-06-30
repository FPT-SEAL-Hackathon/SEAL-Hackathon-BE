package com.fpt.swp.sealhackathonbe.auth.service.mapper;

import com.fpt.swp.sealhackathonbe.user.entity.User;

/**
 * Định nghĩa cách lấy người dùng đang đăng nhập.
 */
public interface AuthenticationService {

    /**
     * Lấy user hiện tại từ SecurityContext.
     */
    public User getCurrentUser();
}
