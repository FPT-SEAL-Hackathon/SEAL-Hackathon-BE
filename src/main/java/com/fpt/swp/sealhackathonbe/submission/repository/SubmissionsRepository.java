package com.fpt.swp.sealhackathonbe.submission.repository;

import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionsRepository extends JpaRepository<Submissions, UUID> {
    // Dung cho API lay submission theo team-round va de reload submission sau khi upsert.
    Optional<Submissions> findByTeamIdAndRoundId(UUID teamId, UUID roundId);

    // Dung cho API lay danh sach submission theo round.
    List<Submissions> findByRoundId(UUID roundId);

    // Dung cho API lay cac submission trong round chua duoc review/cham diem.
    List<Submissions> findByRoundIdAndSubmissionStatusIdNotIn(UUID roundId, List<UUID> statusIds);

    // Dung khi team bi loai: tat ca submission hien co cua team cung chuyen sang Disqualified.
    List<Submissions> findByTeamId(UUID teamId);

    // Submission khong chua EventID truc tiep, nen loc event thong qua team cua submission.
    @Query("SELECT s FROM Submissions s WHERE s.team.eventId = :eventId")
    List<Submissions> findByEventId(@Param("eventId") UUID eventId);

    // Organizer repository overview: lay CA bai that VA sample submission cua event.
    // Sample co TeamID = null nen khong the loc qua team; phai loc qua round -> category -> event.
    // Submissions khong co quan he 'round' mapped nen cross-join voi Round entity theo roundId.
    @Query("SELECT s FROM Submissions s, Round r WHERE r.roundId = s.roundId AND r.category.event.eventId = :eventId")
    List<Submissions> findByEventIdIncludingSamples(@Param("eventId") UUID eventId);

    // CHUA DUNG TAM THOI:
    // Hien khong co service submission nao dang goi method nay.
    // Chi giu lai neu sau nay can check trung submission truoc khi upsert.
    boolean existsByTeamIdAndRoundId(UUID teamId, UUID roundId);

    boolean existsByRoundId(UUID roundId);

    // Hard delete user: submission thuoc ve team nen duoc GIU LAI,
    // chi gan lai nguoi nop sang leader hien tai cua team.
    List<Submissions> findBySubmittedByUserId(UUID submittedByUserId);
}
