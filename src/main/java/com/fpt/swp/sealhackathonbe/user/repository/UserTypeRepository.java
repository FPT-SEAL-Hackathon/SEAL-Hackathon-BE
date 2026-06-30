package com.fpt.swp.sealhackathonbe.user.repository;

import com.fpt.swp.sealhackathonbe.user.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Tra cứu UserType, nguồn dữ liệu để gán role cho tài khoản.
 */
@Repository
public interface UserTypeRepository extends JpaRepository<UserType, UUID> {

    /**
     * Tìm UserType theo khóa chính.
     */
    Optional<UserType> findByUserTypeId(UUID userTypeId);
}
