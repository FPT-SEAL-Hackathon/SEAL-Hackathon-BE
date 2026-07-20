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
import com.fpt.swp.sealhackathonbe.consultation.repository.TeamMentorNoteRepository;
import com.fpt.swp.sealhackathonbe.consultation.entity.TeamMentorNote;
import com.fpt.swp.sealhackathonbe.consultation.service.ConsultationService;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRequestRepository requestRepository;
    private final ConsultationMessageRepository messageRepository;
    private final CategoryMentorRepository categoryMentorRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TeamsRepository teamsRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final RoundJudgeRepository roundJudgeRepository;
    private final TeamMentorNoteRepository teamMentorNoteRepository;
    private final NotificationService notificationService;

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
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Mentor is already assigned to this category");
                });

        // Enforce BR-19: A Mentor can be a Judge in another Category, but must not
        // judge the same Category where they are assigned as Mentor.
        List<Round> judgeRounds = roundJudgeRepository.findRoundsByJudgeJudgeId(mentorId);
        boolean isJudgeInCategory = judgeRounds.stream()
                .anyMatch(r -> r.getCategory().getCategoryId().equals(categoryId));
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
        List<ConsultationStatus> openStatuses = List.of(
                ConsultationStatus.PENDING,
                ConsultationStatus.ACCEPTED,
                ConsultationStatus.IN_PROGRESS);
        return categoryMentorRepository.findByMentor_UserId(mentor.getUserId()).stream()
                .map(cm -> {
                    UUID catId = cm.getCategory().getCategoryId();
                    long teamCount = teamsRepository.countByCategoryId(catId);
                    long openRequests = requestRepository.countByCategory_CategoryIdAndStatusIn(
                            catId, openStatuses);
                    return AssignedCategoryResponse.builder()
                            .categoryId(catId)
                            .categoryName(cm.getCategory().getCategoryName())
                            .eventId(cm.getCategory().getEvent().getEventId())
                            .eventName(cm.getCategory().getEvent().getEventName())
                            .numberOfTeams((int) teamCount)
                            .numberOfOpenRequests((int) openRequests)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TeamSummaryForMentorResponse> getTeamsForMentorCategory(User mentor, UUID categoryId) {
        // Xác nhận mentor được assign vào category này
        categoryMentorRepository.findByCategory_CategoryIdAndMentor_UserId(categoryId, mentor.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You are not assigned to this category"));

        List<ConsultationStatus> openStatuses = List.of(
                ConsultationStatus.PENDING,
                ConsultationStatus.ACCEPTED,
                ConsultationStatus.IN_PROGRESS);

        List<Teams> teams = teamsRepository.findByCategoryId(categoryId);
        return teams.stream().map(team -> {
            int memberCount = (int) teamMembersRepository.countByTeamIdAndActiveTrue(team.getTeamId());
            long openRequests = requestRepository.countByCategory_CategoryIdAndStatusIn(
                    categoryId, openStatuses);
            long totalRequests = requestRepository.countByTeam_TeamId(team.getTeamId());
            return TeamSummaryForMentorResponse.from(team, memberCount, openRequests, totalRequests);
        }).collect(Collectors.toList());
    }

    @Override
    public Page<ConsultationRequestResponse> getMentorRequests(User mentor, UUID categoryId, UUID teamId, String status,
            String priority, Pageable pageable) {
        // Basic implementation, filters can be extended with specifications
        Page<ConsultationRequest> requests;
        if (categoryId != null) {
            requests = requestRepository.findByCategory_CategoryId(categoryId, pageable);
        } else {
            List<UUID> assignedCatIds = categoryMentorRepository.findByMentor_UserId(mentor.getUserId())
                    .stream().map(cm -> cm.getCategory().getCategoryId()).collect(Collectors.toList());
            if (assignedCatIds.isEmpty()) {
                return Page.empty();
            }
            requests = requestRepository.findByCategory_CategoryIdIn(assignedCatIds, pageable);
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
        ConsultationRequestResponse response = ConsultationRequestResponse.from(requestRepository.save(req), null, 0);
        // Notify team leader
        sendNotificationSafe(
                req.getCreatedBy().getUserId(),
                mentor.getUserId(),
                req.getEvent().getEventId(),
                "Consultation Request Accepted",
                String.format("Expert %s has accepted your consultation request \u201c%s\u201d.", mentor.getFullName(),
                        req.getTitle()));
        // Notify other mentors
        for (CategoryMentor cm : categoryMentorRepository
                .findByCategory_CategoryId(req.getCategory().getCategoryId())) {
            if (!cm.getMentor().getUserId().equals(mentor.getUserId())) {
                sendNotificationSafe(
                        cm.getMentor().getUserId(),
                        mentor.getUserId(),
                        req.getEvent().getEventId(),
                        "Consultation Request Accepted",
                        String.format("Expert %s has accepted consultation request \u201c%s\u201d.",
                                mentor.getFullName(), req.getTitle()));
            }
        }
        return response;
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
        ConsultationRequestResponse response = ConsultationRequestResponse.from(requestRepository.save(req), reason, 0);
        // Notify team leader
        sendNotificationSafe(
                req.getCreatedBy().getUserId(),
                mentor.getUserId(),
                req.getEvent().getEventId(),
                "Consultation Request Rejected",
                String.format("Expert %s has rejected your consultation request \u201c%s\u201d. Reason: %s",
                        mentor.getFullName(), req.getTitle(), reason));
        // Notify other mentors
        for (CategoryMentor cm : categoryMentorRepository
                .findByCategory_CategoryId(req.getCategory().getCategoryId())) {
            if (!cm.getMentor().getUserId().equals(mentor.getUserId())) {
                sendNotificationSafe(
                        cm.getMentor().getUserId(),
                        mentor.getUserId(),
                        req.getEvent().getEventId(),
                        "Consultation Request Rejected",
                        String.format("Expert %s has rejected consultation request \u201c%s\u201d.",
                                mentor.getFullName(), req.getTitle()));
            }
        }
        return response;
    }

    @Override
    @Transactional
    public ConsultationRequestResponse markInProgress(User mentor, UUID requestId) {
        ConsultationRequest req = getMentorAssignedRequest(mentor, requestId);
        if (req.getStatus() != ConsultationStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only ACCEPTED request can be marked IN_PROGRESS");
        }
        req.setStatus(ConsultationStatus.IN_PROGRESS);
        ConsultationRequestResponse response = ConsultationRequestResponse.from(requestRepository.save(req), null, 0);
        // Notify team leader
        sendNotificationSafe(
                req.getCreatedBy().getUserId(),
                mentor.getUserId(),
                req.getEvent().getEventId(),
                "Consultation In Progress",
                String.format("Expert %s is now working on your consultation request \u201c%s\u201d.",
                        mentor.getFullName(), req.getTitle()));
        // Notify other mentors
        for (CategoryMentor cm : categoryMentorRepository
                .findByCategory_CategoryId(req.getCategory().getCategoryId())) {
            if (!cm.getMentor().getUserId().equals(mentor.getUserId())) {
                sendNotificationSafe(
                        cm.getMentor().getUserId(),
                        mentor.getUserId(),
                        req.getEvent().getEventId(),
                        "Consultation In Progress",
                        String.format("Expert %s is now working on consultation request \u201c%s\u201d.",
                                mentor.getFullName(), req.getTitle()));
            }
        }
        return response;
    }

    @Override
    @Transactional
    public ConsultationRequestResponse resolveRequest(User mentor, UUID requestId) {
        ConsultationRequest req = getMentorAssignedRequest(mentor, requestId);
        if (req.getStatus() != ConsultationStatus.ACCEPTED && req.getStatus() != ConsultationStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only ACCEPTED or IN_PROGRESS request can be resolved");
        }
        req.setStatus(ConsultationStatus.RESOLVED);
        req.setClosedAt(LocalDateTime.now());
        ConsultationRequestResponse response = ConsultationRequestResponse.from(requestRepository.save(req), null, 0);
        // Notify team leader
        sendNotificationSafe(
                req.getCreatedBy().getUserId(),
                mentor.getUserId(),
                req.getEvent().getEventId(),
                "Consultation Request Resolved",
                String.format("Expert %s has resolved your consultation request \u201c%s\u201d.", mentor.getFullName(),
                        req.getTitle()));
        // Notify other mentors
        for (CategoryMentor cm : categoryMentorRepository
                .findByCategory_CategoryId(req.getCategory().getCategoryId())) {
            if (!cm.getMentor().getUserId().equals(mentor.getUserId())) {
                sendNotificationSafe(
                        cm.getMentor().getUserId(),
                        mentor.getUserId(),
                        req.getEvent().getEventId(),
                        "Consultation Request Resolved",
                        String.format("Expert %s has resolved consultation request \u201c%s\u201d.",
                                mentor.getFullName(), req.getTitle()));
            }
        }
        return response;
    }

    @Override
    public TeamMentorNoteResponse getTeamMentorNote(User mentor, UUID requestId) {
        ConsultationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        // Verify mentor is assigned to this category
        categoryMentorRepository
                .findByCategory_CategoryIdAndMentor_UserId(req.getCategory().getCategoryId(), mentor.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You are not assigned to this request's category"));

        TeamMentorNote note = teamMentorNoteRepository.findByTeamIdAndMentorId(req.getTeam().getTeamId(), mentor.getUserId())
                .orElse(null);
        if (note == null) {
            return TeamMentorNoteResponse.builder()
                    .requestId(requestId)
                    .mentorId(mentor.getUserId())
                    .note("")
                    .build();
        }

        return TeamMentorNoteResponse.builder()
                .requestId(requestId)
                .mentorId(note.getMentorId())
                .note(note.getNote())
                .updatedAt(note.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public TeamMentorNoteResponse updateTeamMentorNote(User mentor, UUID requestId, TeamMentorNoteRequest request) {
        ConsultationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        categoryMentorRepository
                .findByCategory_CategoryIdAndMentor_UserId(req.getCategory().getCategoryId(), mentor.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You are not assigned to this request's category"));

        TeamMentorNote note = teamMentorNoteRepository.findByTeamIdAndMentorId(req.getTeam().getTeamId(), mentor.getUserId())
                .orElse(null);

        if (note == null) {
            note = TeamMentorNote.builder()
                    .team(req.getTeam())
                    .teamId(req.getTeam().getTeamId())
                    .mentor(mentor)
                    .mentorId(mentor.getUserId())
                    .note(request.getNote())
                    .build();
        } else {
            note.setNote(request.getNote());
        }

        note = teamMentorNoteRepository.save(note);

        // Notify team leader about updated note
        UUID eventId = req.getEvent() != null ? req.getEvent().getEventId() : null;
        sendNotificationSafe(
                req.getTeam().getLeaderUserId(),
                mentor.getUserId(),
                eventId,
                "Expert Note Updated",
                String.format("Expert %s has updated a note for your request \u201c%s\u201d.", mentor.getFullName(),
                        req.getTitle()));

        return TeamMentorNoteResponse.builder()
                .requestId(requestId)
                .mentorId(note.getMentorId())
                .note(note.getNote())
                .updatedAt(note.getUpdatedAt())
                .build();
    }

    @Override
    public java.util.List<TeamMentorNoteResponse> getMyTeamMentorNotes(User user, UUID requestId) {
        ConsultationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Request not found"));

        Teams team = req.getTeam();
        boolean isMember = team.getLeaderUserId().equals(user.getUserId()) ||
                teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(team.getTeamId(), user.getUserId())
                        .isPresent();
        if (!isMember) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You are not a member of this team");
        }

        return teamMentorNoteRepository.findByTeamId(team.getTeamId()).stream()
                .map(note -> TeamMentorNoteResponse.builder()
                        .requestId(requestId)
                        .mentorId(note.getMentorId())
                        .note(note.getNote())
                        .updatedAt(note.getUpdatedAt())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    private ConsultationRequest getMentorAssignedRequest(User mentor, UUID requestId) {
        ConsultationRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        categoryMentorRepository
                .findByCategory_CategoryIdAndMentor_UserId(req.getCategory().getCategoryId(), mentor.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You are not assigned to this request's category"));

        return req;
    }

    @Override
    public List<MentorProfileResponse> getMyMentors(User user) {
        Teams team = getActiveTeamForUser(user);
        List<CategoryMentor> mentors = categoryMentorRepository
                .findByCategory_CategoryId(team.getCategory().getCategoryId());

        return mentors.stream()
                .map(MentorProfileResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConsultationRequestResponse createConsultationRequest(User user,
            CreateConsultationRequestRequest requestDto) {
        Teams team = getActiveTeamForUser(user);
        if (!team.getLeaderUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only team leader can create consultation requests");
        }
        if (!com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants.ACTIVE.equals(team.getTeamStatusId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only approved teams can create consultation requests");
        }

        List<CategoryMentor> mentors = categoryMentorRepository
                .findByCategory_CategoryId(team.getCategory().getCategoryId());
        if (mentors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No mentor is assigned to this category");
        }

        ConsultationRequest request = ConsultationRequest.builder()
                .event(team.getEvent())
                .category(team.getCategory())
                .team(team)
                .createdBy(user)
                .mentor(mentors.get(0).getMentor()) // Satisfy MentorUserID NOT NULL in DB
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

        // Notify all mentors in the category about new consultation request
        for (CategoryMentor cm : mentors) {
            sendNotificationSafe(
                    cm.getMentor().getUserId(),
                    user.getUserId(),
                    team.getEvent().getEventId(),
                    "New Consultation Request",
                    String.format("Team \u201c%s\u201d has sent a new consultation request: \u201c%s\u201d.",
                            team.getTeamName(), requestDto.getTitle()));
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only team leader can cancel consultation requests");
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

        if (req.getStatus() == ConsultationStatus.RESOLVED || req.getStatus() == ConsultationStatus.REJECTED
                || req.getStatus() == ConsultationStatus.CANCELLED) {
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

        // Cross-notify: mentor messages notify team leader and other mentors; team
        // messages notify all mentors
        boolean senderIsMentor = isMentorRole(user);

        String preview = messageDto.getContent() != null && messageDto.getContent().length() > 80
                ? messageDto.getContent().substring(0, 80) + "..."
                : messageDto.getContent();

        List<CategoryMentor> categoryMentors = categoryMentorRepository
                .findByCategory_CategoryId(req.getCategory().getCategoryId());

        if (senderIsMentor) {
            // Notify Team Leader
            sendNotificationSafe(
                    req.getCreatedBy().getUserId(),
                    user.getUserId(),
                    req.getEvent().getEventId(),
                    "New Message in Consultation",
                    String.format("%s sent a message in \u201c%s\u201d: %s", user.getFullName(), req.getTitle(),
                            preview));
            // Notify other mentors in the category
            for (CategoryMentor cm : categoryMentors) {
                if (!cm.getMentor().getUserId().equals(user.getUserId())) {
                    sendNotificationSafe(
                            cm.getMentor().getUserId(),
                            user.getUserId(),
                            req.getEvent().getEventId(),
                            "New Message in Consultation",
                            String.format("Expert %s sent a message in \u201c%s\u201d: %s", user.getFullName(),
                                    req.getTitle(), preview));
                }
            }
        } else {
            // Notify all mentors
            for (CategoryMentor cm : categoryMentors) {
                sendNotificationSafe(
                        cm.getMentor().getUserId(),
                        user.getUserId(),
                        req.getEvent().getEventId(),
                        "New Message in Consultation",
                        String.format("Team \u201c%s\u201d sent a message in \u201c%s\u201d: %s",
                                req.getTeam().getTeamName(), req.getTitle(), preview));
            }
        }

        return ConsultationMessageResponse.from(msg);
    }

    private void checkRequestAccess(User user, ConsultationRequest req) {
        String role = getRoleName(user);
        if ("ORGANIZER".equalsIgnoreCase(role)) {
            return; // Admins can view
        } else if (isMentorRole(user)) {
            boolean assigned = categoryMentorRepository
                    .findByCategory_CategoryIdAndMentor_UserId(req.getCategory().getCategoryId(), user.getUserId())
                    .isPresent();
            if (!assigned) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You are not assigned to this request's category");
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
        // First try to find a team where the user is leader AND the team is active
        List<Teams> ledTeams = teamsRepository.findByLeaderUserId(user.getUserId());
        for (Teams team : ledTeams) {
            if (com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants.ACTIVE.equals(team.getTeamStatusId())) {
                return team;
            }
        }
        
        // If no active team is led, fall back to any led team (which will likely fail later validation)
        if (!ledTeams.isEmpty()) {
            return ledTeams.get(0);
        }

        // Find team via TeamMembers (as a member)
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
        String roleName = getRoleName(user);
        return "Mentor".equalsIgnoreCase(roleName) || "Expert".equalsIgnoreCase(roleName);
    }

    /**
     * Send a notification without throwing an exception on failure,
     * so that the main business operation is never blocked by a notification error.
     */
    private void sendNotificationSafe(UUID recipientId, UUID senderId, UUID eventId, String title, String body) {
        try {
            notificationService.sendNotification(recipientId, senderId, eventId, title, body);
        } catch (Exception e) {
            log.warn("Failed to send consultation notification to user {}: {}", recipientId, e.getMessage());
        }
    }
}
