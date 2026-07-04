package com.fpt.swp.sealhackathonbe.event.repository;

import com.fpt.swp.sealhackathonbe.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findAllByIsDeletedFalse();
    List<Event> findAllByIsDeletedFalseAndEventStatusEventStatusNameInOrderByEventStartDateAsc(List<String> statusNames);
    Optional<Event> findByEventIdAndIsDeletedFalse(UUID eventId);
    Optional<Event> findByEventIdAndIsDeletedFalseAndEventStatusEventStatusNameIn(UUID eventId, List<String> statusNames);
    boolean existsByEventNameIgnoreCaseAndIsDeletedFalse(String eventName);
    boolean existsByEventNameIgnoreCaseAndIsDeletedFalseAndEventIdNot(String eventName, UUID eventId);
}
