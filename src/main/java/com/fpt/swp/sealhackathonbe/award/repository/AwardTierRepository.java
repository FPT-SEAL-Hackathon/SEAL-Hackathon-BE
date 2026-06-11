package com.fpt.swp.sealhackathonbe.award.repository;

import com.fpt.swp.sealhackathonbe.award.entity.AwardTier;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface AwardTierRepository extends JpaRepository<AwardTier, Long> {
    Optional<AwardTier> findById(UUID uuid);
}
