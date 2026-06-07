package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.user.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepo repo;


}
