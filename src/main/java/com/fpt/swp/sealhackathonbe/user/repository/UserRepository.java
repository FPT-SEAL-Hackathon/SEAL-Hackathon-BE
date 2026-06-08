package com.fpt.swp.sealhackathonbe.user.repository;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    User findByEmail(String email);
}
