package com.fpt.swp.sealhackathonbe.team.service.impl;

import com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants;

import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.auth.entity.AuditLog;
import com.fpt.swp.sealhackathonbe.auth.repository.AuditLogRepository;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.eventparticipant.entity.EventParticipant;
import com.fpt.swp.sealhackathonbe.eventparticipant.repository.EventParticipantRepository;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import com.fpt.swp.sealhackathonbe.team.dto.CreateTeamRequest;
import com.fpt.swp.sealhackathonbe.team.dto.TeamEligibilityMemberResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamEligibilityReviewResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamMemberDetailResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamResponse;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.event.TeamRegistrationRejectedEvent;
import com.fpt.swp.sealhackathonbe.team.repository.TeamJoinRequestsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.service.TeamEventRegistrationService;
import com.fpt.swp.sealhackathonbe.team.service.TeamService;
import com.fpt.swp.sealhackathonbe.team.service.mapper.TeamMapper;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class TeamServiceImpl implements TeamService {
    private static final int TEAM_NAME_MAX_LENGTH = 300;
    private static final String REJECTED_TEAM_NAME_SUFFIX_PREFIX = " [rejected:";

    private static final UUID TEAM_STATUS_FORMING = TeamStatusConstants.FORMING;
    private static final UUID TEAM_STATUS_PENDING = TeamStatusConstants.PENDING;
    private static final UUID TEAM_STATUS_ACTIVE = TeamStatusConstants.ACTIVE;
    private static final UUID TEAM_STATUS_DISQUALIFIED = TeamStatusConstants.DISQUALIFIED;
    private static final UUID TEAM_STATUS_WITHDRAWN = TeamStatusConstants.WITHDRAWN;
    private static final UUID TEAM_STATUS_REJECTED = TeamStatusConstants.REJECTED;

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final TeamsRepository teamsRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final TeamJoinRequestsRepository teamJoinRequestsRepository;
    private final AuditLogRepository auditLogRepository;
    private final TeamEventRegistrationService teamEventRegistrationService;
    private final TeamJoinRequestCleaner teamJoinRequestCleaner;
    private final EventParticipantRepository eventParticipantRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request, UUID currentUserId) {
        // Luồng tạo team: client gửi event/category/name -> kiểm tra event còn hoạt
        // động
        // và cấu hình size -> kiểm tra trùng tên/team active -> lưu Teams -> lưu leader
        // vào TeamMembers -> map ra DTO.
        Event event = getActiveEvent(request.getEventId());
        teamEventRegistrationService.assertEventOpenForRegistration(request.getEventId());
        // Team-first: tạo team không cần là EventParticipant — chỉ cần student
        // ACTIVE với hồ sơ đầy đủ; đăng ký event là bước sau do leader thực hiện.
        teamEventRegistrationService.assertEligibleStudent(currentUserId);
        validateTeamSizeConfig(event);
        validateCategoryBelongsToEvent(request.getCategoryId(), request.getEventId());

        LocalDateTime now = LocalDateTime.now();
        releaseRejectedOrInactiveDuplicateTeamName(request.getEventId(), request.getTeamName(), now);

        if (teamMembersRepository.existsByUserIdAndTeam_EventIdAndActiveTrue(currentUserId, event.getEventId())) {
            throw new BusinessConflictException("User already belongs to an active team in this event");
        }

        Teams team = new Teams();
        team.setEventId(request.getEventId());
        team.setCategoryId(request.getCategoryId());
        team.setTeamName(request.getTeamName());
        team.setTeamStatusId(TEAM_STATUS_FORMING);
        team.setLeaderUserId(currentUserId);
        team.setCreatedAt(now);
        team.setUpdatedAt(now);

        Teams savedTeam = teamsRepository.save(team);

        TeamMembers leaderMember = new TeamMembers();
        leaderMember.setTeamId(savedTeam.getTeamId());
        leaderMember.setUserId(currentUserId);
        leaderMember.setJoinedAt(now);
        leaderMember.setActive(true);

        teamMembersRepository.save(leaderMember);
        teamEventRegistrationService.ensurePendingRegistration(
                savedTeam.getTeamId(),
                savedTeam.getEventId(),
                currentUserId,
                currentUserId);

        // User đã có team của riêng mình: tự hủy các request PENDING họ từng gửi
        // sang team khác trong cùng event.
        teamJoinRequestCleaner.cancelOtherPendingRequestsForUser(
                currentUserId, event.getEventId(), savedTeam.getTeamId());

        List<TeamMembers> members = teamMembersRepository.findByTeamIdAndActiveTrue(savedTeam.getTeamId());
        return toTeamResponse(savedTeam, members);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllTeams() {
        return teamsRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getByEventId(UUID eventId) {
        return teamsRepository.findByEventIdWithActiveMembers(eventId)
                .stream()
                .map(team -> {
                    List<TeamMembers> members = teamMembersRepository.findByTeamIdAndActiveTrue(team.getTeamId());
                    return toTeamResponse(team, members);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getTeamsByUserId(UUID userId) {
        return teamsRepository.findByUserId(userId)
                .stream()
                .map(team -> {
                    List<TeamMembers> members = teamMembersRepository.findByTeamIdAndActiveTrue(team.getTeamId());
                    return toTeamResponse(team, members);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamEligibilityReviewResponse> reviewTeamsEligibility(UUID eventId) {
        Event event = getActiveEvent(eventId);

        return teamsRepository.findByEventId(eventId)
                .stream()
                .map(team -> toEligibilityReviewResponse(team, event))
                .toList();
    }

    @Override
    @Transactional
    public TeamResponse activateTeam(UUID teamId, String note, UUID adminUserId) {
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
        Event event = requireActiveEvent(team.getEvent());

        if (!TEAM_STATUS_PENDING.equals(team.getTeamStatusId())) {
            throw new BusinessConflictException("Only pending teams can be approved");
        }

        TeamEligibilityReviewResponse review = toEligibilityReviewResponse(team, event);
        if (!Boolean.TRUE.equals(review.getEligibleForCompetition())) {
            // Nêu rõ lý do (size min/max, hồ sơ thiếu...) để organizer biết cần gì trước
            // khi duyệt.
            String reasons = review.getIssues() != null && !review.getIssues().isEmpty()
                    ? String.join("; ", review.getIssues())
                    : "unknown reason";
            throw new BusinessConflictException("Team is not eligible for competition: " + reasons);
        }

        team.setTeamStatusId(TEAM_STATUS_ACTIVE);
        team.setUpdatedAt(LocalDateTime.now());
        Teams savedTeam = teamsRepository.save(team);
        saveEligibilityApprovedAuditLog(savedTeam, note, adminUserId);

        List<TeamMembers> members = teamMembersRepository.findByTeamIdAndActiveTrue(savedTeam.getTeamId());
        return toTeamResponse(savedTeam, members);
    }

    private void releaseRejectedOrInactiveDuplicateTeamName(UUID eventId, String teamName, LocalDateTime now) {
        List<Teams> sameNameTeams = teamsRepository.findByEventIdAndTeamNameIgnoreCaseForUpdate(eventId, teamName);
        List<Teams> teamsToRelease = new ArrayList<>();
        List<TeamMembers> membersToDeactivate = new ArrayList<>();

        for (Teams existingTeam : sameNameTeams) {
            if (TEAM_STATUS_REJECTED.equals(existingTeam.getTeamStatusId())) {
                releaseTeamName(existingTeam, now);
                teamsToRelease.add(existingTeam);
                continue;
            }

            List<TeamMembers> activeMembers = teamMembersRepository.findByTeamIdAndActiveTrue(existingTeam.getTeamId());
            if (!activeMembers.isEmpty() && !allActiveMembersRejectedForEvent(existingTeam, activeMembers)) {
                throw new BusinessConflictException("Team name already exists in this event");
            }

            existingTeam.setTeamStatusId(TEAM_STATUS_REJECTED);
            existingTeam.setUpdatedAt(now);
            teamsToRelease.add(existingTeam);

            activeMembers.forEach(member -> {
                member.setActive(false);
                member.setLeftAt(now);
            });
            membersToDeactivate.addAll(activeMembers);
        }

        if (!teamsToRelease.isEmpty()) {
            teamsRepository.saveAll(teamsToRelease);
            teamsRepository.flush();
        }
        if (!membersToDeactivate.isEmpty()) {
            teamMembersRepository.saveAll(membersToDeactivate);
        }
    }

    private void releaseTeamName(Teams team, LocalDateTime now) {
        String suffix = REJECTED_TEAM_NAME_SUFFIX_PREFIX + team.getTeamId() + "]";
        String baseName = team.getTeamName();
        int maxBaseLength = TEAM_NAME_MAX_LENGTH - suffix.length();
        if (baseName.length() > maxBaseLength) {
            baseName = baseName.substring(0, maxBaseLength);
        }

        team.setTeamName(baseName + suffix);
        team.setUpdatedAt(now);
    }

    private boolean allActiveMembersRejectedForEvent(Teams team, List<TeamMembers> activeMembers) {
        for (TeamMembers member : activeMembers) {
            boolean rejected = eventParticipantRepository
                    .findByEventIdAndUserId(team.getEventId(), member.getUserId())
                    .map(participant -> participant.getParticipantStatus() != null
                            && "REJECTED".equalsIgnoreCase(participant.getParticipantStatus().getStatusName()))
                    .orElse(false);
            if (!rejected) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional
    public TeamResponse rejectTeam(UUID teamId, String note, UUID adminUserId) {
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        if (!TEAM_STATUS_PENDING.equals(team.getTeamStatusId())) {
            throw new BusinessConflictException("Only pending teams can be rejected");
        }

        LocalDateTime now = LocalDateTime.now();
        team.setTeamStatusId(TEAM_STATUS_REJECTED);
        team.setUpdatedAt(now);
        Teams savedTeam = teamsRepository.save(team);
        saveEligibilityRejectedAuditLog(savedTeam, note, adminUserId);

        List<TeamMembers> members = teamMembersRepository.findByTeamIdAndActiveTrue(savedTeam.getTeamId());
        members.forEach(member -> {
            member.setActive(false);
            member.setLeftAt(now);
        });
        teamMembersRepository.saveAll(members);

        // Team đã bị reject: đóng nốt mọi join request PENDING còn treo
        // (roster khóa vĩnh viễn, leader không thể xử lý chúng nữa).
        teamJoinRequestCleaner.rejectPendingRequestsForTeam(
                savedTeam.getTeamId(), adminUserId, "Team registration was rejected");

        eventPublisher.publishEvent(new TeamRegistrationRejectedEvent(
                members.stream()
                        .map(TeamMembers::getUserId)
                        .distinct()
                        .toList(),
                adminUserId,
                savedTeam.getEventId(),
                savedTeam.getTeamName(),
                note));

        return toTeamResponse(savedTeam, members);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getById(UUID teamId) {
        // Luồng xem team theo ID: teamId -> Teams -> danh sách member active ->
        // TeamResponse.
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        List<TeamMembers> members = teamMembersRepository.findByTeamIdAndActiveTrue(team.getTeamId());
        return toTeamResponse(team, members);
    }

    private TeamResponse toTeamResponse(Teams team, List<TeamMembers> members) {
        TeamResponse response = TeamMapper.toTeamResponse(team, members);
        if (response.getMembers() == null) {
            return response;
        }

        response.getMembers().forEach(member -> {
            String participantStatus = resolveParticipantStatusName(team, member.getUserId());
            member.setParticipantStatus(participantStatus);
            member.setParticipantStatusName(participantStatus);
        });

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public TeamMemberDetailResponse getTeamMemberDetail(
            UUID teamId,
            UUID userId,
            UUID currentUserId,
            boolean organizerViewer) {
        // Luồng xem chi tiết member: xác nhận user đang active trong team -> lấy hồ sơ
        // User
        // -> mapper ghép dữ liệu TeamMembers + User thành DTO, không trả passwordHash.
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        if (!organizerViewer) {
            teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, currentUserId)
                    .orElseThrow(() -> new AccessDeniedException("You do not belong to this team"));
        }

        TeamMembers member = teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Active team member not found"));

        User user = member.getUser();

        TeamMemberDetailResponse response = TeamMapper.toTeamMemberDetailResponse(member, user);
        String participantStatus = resolveParticipantStatusName(team, userId);
        response.setParticipantStatus(participantStatus);
        response.setParticipantStatusName(participantStatus);
        return response;
    }

    private String resolveParticipantStatusName(Teams team, UUID userId) {
        if (TEAM_STATUS_DISQUALIFIED.equals(team.getTeamStatusId())) {
            return "Suspended";
        }

        return eventParticipantRepository
                .findByEventIdAndUserId(team.getEventId(), userId)
                .map(EventParticipant::getParticipantStatus)
                .map(status -> status.getStatusName())
                .orElse(null);
    }

    @Override
    @Transactional
    public void removeMember(UUID teamId, UUID userId, UUID currentUserId, String reason) {
        // Thành viên được tự rời; leader được kick thành viên hoặc tự rời.
        // Leader rời sẽ chuyển quyền cho thành viên active tham gia sớm nhất, hoặc rút
        // team nếu không còn ai.
        Teams team = teamsRepository.findByIdForUpdate(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        TeamMembers member = teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Active team member not found"));

        boolean isLeader = team.getLeaderUserId().equals(currentUserId);
        boolean isSelfLeaving = userId.equals(currentUserId);
        boolean isLeaderRemovingMember = isLeader && !isSelfLeaving;

        if (!isLeader && !isSelfLeaving) {
            throw new AccessDeniedException("You do not have permission to remove this member");
        }
        String removalReason = trimToNull(reason);
        if (isLeaderRemovingMember && removalReason == null) {
            throw new BusinessConflictException("Removal reason is required");
        }

        assertRosterEditable(team);

        LocalDateTime now = LocalDateTime.now();
        member.setActive(false);
        member.setLeftAt(now);
        teamMembersRepository.save(member);
        teamEventRegistrationService.removePendingRegistration(team.getEventId(), userId);

        if (team.getLeaderUserId().equals(userId)) {
            List<TeamMembers> remainingMembers = teamMembersRepository
                    .findByTeamIdAndActiveTrueOrderByJoinedAtAscTeamMemberIdAsc(teamId);

            if (remainingMembers.isEmpty()) {
                deleteEmptyFormingTeam(team);
            } else {
                team.setLeaderUserId(remainingMembers.get(0).getUserId());
                team.setUpdatedAt(now);
                teamsRepository.save(team);
            }
        }

        if (isLeaderRemovingMember) {
            notifyMemberRemoved(team, userId, currentUserId, removalReason);
        }
    }

    private void notifyMemberRemoved(Teams team, UUID removedUserId, UUID leaderUserId, String reason) {
        String eventName = team.getEvent() != null ? team.getEvent().getEventName() : "the event";
        String body = "You have been removed from team " + team.getTeamName()
                + " in " + eventName + ". Reason: " + reason;
        notificationService.sendNotification(
                removedUserId,
                leaderUserId,
                team.getEventId(),
                "Removed From Team",
                body);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void deleteEmptyFormingTeam(Teams team) {
        UUID teamId = team.getTeamId();
        teamJoinRequestsRepository.deleteByTeamId(teamId);
        teamMembersRepository.deleteByTeamId(teamId);
        teamsRepository.delete(team);
    }

    @Override
    @Transactional
    public com.fpt.swp.sealhackathonbe.team.dto.LeadershipReassignmentResult reassignLeadershipForDeactivatedUser(
            UUID userId, UUID actorUserId) {
        // Deactivate GIỮ ghế của user (không giảm sĩ số) — chỉ chuyển quyền leader nếu cần,
        // để team không bị đóng băng (chỉ leader mới nộp bài được). KHÔNG gọi assertRosterEditable
        // vì việc này phải chạy được cả khi roster đã khóa giữa event.
        var result = com.fpt.swp.sealhackathonbe.team.dto.LeadershipReassignmentResult.builder().build();

        List<TeamMembers> memberships = teamMembersRepository.findAllByUserIdAndActiveTrue(userId);
        for (TeamMembers membership : memberships) {
            Teams team = teamsRepository.findByIdForUpdate(membership.getTeamId()).orElse(null);
            if (team == null || !team.getLeaderUserId().equals(userId)) {
                continue; // user chỉ là thành viên thường → không đụng
            }

            List<TeamMembers> others = teamMembersRepository
                    .findByTeamIdAndActiveTrueOrderByJoinedAtAscTeamMemberIdAsc(team.getTeamId())
                    .stream()
                    .filter(m -> !m.getUserId().equals(userId))
                    .toList();

            if (others.isEmpty()) {
                // Leader kiêm thành viên duy nhất — KHÔNG giải tán (deactivate đảo ngược được),
                // giữ nguyên team (tự đóng băng) và cảnh báo để organizer xử lý.
                result.getFrozenTeams().add(team.getTeamName());
                continue;
            }

            TeamMembers newLeader = others.get(0);
            team.setLeaderUserId(newLeader.getUserId());
            team.setUpdatedAt(LocalDateTime.now());
            teamsRepository.save(team);

            String newLeaderName = newLeader.getUser() != null ? newLeader.getUser().getFullName() : null;
            result.getTransfers().add(
                    com.fpt.swp.sealhackathonbe.team.dto.LeadershipReassignmentResult.TransferInfo.builder()
                            .teamName(team.getTeamName())
                            .newLeaderName(newLeaderName)
                            .build());

            for (TeamMembers member : others) {
                try {
                    notificationService.sendNotification(
                            member.getUserId(),
                            actorUserId,
                            team.getEventId(),
                            "Team Leader Changed",
                            "Trưởng nhóm của team " + team.getTeamName()
                                    + " đã bị vô hiệu hóa. "
                                    + (newLeaderName != null ? newLeaderName : "Một thành viên")
                                    + " nay là trưởng nhóm mới.");
                } catch (RuntimeException ex) {
                    // Thông báo là best-effort: lỗi gửi không được làm rollback việc chuyển quyền.
                    log.warn("Failed to notify member {} of leader change in team {}",
                            member.getUserId(), team.getTeamId(), ex);
                }
            }
        }

        return result;
    }

    @Override
    @Transactional
    public void disbandTeam(UUID teamId, UUID currentUserId) {
        // Leader giải tán team đang FORMING bằng một thao tác:
        // gỡ đăng ký event PENDING của từng thành viên, rồi xóa team
        // (kèm toàn bộ join request và membership) — không để team "trôi".
        Teams team = teamsRepository.findByIdForUpdate(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        if (!team.getLeaderUserId().equals(currentUserId)) {
            throw new AccessDeniedException("Only the team leader can disband the team");
        }

        assertRosterEditable(team);

        List<TeamMembers> members = teamMembersRepository.findByTeamIdAndActiveTrue(teamId);
        for (TeamMembers member : members) {
            teamEventRegistrationService.removePendingRegistration(team.getEventId(), member.getUserId());
        }

        deleteEmptyFormingTeam(team);
    }

    @Override
    @Transactional
    public TeamResponse transferLeadership(UUID teamId, UUID newLeaderUserId, UUID currentUserId) {
        Teams team = teamsRepository.findByIdForUpdate(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        if (!team.getLeaderUserId().equals(currentUserId)) {
            throw new AccessDeniedException("Only the current team leader can transfer leadership");
        }

        assertRosterEditable(team);

        if (currentUserId.equals(newLeaderUserId)) {
            throw new BusinessConflictException("New leader must be a different team member");
        }
        teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, newLeaderUserId)
                .orElseThrow(() -> new BusinessConflictException(
                        "New leader must be an active member of this team"));

        team.setLeaderUserId(newLeaderUserId);
        team.setUpdatedAt(LocalDateTime.now());
        Teams savedTeam = teamsRepository.save(team);

        List<TeamMembers> members = teamMembersRepository.findByTeamIdAndActiveTrue(teamId);
        return toTeamResponse(savedTeam, members);
    }

    private TeamEligibilityReviewResponse toEligibilityReviewResponse(Teams team, Event event) {
        List<TeamMembers> members = teamMembersRepository.findByTeamIdOrderByJoinedAtAsc(team.getTeamId());
        List<TeamEligibilityMemberResponse> memberResponses = members.stream()
                .map(this::toEligibilityMemberResponse)
                .toList();

        long activeMemberCount = members.stream().filter(TeamMembers::getActive).count();
        List<String> issues = new ArrayList<>();

        Integer minTeamSize = event.getMinTeamSize();
        if (minTeamSize != null && activeMemberCount < minTeamSize) {
            issues.add("Team has fewer active members than the event minimum");
        }

        Integer maxTeamSize = event.getMaxTeamSize();
        if (maxTeamSize != null && activeMemberCount > maxTeamSize) {
            issues.add("Team has more active members than the event maximum");
        }

        if (TEAM_STATUS_DISQUALIFIED.equals(team.getTeamStatusId())) {
            issues.add("Team is disqualified");
        }

        if (TEAM_STATUS_REJECTED.equals(team.getTeamStatusId())) {
            issues.add("Team registration request was rejected");
        }

        if (TEAM_STATUS_WITHDRAWN.equals(team.getTeamStatusId())) {
            issues.add("Team withdrew from the event");
        }

        boolean teamSizeEligible = issues.stream()
                .noneMatch(issue -> issue.contains("active members"));
        boolean membersInfoComplete = memberResponses.stream()
                .allMatch(member -> Boolean.TRUE.equals(member.getProfileComplete()));
        boolean eligibleForCompetition = teamSizeEligible
                && membersInfoComplete
                && !TEAM_STATUS_DISQUALIFIED.equals(team.getTeamStatusId())
                && !TEAM_STATUS_WITHDRAWN.equals(team.getTeamStatusId())
                && !TEAM_STATUS_REJECTED.equals(team.getTeamStatusId());

        if (!membersInfoComplete) {
            issues.add("One or more members have incomplete profile information");
        }

        TeamEligibilityReviewResponse response = new TeamEligibilityReviewResponse();
        response.setTeamId(team.getTeamId());
        response.setEventId(team.getEventId());
        response.setCategoryId(team.getCategoryId());
        response.setTeamName(team.getTeamName());
        response.setTeamStatusId(team.getTeamStatusId());
        response.setLeaderUserId(team.getLeaderUserId());
        response.setMinTeamSize(minTeamSize);
        response.setMaxTeamSize(maxTeamSize);
        response.setActiveMemberCount(activeMemberCount);
        response.setTeamSizeEligible(teamSizeEligible);
        response.setMembersInfoComplete(membersInfoComplete);
        response.setEligibleForCompetition(eligibleForCompetition);
        response.setIssues(issues);
        response.setMembers(memberResponses);
        return response;
    }

    private TeamEligibilityMemberResponse toEligibilityMemberResponse(TeamMembers member) {
        User user = member.getUser();
        List<String> issues = new ArrayList<>();

        if (user == null) {
            issues.add("User profile not found");
        } else {
            if (isBlank(user.getFullName())) {
                issues.add("Full name is missing");
            }

            if (isBlank(user.getPhone())) {
                issues.add("Phone is missing");
            } else if (!hasValidPhoneLength(user.getPhone())) {
                issues.add("Phone format is invalid");
            }

            boolean hasFptCode = !isBlank(user.getFptStudentCode());
            boolean hasExternalCode = !isBlank(user.getExternalStudentCode());
            boolean hasUniversity = !isBlank(user.getUniversityName());

            if (!hasFptCode && !hasExternalCode) {
                issues.add("Student code is missing");
            }
            if (!hasFptCode && hasExternalCode && !hasUniversity) {
                issues.add("University name is missing");
            }

            String accountStatusName = user.getAccountStatus() != null
                    ? user.getAccountStatus().getStatusName()
                    : null;
            if (!"Active".equalsIgnoreCase(accountStatusName)) {
                issues.add("Account is not active");
            }
        }

        TeamEligibilityMemberResponse response = new TeamEligibilityMemberResponse();
        response.setTeamMemberId(member.getTeamMemberId());
        response.setUserId(member.getUserId());
        response.setJoinedAt(member.getJoinedAt());
        response.setActive(member.getActive());
        response.setProfileComplete(issues.isEmpty());
        response.setIssues(issues);

        if (user != null) {
            response.setFullName(user.getFullName());
            response.setEmail(user.getEmail());
            response.setPhone(user.getPhone());
            response.setFptStudentCode(user.getFptStudentCode());
            response.setExternalStudentCode(user.getExternalStudentCode());
            response.setUniversityName(user.getUniversityName());
            response.setUserTypeName(user.getUserType() != null ? user.getUserType().getTypeName() : null);
            response.setAccountStatusName(
                    user.getAccountStatus() != null ? user.getAccountStatus().getStatusName() : null);
        }

        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean hasValidPhoneLength(String phone) {
        String digits = phone.replaceAll("\\D", "");
        return digits.length() >= 9 && digits.length() <= 15;
    }

    private void saveEligibilityApprovedAuditLog(Teams team, String note, UUID adminUserId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActionType("TEAM_ELIGIBILITY_APPROVED");
        auditLog.setEntityType("Teams");
        auditLog.setEntityId(team.getTeamId());
        auditLog.setActorUserId(adminUserId);
        auditLog.setNewValueJson(
                "{\"status\":\"Active\",\"note\":\"" + escapeJson(note) + "\"}");
        auditLog.setOccurredAt(LocalDateTime.now());
        auditLog.setNotes(note);

        auditLogRepository.save(auditLog);
    }

    private void saveEligibilityRejectedAuditLog(Teams team, String note, UUID adminUserId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActionType("TEAM_ELIGIBILITY_REJECTED");
        auditLog.setEntityType("Teams");
        auditLog.setEntityId(team.getTeamId());
        auditLog.setActorUserId(adminUserId);
        auditLog.setNewValueJson(
                "{\"status\":\"Rejected\",\"note\":\"" + escapeJson(note) + "\"}");
        auditLog.setOccurredAt(LocalDateTime.now());
        auditLog.setNotes(note);

        auditLogRepository.save(auditLog);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private Event getActiveEvent(UUID eventId) {
        // Team chi duoc tao trong event ton tai va chua bi soft delete.
        return eventRepository.findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
    }

    private void validateCategoryBelongsToEvent(UUID categoryId, UUID eventId) {
        boolean exists = categoryRepository.existsByCategoryIdAndEventEventIdAndIsActiveTrue(categoryId, eventId);
        if (!exists) {
            throw new BusinessConflictException("Category does not belong to this event");
        }
    }

    private void validateTeamSizeConfig(Event event) {
        // Chan cau hinh event khong hop le truoc khi ap dung gioi han thanh vien.
        Integer minTeamSize = event.getMinTeamSize();
        Integer maxTeamSize = event.getMaxTeamSize();

        if (minTeamSize != null && minTeamSize < 1) {
            throw new BusinessConflictException("Minimum team size must be at least 1");
        }

        if (maxTeamSize != null && maxTeamSize < 1) {
            throw new BusinessConflictException("Maximum team size must be at least 1");
        }

        if (minTeamSize != null && maxTeamSize != null && minTeamSize > maxTeamSize) {
            throw new BusinessConflictException("Minimum team size cannot be greater than maximum team size");
        }
    }

    private void assertRosterEditable(Teams team) {
        if (!TEAM_STATUS_FORMING.equals(team.getTeamStatusId())) {
            throw new BusinessConflictException("Team roster can only be changed while the team is forming");
        }
    }

    private Event requireActiveEvent(Event event) {
        // Quan he lazy co the null; nghiep vu team khong xu ly event da bi soft delete.
        if (event == null || Boolean.TRUE.equals(event.getIsDeleted())) {
            throw new EntityNotFoundException("Event not found");
        }

        return event;
    }
}
