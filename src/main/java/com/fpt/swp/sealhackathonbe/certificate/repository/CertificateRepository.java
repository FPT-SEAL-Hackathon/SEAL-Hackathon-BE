package com.fpt.swp.sealhackathonbe.certificate.repository;

import com.fpt.swp.sealhackathonbe.certificate.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    @Query("SELECT c FROM Certificate c WHERE c.award.id = :awardId")
    Optional<Certificate> findByAwardId(@Param("awardId") UUID awardId);
}
