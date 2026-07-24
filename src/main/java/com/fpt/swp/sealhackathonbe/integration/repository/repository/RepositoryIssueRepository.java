package com.fpt.swp.sealhackathonbe.integration.repository.repository;

import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositoryIssueRepository extends JpaRepository<RepositoryIssue, UUID> {
    Optional<RepositoryIssue> findByExternalId(String externalId);
    Optional<RepositoryIssue> findByRepository_RepositoryIdAndExternalId(UUID repositoryId, String externalId);
    Page<RepositoryIssue> findAllByRepository_RepositoryIdOrderByExternalUpdatedAtDesc(UUID repositoryId, Pageable pageable);
}
