package com.fpt.swp.sealhackathonbe.user.repository;

import com.fpt.swp.sealhackathonbe.user.entity.UserOAuthAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Truy cập định danh OAuth. Khóa nghiệp vụ là (Provider, ProviderUserID).
 */
@Repository
public interface UserOAuthAccountRepository extends JpaRepository<UserOAuthAccount, UUID> {

    @EntityGraph(attributePaths = {"user", "user.userType", "user.accountStatus"})
    Optional<UserOAuthAccount> findByProviderAndProviderUserId(String provider, String providerUserId);

    List<UserOAuthAccount> findByUser_UserId(UUID userId);

    Optional<UserOAuthAccount> findByUser_UserIdAndProvider(UUID userId, String provider);

    boolean existsByUser_UserIdAndProvider(UUID userId, String provider);
}
