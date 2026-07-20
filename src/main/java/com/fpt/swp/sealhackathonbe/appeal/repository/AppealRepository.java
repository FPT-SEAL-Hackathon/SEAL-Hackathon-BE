package com.fpt.swp.sealhackathonbe.appeal.repository;

import com.fpt.swp.sealhackathonbe.appeal.entity.Appeals;
import com.fpt.swp.sealhackathonbe.appeal.entity.AppealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppealRepository extends JpaRepository<Appeals, UUID> {
    List<Appeals> findByEvent_EventIdOrderByCreatedAtDesc(UUID eventId);
    List<Appeals> findByTeam_TeamIdOrderByCreatedAtDesc(UUID teamId);
    boolean existsByTeam_TeamIdAndStatus(UUID teamId, AppealStatus status);
}
