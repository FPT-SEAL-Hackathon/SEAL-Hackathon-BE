package com.fpt.swp.sealhackathonbe.consultation.service.impl;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.entity.CategoryMentor;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryMentorRepository;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.consultation.dto.*;
import com.fpt.swp.sealhackathonbe.consultation.entity.ConsultationMessage;
import com.fpt.swp.sealhackathonbe.consultation.entity.ConsultationRequest;
import com.fpt.swp.sealhackathonbe.consultation.entity.ConsultationStatus;
import com.fpt.swp.sealhackathonbe.consultation.repository.ConsultationMessageRepository;
import com.fpt.swp.sealhackathonbe.consultation.repository.ConsultationRequestRepository;
import com.fpt.swp.sealhackathonbe.consultation.service.ConsultationService;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRequestRepository requestRepository;
    private final ConsultationMessageRepository messageRepository;
    private final CategoryMentorRepository categoryMentorRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TeamsRepository teamsRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final RoundJudgeRepository roundJudgeRepository;

    @Override
    @Transactional
    public void assignMentorToCategory(UUID categoryId, UUID mentorId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        User mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mentor not found"));

        if (!isMentorRole(mentor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a mentor");
        }

        categoryMentorRepository.findByCategory_CategoryIdAndMentor_UserId(categoryId, mentorId)
                .ifPresent(cm -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mentor is already assigned to this category");
                });

        // Enforce BR-19: A Mentor can be a Judge in another Category, but must not judge the same Category where they are assigned as Mentor.
        List<Round> judgeRounds = roundJudgeRepository.findRoundsByJudgeJudgeId(mentorId);
        boolean isJudgeInCategory = judgeRounds.stream().anyMatch(r -> r.getCategory().getCategoryId().equals(categoryId));
        if (isJudgeInCategory) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already a judge in this category");
        }

        CategoryMentor cm = CategoryMentor.builder()
                .category(category)
                .mentor(mentor)
                .assignedAt(LocalDateTime.now())
                .build();
        categoryMentorRepository.save(cm);
    }

    @Override
    @Transactional
    public void removeMentorFromCategory(UUID categoryId, UUID mentorId) {
        CategoryMentor cm = categoryMentorRepository.findByCategory_CategoryIdAndMentor_UserId(categoryId, mentorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
        categoryMentorRepository.delete(cm);
    }

    @Override
    public List<MentorProfileResponse> getMentorsOfCategory(UUID categoryId) {
        return categoryMentorRepository.findByCategory_CategoryId(categoryId).stream()
                .map(MentorProfileResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignedCategoryResponse> getAssignedCategoriesForMentor(User mentor) {
        return categoryMentorRepository.findByMentor_UserId(mentor.getUserId()).stream()
                .map(cm -> AssignedCategoryResponse.builder()
                        .categoryId(cm.getCategory().getCategoryId())
                        .categoryName(cm.getCategory().getCategoryName())
                        .eventId(cm.getCategory().getEvent().getEventId())
                        .eventName(cm.getCategory().getEvent().getEventName())
                        .numberOfTeams(0) // Could optimize with a specific count query later
                        .numberOfOpenRequests(0)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Page<ConsultationRequestResponse> getMentorRequests(User mentor, UUID categoryId, UUID teamId, String status, String priority, Pageable pageable) {
        // Basic implementation, filters can be extended with specifications
        Page<ConsultationRequest> requests;
        if (categoryId != null) {
            requests = requestRepository.findByMentor_UserIdAndCategory_CategoryId(mentor.getUserId(), categoryId, pageable);
        } else {
            requests = requestRepository.findByMentor_UserId(mentor.getUserId(), pageable);
        }
        
        return requests.map(req -> ConsultationRequestResponse.from(req, "...", 0));
    }

    @Override
    @Transactional
    public ConsultationRequestResponse acceptRequest(User mentor, UUID requestId) {
        ConsultationRequest req = getMentorAssignedRequest(mentor, requestId);
        if (req.getStatus() != ConsultationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PENDING request can be accepted");
        }
        req.setStatus(ConsultationStatus.ACCEPTED);
        return ConsultationRequestResponse.from(requestRepository.save(req), null, 0);
    }

    @Override
    @Transactional
    public ConsultationRequestResponse rejectRequest(User mentor, UUID requestId, String reason) {
        ConsultationRequest req = getMentorAssignedRequest(mentor, requestId);
        if (req.getStatus() != ConsultationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PENDING request can be rejected");
        }
        req.setStatus(ConsultationStatus.REJECTED);
        req.setClosedAt(LocalDateTime.now());
        // Can store reason in a message or note
        ConsultationMessage msg = ConsultationMessage.builder()
                .request(req).sender(mentor).content("REJECTED REASON: " + reason).build();
        messageRepository.save(msg);
        return ConsultationRequestResponse.from(requestRepository.save(req), reason, 0);
    }

    @Override
    @Transactional
    public ConsultationRequestResponse markInProgress(User mentor, UUID requestId) {
        ConsultationRequest req = getMentorAssignedRequest(mentor, requestId);
        if (req.getStatus() != ConsultationStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only ACCEPTED request can be marked IN_PROGRESS");
        }
        req.setStatus(ConsultationStatus.IN_PROGRESS);
        return ConsultationRequestResponse.from(requestRepository.save(req), null, 0);
    }

    @Override
    @Transactional
    public ConsultationRequestResponse resolveRequest(User mentor, UUID requestId) {
        ConsultationRequest req = getMentorAssignedRequest(mentor, requestId);
        if (req.getStatus() != ConsultationStatus.ACCEPTED && req.getStatus() != ConsultationStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only ACCEPTED or IN_PROGRESS request can be resolved");
        }
        req.setStatus(ConsultationStatus.RESOLVED);
        req.setClosedAt(LocalDateTime.now());
        return ConsultationRequestResponse.from(requestRepository.save(req), null, 0);
    }

    private ConsultationRequest getMentorAssignedRequest(User mentor, UUID requestId) {
        ConsultationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        if (!req.getMentor().getUserId().equals(mentor.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not assigned to this request");
        }
        return req;
    }

    @Override
    public List<MentorProfileResponse> getMyMentors(User user) {
        Teams team = getActiveTeamForUser(user);
        List<CategoryMentor> mentors = categoryMentorRepository.findByCategory_CategoryId(team.getCategory().getCategoryId());
        
        return mentors.stream()
                .map(MentorProfileResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConsultationRequestResponse createConsultationRequest(User user, CreateConsultationRequestRequest requestDto) {
        Teams team = getActiveTeamForUser(user);
        if (!team.getLeaderUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only team leader can create consultation requests");
        }
        if (!"Active".equalsIgnoreCase(team.getTeamStatus().getStatusName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only approved teams can create consultation requests");
        }
        
        List<CategoryMentor> mentors = categoryMentorRepository.findByCategory_CategoryId(team.getCategory().getCategoryId());
        if (mentors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No mentor is assigned to this category");
        }
        User mentor = null;
        if (requestDto.getMentorId() != null) {
            mentor = mentors.stream()
                    .map(CategoryMentor::getMentor)
                    .filter(m -> m.getUserId().equals(requestDto.getMentorId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid mentor ID for this category"));
        } else {
            mentor = mentors.get(0).getMentor();
        }

        ConsultationRequest request = ConsultationRequest.builder()
                .event(team.getEvent())
                .category(team.getCategory())
                .team(team)
                .mentor(mentor)
                .createdBy(user)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .priority(requestDto.getPriority())
                .status(ConsultationStatus.PENDING)
                .build();
        
        requestRepository.save(request);
        
        if (requestDto.getAttachmentUrl() != null) {
            ConsultationMessage msg = ConsultationMessage.builder()
                    .request(request)
                    .sender(user)
                    .content("Initial Request")
                    .attachmentUrl(requestDto.getAttachmentUrl())
                    .build();
            messageRepository.save(msg);
        }

        return ConsultationRequestResponse.from(request, null, 0);
    }

    @Override
    public Page<ConsultationRequestResponse> getMyTeamRequests(User user, String status, Pageable pageable) {
        Teams team = getActiveTeamForUser(user);
        Page<ConsultationRequest> requests = requestRepository.findByTeam_TeamId(team.getTeamId(), pageable);
        return requests.map(req -> ConsultationRequestResponse.from(req, "...", 0));
    }

    @Override
    @Transactional
    public ConsultationRequestResponse cancelRequest(User user, UUID requestId) {
        Teams team = getActiveTeamForUser(user);
        if (!team.getLeaderUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only team leader can cancel consultation requests");
        }
        ConsultationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        if (!req.getTeam().getTeamId().equals(team.getTeamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this request");
        }
        if (req.getStatus() != ConsultationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PENDING request can be cancelled");
        }
        req.setStatus(ConsultationStatus.CANCELLED);
        req.setClosedAt(LocalDateTime.now());
        return ConsultationRequestResponse.from(requestRepository.save(req), null, 0);
    }

    @Override
    public ConsultationRequestResponse getConsultationRequestDetail(User user, UUID requestId) {
        ConsultationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        checkRequestAccess(user, req);
        return ConsultationRequestResponse.from(req, null, 0);
    }

    @Override
    public List<ConsultationMessageResponse> getConsultationMessages(User user, UUID requestId) {
        ConsultationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        checkRequestAccess(user, req);
        return messageRepository.findByRequest_RequestIdOrderByCreatedAtAsc(requestId).stream()
                .map(ConsultationMessageResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConsultationMessageResponse sendMessage(User user, UUID requestId, MessageRequest messageDto) {
        ConsultationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        checkRequestAccess(user, req);

        if (req.getStatus() == ConsultationStatus.RESOLVED || req.getStatus() == ConsultationStatus.REJECTED || req.getStatus() == ConsultationStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot send message in closed request");
        }

        ConsultationMessage msg = ConsultationMessage.builder()
                .request(req)
                .sender(user)
                .content(messageDto.getContent())
                .attachmentUrl(messageDto.getAttachmentUrl())
                .build();
        messageRepository.save(msg);
        
        req.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(req);

        return ConsultationMessageResponse.from(msg);
    }

    private void checkRequestAccess(User user, ConsultationRequest req) {
        String role = getRoleName(user);
        if ("ORGANIZER".equalsIgnoreCase(role)) {
            return; // Admins can view
        } else if (isMentorRole(user)) {
            if (!req.getMentor().getUserId().equals(user.getUserId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not assigned to this request");
            }
        } else {
            // Check if user is in the team
            Teams team = getActiveTeamForUser(user);
            if (!req.getTeam().getTeamId().equals(team.getTeamId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this request");
            }
        }
    }

    private Teams getActiveTeamForUser(User user) {
        // Simple logic for hackathon context, usually user belongs to one active team
        // In reality, this requires a query to get the active team of the user
        // We will fetch the first team they are a member of or leader of
        List<Teams> ledTeams = teamsRepository.findByLeaderUserId(user.getUserId());
        if (!ledTeams.isEmpty()) {
            return ledTeams.get(0);
        }
        
        // Find team via TeamMembers
        var teamMemberOpt = teamMembersRepository.findByUserIdAndActiveTrue(user.getUserId());
        if (teamMemberOpt.isPresent()) {
            return teamMemberOpt.get().getTeam();
        }
        
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not belong to any active team");
    }

    private String getRoleName(User user) {
        return user.getUserType() != null ? user.getUserType().getTypeName() : null;
    }

    private boolean isMentorRole(User user) {
        return "Internal Judge".equalsIgnoreCase(getRoleName(user));
    }
}
