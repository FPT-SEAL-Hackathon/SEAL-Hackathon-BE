package com.fpt.swp.sealhackathonbe.round.repository;

import com.fpt.swp.sealhackathonbe.round.entity.RoundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoundStatusRepository extends JpaRepository<RoundStatus, UUID> {
}
