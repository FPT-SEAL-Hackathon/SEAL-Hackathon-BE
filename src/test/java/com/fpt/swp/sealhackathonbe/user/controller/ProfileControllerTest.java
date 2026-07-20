package com.fpt.swp.sealhackathonbe.user.controller;

import com.fpt.swp.sealhackathonbe.auth.service.impl.AuthenticationServiceImpl;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.user.dto.UpdateMyProfileRequest;
import com.fpt.swp.sealhackathonbe.user.entity.AccountStatus;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserType;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tự sửa hồ sơ: chỉ đổi được fullName/phone/university; email và student code
 * không nằm trong request nên bất biến; External Student không được xóa trắng
 * trường (điều kiện eligibility lập team).
 */
@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private AuthenticationServiceImpl authService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileController controller;

    private User user(String roleName) {
        UserType type = new UserType();
        type.setTypeName(roleName);
        AccountStatus status = new AccountStatus();
        status.setStatusName("Active");
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("student@test.com");
        user.setFullName("Old Name");
        user.setPhone("0900000000");
        user.setUniversityName("Old University");
        user.setUserType(type);
        user.setAccountStatus(status);
        user.setIsDeleted(false);
        return user;
    }

    private UpdateMyProfileRequest request(String fullName, String phone, String university) {
        UpdateMyProfileRequest request = new UpdateMyProfileRequest();
        request.setFullName(fullName);
        request.setPhone(phone);
        request.setUniversityName(university);
        return request;
    }

    @Test
    void updatesEditableFieldsAndPersists() {
        User user = user("FPT Student");
        when(authService.getCurrentUser()).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        controller.updateMe(request("New Name", "0912345678", null));

        assertEquals("New Name", user.getFullName());
        assertEquals("0912345678", user.getPhone());
        assertEquals("student@test.com", user.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void rejectsBlankFullName() {
        User user = user("FPT Student");
        when(authService.getCurrentUser()).thenReturn(user);

        assertThrows(BadRequestException.class,
                () -> controller.updateMe(request("   ", null, null)));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void rejectsInvalidPhone() {
        User user = user("FPT Student");
        when(authService.getCurrentUser()).thenReturn(user);

        assertThrows(BadRequestException.class,
                () -> controller.updateMe(request(null, "abc", null)));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void externalStudentCannotClearUniversity() {
        User user = user("External Student");
        when(authService.getCurrentUser()).thenReturn(user);

        assertThrows(BadRequestException.class,
                () -> controller.updateMe(request(null, null, "  ")));
        assertEquals("Old University", user.getUniversityName());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void nonStudentCanClearUniversity() {
        User user = user("Mentor");
        when(authService.getCurrentUser()).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        controller.updateMe(request(null, null, ""));

        assertNull(user.getUniversityName());
        verify(userRepository).save(user);
    }
}
