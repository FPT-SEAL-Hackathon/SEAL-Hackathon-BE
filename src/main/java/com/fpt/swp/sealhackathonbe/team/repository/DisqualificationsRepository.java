package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisqualificationsRepository extends JpaRepository<Disqualifications, Integer> {
    List<Disqualifications> findByTeamId(Integer teamId);
}
