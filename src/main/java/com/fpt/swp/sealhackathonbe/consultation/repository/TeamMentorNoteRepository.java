package com.fpt.swp.sealhackathonbe.consultation.repository;

import com.fpt.swp.sealhackathonbe.consultation.entity.TeamMentorNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TeamMentorNoteRepository extends JpaRepository<TeamMentorNote, UUID> {
    Optional<TeamMentorNote> findByTeamIdAndMentorId(UUID teamId, UUID mentorId);
    java.util.List<TeamMentorNote> findByTeamId(UUID teamId);
}
