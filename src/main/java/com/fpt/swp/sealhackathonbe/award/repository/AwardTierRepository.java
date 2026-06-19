package com.fpt.swp.sealhackathonbe.award.repository;

import com.fpt.swp.sealhackathonbe.award.entity.AwardTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AwardTierRepository extends JpaRepository<AwardTier, UUID> {
}
