package com.fpt.swp.sealhackathonbe.eventparticipant.repository;

import com.fpt.swp.sealhackathonbe.eventparticipant.entity.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipantStatusRepository extends JpaRepository<ParticipantStatus, UUID> {
    Optional<ParticipantStatus> findByStatusNameIgnoreCase(String statusName);
}
