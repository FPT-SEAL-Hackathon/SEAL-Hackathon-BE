package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.auth.service.JWTService;
import com.fpt.swp.sealhackathonbe.auth.dto.LoginRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.LoginResponse;
import com.fpt.swp.sealhackathonbe.auth.dto.RegisterRequest;
import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private JWTService jwtService;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    private UserRepository userRepo;


    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

//    public User register(User user) {
//        user.setPasswordHash(encoder.encode(user.getPasswordHash()));
//        userRepo.save(user);
//        return user;
//    }

    public LoginResponse verify(LoginRequest request) {
        Authentication authentication =
                authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );
        if (authentication.isAuthenticated()) {
            String token =
                    jwtService.generateToken(
                            request.getEmail()
                    );
            return new LoginResponse(token);
        }
        throw new RuntimeException(
                "Invalid email or password"
        );
    }

    public UserResponse register(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPasswordHash(
                encoder.encode(request.getPassword())
        );
        User savedUser = userRepo.save(user);
        return new UserResponse(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getFullName()
        );
    }


}
