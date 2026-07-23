package com.fpt.swp.sealhackathonbe.integration.repository.repository;

import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositoryEntityRepository extends JpaRepository<RepositoryEntity, UUID> {
    Optional<RepositoryEntity> findByIntegration_IntegrationIdAndExternalId(UUID integrationId, String externalId);
    List<RepositoryEntity> findAllByIntegration_Event_EventId(UUID eventId);
}
