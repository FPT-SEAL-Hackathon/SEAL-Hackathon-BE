package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.auth.dto.LoginRequest;
import com.fpt.swp.sealhackathonbe.core.exception.AccountRemovedException;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.DeletedUserTombstoneRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Login với email đã bị xóa cứng (còn tombstone): phải báo "tạo tài khoản mới"
 * thay vì "sai mật khẩu"; tombstone KHÔNG chặn khi email đã có tài khoản mới.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTombstoneLoginTest {

    private static final String EMAIL = "removed@test.com";

    @Mock
    private UserRepository userRepo;

    @Mock
    private DeletedUserTombstoneRepository tombstoneRepository;

    @Mock
    private AuthenticationManager authManager;

    @InjectMocks
    private UserService userService;

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword("secret-password");
        return request;
    }

    @Test
    void rejectsLoginWithAccountRemovedWhenTombstoneExistsAndNoAccountLeft() {
        when(userRepo.findByEmailAndIsDeletedFalse(EMAIL)).thenReturn(List.of());
        when(tombstoneRepository.existsByEmailIgnoreCaseAndExpiresAtAfter(
                anyString(), any(LocalDateTime.class))).thenReturn(true);

        assertThrows(AccountRemovedException.class, () -> userService.verify(loginRequest()));
        verify(authManager, never()).authenticate(any());
    }

    @Test
    void skipsTombstoneCheckWhenEmailAlreadyHasNewAccount() {
        when(userRepo.findByEmailAndIsDeletedFalse(EMAIL)).thenReturn(List.of(new User()));
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> userService.verify(loginRequest()));
        verify(tombstoneRepository, never())
                .existsByEmailIgnoreCaseAndExpiresAtAfter(anyString(), any(LocalDateTime.class));
    }

    @Test
    void fallsThroughToNormalAuthenticationWhenNoTombstone() {
        when(userRepo.findByEmailAndIsDeletedFalse(EMAIL)).thenReturn(List.of());
        when(tombstoneRepository.existsByEmailIgnoreCaseAndExpiresAtAfter(
                anyString(), any(LocalDateTime.class))).thenReturn(false);
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> userService.verify(loginRequest()));
    }
}
