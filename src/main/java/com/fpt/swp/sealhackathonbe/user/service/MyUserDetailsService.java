package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserPrincipal;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Nạp tài khoản theo email cho Spring Security khi đăng nhập.
 */
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Tạo UserPrincipal để Spring kiểm tra mật khẩu và quyền ROLE_*.
     * Đăng nhập local chỉ xét tài khoản LOCAL-enabled: email có thể trùng
     * với tài khoản OAuth nhưng tài khoản OAuth không đăng nhập bằng mật khẩu.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository
                .findFirstByEmailAndLocalLoginEnabledTrueAndIsDeletedFalseOrderByCreatedAtAsc(email)
                .orElse(null);
        if (user == null) {
            // Fallback cho phiên JWT của user OAuth-only (không có mật khẩu local).
            user = userRepository.findByEmail(email);
        }
        if (user == null) {
            throw new UsernameNotFoundException("user not found");
        }

        return new UserPrincipal(user);
    }
}
