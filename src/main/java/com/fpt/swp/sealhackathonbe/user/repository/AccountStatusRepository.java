package com.fpt.swp.sealhackathonbe.user.repository;

import com.fpt.swp.sealhackathonbe.user.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Tra cứu trạng thái tài khoản dùng trong đăng ký và xác minh email.
 */
@Repository
public interface AccountStatusRepository extends JpaRepository<AccountStatus, UUID> {

    /**
     * Tìm trạng thái theo tên cấu hình trong database.
     */
    Optional<AccountStatus> findByStatusName(String statusName);

    Optional<AccountStatus> findByStatusNameIgnoreCase(String statusName);
}
