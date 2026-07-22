package com.fpt.swp.sealhackathonbe.integration.repository.repository;

import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositoryIntegrationRepository extends JpaRepository<RepositoryIntegration, UUID> {
    List<RepositoryIntegration> findAllByEvent_EventId(UUID eventId);
    Optional<RepositoryIntegration> findByEvent_EventIdAndProviderAndConnectionStatus(UUID eventId, String provider, String connectionStatus);
}
