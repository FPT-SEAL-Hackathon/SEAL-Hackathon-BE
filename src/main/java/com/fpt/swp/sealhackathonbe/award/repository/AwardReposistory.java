package com.fpt.swp.sealhackathonbe.award.repository;

import com.fpt.swp.sealhackathonbe.award.entity.Award;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AwardReposistory extends JpaRepository<Award, Long> {
}
