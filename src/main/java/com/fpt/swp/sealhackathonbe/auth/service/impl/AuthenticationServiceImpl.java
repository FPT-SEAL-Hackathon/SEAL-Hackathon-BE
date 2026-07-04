package com.fpt.swp.sealhackathonbe.auth.service.impl;

import com.fpt.swp.sealhackathonbe.auth.service.mapper.AuthenticationService;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserPrincipal;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Cung cấp người dùng hiện tại từ SecurityContext cho các xử lý cần ownership.
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    /**
     * Permission:
     * Trả về user đang đăng nhập hoặc ném lỗi nếu request chưa xác thực.
     */
    public User getCurrentUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser")) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }

        if (!(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }

        return principal.getUser();
    }
}
