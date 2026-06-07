package com.fpt.swp.sealhackathonbe.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @GetMapping("/register")
    public String regis(){
        return "regis-form";
    }
//    @PostMapping("/register")
//    public Users register(@RequestBody Users user) {
//        return service.register(user);
//
//    }
//
//    @PostMapping("/login")
//    public String login(@RequestBody Users user) {
//
//        return service.verify(user);
//    }
    
}

