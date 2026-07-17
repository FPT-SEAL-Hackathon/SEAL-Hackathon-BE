package com.fpt.swp.sealhackathonbe.auth.repository;

import com.fpt.swp.sealhackathonbe.auth.entity.AccountLinkTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountLinkTicketRepository extends JpaRepository<AccountLinkTicket, UUID> {

    Optional<AccountLinkTicket> findByTokenHash(String tokenHash);

    List<AccountLinkTicket> findByUser_UserIdAndPurposeAndConsumedAtIsNull(UUID userId, String purpose);

    /**
     * Hard delete user: xóa toàn bộ ticket liên kết tài khoản của user.
     */
    long deleteByUser_UserId(UUID userId);
}
