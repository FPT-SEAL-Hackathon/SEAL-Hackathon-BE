package com.fpt.swp.sealhackathonbe.user.repository;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Truy cập tài khoản phục vụ xác thực và quản lý người dùng.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * RBAC:
     * Nạp kèm role/status để quyết định xác thực và phân quyền.
     */
    @EntityGraph(attributePaths = {"userType", "accountStatus"})
    User findByEmail(String email);

    /**
     * Kiểm tra email đã được đăng ký để tránh trùng tài khoản.
     */
    boolean existsByEmail(String email);
}
