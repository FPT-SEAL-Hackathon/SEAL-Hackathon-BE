package com.fpt.swp.sealhackathonbe.integration.repository.repository;

import com.fpt.swp.sealhackathonbe.integration.repository.entity.SubmissionRepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubmissionRepositoryEntityRepository extends JpaRepository<SubmissionRepositoryEntity, UUID> {
    Optional<SubmissionRepositoryEntity> findBySubmission_SubmissionId(UUID submissionId);
}
