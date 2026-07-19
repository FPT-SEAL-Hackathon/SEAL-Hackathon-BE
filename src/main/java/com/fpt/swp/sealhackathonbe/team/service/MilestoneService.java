package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.consultation.entity.ConsultationRequest;
import com.fpt.swp.sealhackathonbe.consultation.repository.ConsultationRequestRepository;
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
    private final ConsultationRequestRepository consultationRequestRepository;
    private final com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository teamsRepository;
    private final com.fpt.swp.sealhackathonbe.consultation.service.ConsultationService consultationService;
    private final com.fpt.swp.sealhackathonbe.user.repository.UserRepository userRepository;
    private final com.fpt.swp.sealhackathonbe.notification.service.NotificationService notificationService;

    // ── Read ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MilestoneResponse> getByRequest(UUID requestId) {
        ConsultationRequest request = consultationRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));
        return milestoneRepository
                .findByTeamIdOrderBySortOrderAscCreatedAtAsc(request.getTeam().getTeamId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Create ───────────────────────────────────────────────────────────────

    @Transactional
    public MilestoneResponse create(UUID requestId, UUID mentorUserId, CreateMilestoneRequest req) {
        ConsultationRequest request = consultationRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        int nextOrder = milestoneRepository
                .findByTeamIdOrderBySortOrderAscCreatedAtAsc(request.getTeam().getTeamId())
                .size();

        TeamMilestone milestone = TeamMilestone.builder()
                .teamId(request.getTeam().getTeamId())
                .mentorUserId(mentorUserId)
                .label(req.getLabel().trim())
                .isDone(false)
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : nextOrder)
                .build();

        return toResponse(milestoneRepository.save(milestone));
    }

    // ── Toggle done ───────────────────────────────────────────────────────────

    @Transactional
    public MilestoneResponse toggle(UUID requestId, UUID milestoneId, UUID currentUserId) {
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
        
        boolean wasDone = milestone.getIsDone();
        milestone.setIsDone(!wasDone);
        milestone = milestoneRepository.save(milestone);

        // Auto send message if marked as done by leader
        if (!wasDone && isLeader) {
            com.fpt.swp.sealhackathonbe.user.entity.User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            com.fpt.swp.sealhackathonbe.consultation.dto.MessageRequest msgReq = 
                    new com.fpt.swp.sealhackathonbe.consultation.dto.MessageRequest();
            msgReq.setContent("✅ Milestone completed: " + milestone.getLabel());
            consultationService.sendMessage(user, requestId, msgReq);

            // Fetch request to get category and event
            ConsultationRequest request = consultationRequestRepository.findById(requestId).orElse(null);
            if (request != null) {
                // Get all mentors assigned to this category
                List<com.fpt.swp.sealhackathonbe.consultation.dto.MentorProfileResponse> mentors = 
                        consultationService.getMentorsOfCategory(request.getCategory().getCategoryId());
                
                String teamName = request.getTeam().getTeamName();
                String title = "Milestone Completed";
                String body = "Team " + teamName + " has completed milestone: " + milestone.getLabel();
                UUID eventId = request.getEvent().getEventId();

                for (var mentorProfile : mentors) {
                    notificationService.sendNotification(mentorProfile.getMentorId(), currentUserId, eventId, title, body);
                }
            }
        }

        return toResponse(milestone);
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
