package com.fpt.swp.sealhackathonbe.event.repository;

import com.fpt.swp.sealhackathonbe.event.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventStatusRepository extends JpaRepository<EventStatus, UUID> {
}
