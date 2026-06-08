package com.fpt.swp.sealhackathonbe.round.repository;


import com.fpt.swp.sealhackathonbe.round.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoundRepository extends JpaRepository<Round, UUID> {
}
