package com.fpt.swp.sealhackathonbe.integration.repository.repository;

import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositorySyncLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositorySyncLogRepository extends JpaRepository<RepositorySyncLog, UUID> {
    Optional<RepositorySyncLog> findFirstByRepository_RepositoryIdAndStatusOrderByStartedAtDesc(UUID repositoryId, String status);
}
