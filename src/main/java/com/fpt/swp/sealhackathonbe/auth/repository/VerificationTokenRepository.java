package com.fpt.swp.sealhackathonbe.auth.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID>{
    Optional<VerificationToken> findByTokenHash(String tokenHash);

}