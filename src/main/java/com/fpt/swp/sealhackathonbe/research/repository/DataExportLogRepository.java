package com.fpt.swp.sealhackathonbe.research.repository;

import com.fpt.swp.sealhackathonbe.research.entity.DataExportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DataExportLogRepository extends JpaRepository<DataExportLog, UUID> {
    List<DataExportLog> findByEvent_EventIdOrderByExportedAtDesc(UUID eventId);
}
