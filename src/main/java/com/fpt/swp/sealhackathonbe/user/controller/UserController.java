package com.fpt.swp.sealhackathonbe.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class UserController {
    @GetMapping("/")
    public String login(){
        return "index";
    }
    @PostMapping("/login")
    public String home(){
        return "home";
    }


    
}

