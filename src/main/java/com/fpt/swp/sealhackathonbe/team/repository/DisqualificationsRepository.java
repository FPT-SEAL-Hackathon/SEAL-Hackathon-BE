package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DisqualificationsRepository extends JpaRepository<Disqualifications, UUID> {
    List<Disqualifications> findByTeamId(UUID teamId);
}
