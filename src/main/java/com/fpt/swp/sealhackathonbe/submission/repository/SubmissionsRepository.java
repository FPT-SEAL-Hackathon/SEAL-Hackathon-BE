package com.fpt.swp.sealhackathonbe.submission.repository;

import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionsRepository extends JpaRepository<Submissions, UUID> {
    // Dung cho API lay submission theo team-round va de reload submission sau khi upsert.
    Optional<Submissions> findByTeamIdAndRoundId(UUID teamId, UUID roundId);

    // Dung cho API lay danh sach submission theo round.
    List<Submissions> findByRoundId(UUID roundId);

    // CHUA DUNG TAM THOI:
    // Hien khong co service submission nao dang goi method nay.
    // Chi giu lai neu sau nay can check trung submission truoc khi upsert.
    boolean existsByTeamIdAndRoundId(UUID teamId, UUID roundId);

}
