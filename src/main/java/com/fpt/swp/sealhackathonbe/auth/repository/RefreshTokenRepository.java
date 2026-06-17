package com.fpt.swp.sealhackathonbe.auth.repository;

import com.fpt.swp.sealhackathonbe.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUser_UserId(UUID userId);

    void deleteByTokenHash(String tokenHash);
}