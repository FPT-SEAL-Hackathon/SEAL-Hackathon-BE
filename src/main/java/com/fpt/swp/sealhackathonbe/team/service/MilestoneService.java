package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.team.dto.CreateMilestoneRequest;
import com.fpt.swp.sealhackathonbe.team.dto.MilestoneResponse;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMilestone;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMilestoneRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final TeamMilestoneRepository milestoneRepository;
    private final com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository teamsRepository;

    // ── Read ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MilestoneResponse> getByTeam(UUID teamId) {
        return milestoneRepository
                .findByTeamIdOrderBySortOrderAscCreatedAtAsc(teamId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Create ───────────────────────────────────────────────────────────────

    @Transactional
    public MilestoneResponse create(UUID teamId, UUID mentorUserId, CreateMilestoneRequest req) {
        int nextOrder = milestoneRepository
                .findByTeamIdOrderBySortOrderAscCreatedAtAsc(teamId)
                .size();

        TeamMilestone milestone = TeamMilestone.builder()
                .teamId(teamId)
                .mentorUserId(mentorUserId)
                .label(req.getLabel().trim())
                .isDone(false)
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : nextOrder)
                .build();

        return toResponse(milestoneRepository.save(milestone));
    }

    // ── Toggle done ───────────────────────────────────────────────────────────

    @Transactional
    public MilestoneResponse toggle(UUID milestoneId, UUID currentUserId) {
        TeamMilestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new EntityNotFoundException("Milestone not found"));
                
        // Check if user is mentor who created it
        boolean isMentor = milestone.getMentorUserId().equals(currentUserId);
        
        // Or if user is team leader
        boolean isLeader = false;
        if (!isMentor) {
            com.fpt.swp.sealhackathonbe.team.entity.Teams team = teamsRepository.findById(milestone.getTeamId()).orElse(null);
            if (team != null && team.getLeaderUserId().equals(currentUserId)) {
                isLeader = true;
            }
        }
        
        if (!isMentor && !isLeader) {
            throw new AccessDeniedException("You are not authorized to toggle this milestone");
        }
        
        milestone.setIsDone(!milestone.getIsDone());
        return toResponse(milestoneRepository.save(milestone));
    }

    // ── Update label ──────────────────────────────────────────────────────────

    @Transactional
    public MilestoneResponse updateLabel(UUID milestoneId, UUID mentorUserId, String newLabel) {
        if (newLabel == null || newLabel.isBlank()) {
            throw new IllegalArgumentException("Label must not be blank");
        }
        TeamMilestone milestone = getOwned(milestoneId, mentorUserId);
        milestone.setLabel(newLabel.trim());
        return toResponse(milestoneRepository.save(milestone));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void delete(UUID milestoneId, UUID mentorUserId) {
        TeamMilestone milestone = getOwned(milestoneId, mentorUserId);
        milestoneRepository.delete(milestone);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private TeamMilestone getOwned(UUID milestoneId, UUID mentorUserId) {
        TeamMilestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new EntityNotFoundException("Milestone not found"));
        if (!milestone.getMentorUserId().equals(mentorUserId)) {
            throw new AccessDeniedException("You can only manage milestones you created");
        }
        return milestone;
    }

    private MilestoneResponse toResponse(TeamMilestone m) {
        return MilestoneResponse.builder()
                .milestoneId(m.getMilestoneId())
                .teamId(m.getTeamId())
                .mentorUserId(m.getMentorUserId())
                .label(m.getLabel())
                .isDone(m.getIsDone())
                .sortOrder(m.getSortOrder())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
