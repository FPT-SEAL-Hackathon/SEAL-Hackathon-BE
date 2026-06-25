package com.fpt.swp.sealhackathonbe.user.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Chuyển User nội bộ sang UserDetails để Spring Security sử dụng.
 */
public class UserPrincipal implements UserDetails {
    private User user;

    /**
     * Bọc User để phục vụ xác thực mật khẩu và kiểm tra quyền.
     */
    public UserPrincipal(User user) {
        this.user = user;
    }

    /**
     * RBAC:
     * Chuyển UserType thành ROLE_* để @PreAuthorize kiểm tra.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = user.getUserType() != null && user.getUserType().getTypeName() != null
                ? user.getUserType().getTypeName().replace(" ", "_").toUpperCase()
                : "USER";
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role));
    }

    /**
     * Password:
     * Cung cấp password hash đã mã hóa cho Spring Security.
     */
    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    /**
     * Dùng email làm định danh đăng nhập.
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * Chưa áp dụng kiểm tra hết hạn tài khoản ở lớp này.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Chưa áp dụng kiểm tra khóa tài khoản ở lớp này.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Permission:
     * Trả User gốc để controller/service kiểm tra quyền sở hữu.
     */
    public User getUser() {
        return user;
    }

    /**
     * Cập nhật User gốc khi cần thay principal hiện tại.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Chưa áp dụng kiểm tra hết hạn credential ở lớp này.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Chưa áp dụng kiểm tra trạng thái kích hoạt ở lớp này.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
