package com.fpt.swp.sealhackathonbe.user.controller;

import com.fpt.swp.sealhackathonbe.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    private UserService authService;

}

