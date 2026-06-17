package com.fpt.swp.sealhackathonbe.user.entity;

import com.fpt.swp.sealhackathonbe.auth.service.impl.JWTServiceImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {
    private User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + JWTServiceImpl.normalizeRole(user.getUserType().getTypeName()))
        );
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.getAccountExpiresAt() == null
                || user.getAccountExpiresAt().isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isAccountNonLocked() {
        String status = getStatusName();
        return !"LOCKED".equals(status) && !"BANNED".equals(status);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !Boolean.TRUE.equals(user.getIsDeleted())
                && "ACTIVE".equals(getStatusName());
    }

    private String getStatusName() {
        if (user.getAccountStatus() == null || user.getAccountStatus().getStatusName() == null) {
            return "";
        }

        return user.getAccountStatus().getStatusName().trim().toUpperCase();
    }
}
