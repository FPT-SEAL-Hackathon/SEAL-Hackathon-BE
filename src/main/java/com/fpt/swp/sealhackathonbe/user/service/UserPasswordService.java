package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.user.dto.SetPasswordRequest;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Đặt mật khẩu local cho user hiện tại.
 * - OAuth-only (localLoginEnabled=false): đặt lần đầu, không cần mật khẩu cũ,
 *   sau đó bật localLoginEnabled.
 * - Đã có mật khẩu local: bắt buộc xác minh currentPassword.
 */
@Service
public class UserPasswordService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserPasswordService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void setPassword(User currentUser, SetPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and Confirm Password do not match");
        }

        if (Boolean.TRUE.equals(currentUser.getLocalLoginEnabled())) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new BadRequestException("Current password is required.");
            }
            if (!encoder.matches(request.getCurrentPassword(), currentUser.getPasswordHash())) {
                throw new BadCredentialsException("Current password is incorrect.");
            }
        }

        currentUser.setPasswordHash(encoder.encode(request.getNewPassword()));
        currentUser.setLocalLoginEnabled(true);
        userRepository.save(currentUser);
    }
}
