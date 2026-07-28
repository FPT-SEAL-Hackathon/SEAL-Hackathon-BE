package com.fpt.swp.sealhackathonbe.team.service.impl;

import com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants;

import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.team.event.TeamJoinApprovedEvent;
import com.fpt.swp.sealhackathonbe.team.dto.HandleJoinRequest;
import com.fpt.swp.sealhackathonbe.team.dto.JoinTeamRequestResponse;
import com.fpt.swp.sealhackathonbe.team.entity.TeamJoinRequests;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamJoinRequestsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.service.TeamEventRegistrationService;
import com.fpt.swp.sealhackathonbe.team.service.TeamJoinRequestService;
import com.fpt.swp.sealhackathonbe.team.service.mapper.TeamMapper;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamJoinRequestServiceImpl implements TeamJoinRequestService {
    private static final UUID TEAM_STATUS_FORMING =
            TeamStatusConstants.FORMING;

    private static final String REQUEST_STATUS_PENDING = "PENDING";
    private static final String REQUEST_STATUS_APPROVED = "APPROVED";
    private static final String REQUEST_STATUS_REJECTED = "REJECTED";
    private static final String REQUEST_STATUS_CANCELLED = "CANCELLED";

    private final TeamsRepository teamsRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final TeamJoinRequestsRepository teamJoinRequestsRepository;
    private final TeamEventRegistrationService teamEventRegistrationService;
    private final TeamJoinRequestCleaner teamJoinRequestCleaner;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public JoinTeamRequestResponse requestToJoinTeam(UUID teamId, UUID currentUserId) {
        // Luồng xin vào team: user chọn team -> kiểm tra team hợp lệ/chưa đầy
        // -> kiểm tra user chưa thuộc team active -> tạo request PENDING -> trả DTO.
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        validateTeamCanReceiveJoinRequest(team);
        teamEventRegistrationService.assertEventOpenForRegistration(team.getEventId());
        // Team-first: xin vào team chỉ cần là student đủ điều kiện, không cần
        // là EventParticipant; nhưng đội hình bị khóa sau khi team đã đăng ký event.
        teamEventRegistrationService.assertEligibleStudent(currentUserId);
        assertRosterNotLocked(team);

        if (teamMembersRepository.existsByUserIdAndTeam_EventIdAndActiveTrue(currentUserId, team.getEventId())) {
            throw new BusinessConflictException("User already belongs to an active team in this event");
        }

        if (teamJoinRequestsRepository.existsByTeamIdAndUserIdAndRequestStatus(
                teamId,
                currentUserId,
                REQUEST_STATUS_PENDING
        )) {
            throw new BusinessConflictException("User already has a pending request for this team");
        }

        TeamJoinRequests joinRequest = new TeamJoinRequests();
        joinRequest.setTeamId(teamId);
        joinRequest.setUserId(currentUserId);
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        joinRequest.setUser(user);
        joinRequest.setRequestStatus(REQUEST_STATUS_PENDING);
        joinRequest.setRequestedAt(LocalDateTime.now());

        TeamJoinRequests savedRequest = teamJoinRequestsRepository.save(joinRequest);
        return TeamMapper.toJoinTeamRequestResponse(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JoinTeamRequestResponse> getPendingJoinRequests(UUID teamId, UUID leaderUserId) {
        // Luồng leader xem đơn: kiểm tra leader của team -> lấy các request PENDING -> map sang response.
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        if (!team.getLeaderUserId().equals(leaderUserId)) {
            throw new AccessDeniedException("Only team leader can view join requests");
        }

        return teamJoinRequestsRepository.findByTeamIdAndRequestStatus(teamId, REQUEST_STATUS_PENDING)
                .stream()
                .map(TeamMapper::toJoinTeamRequestResponse)
                .toList();
    }

    @Override
    @Transactional
    public JoinTeamRequestResponse handleJoinRequest(
            UUID requestId,
            HandleJoinRequest request,
            UUID leaderUserId
    ) {
        // Luồng xử lý đơn: tìm request PENDING -> kiểm tra người xử lý là leader
        // -> nếu APPROVED thì kiểm tra MaxTeamSize rồi thêm TeamMembers -> cập nhật trạng thái request.
        TeamJoinRequests joinRequest = teamJoinRequestsRepository
                .findByRequestIdAndRequestStatus(requestId, REQUEST_STATUS_PENDING)
                .orElseThrow(() -> new EntityNotFoundException("Pending join request not found"));

        // Lock team (cùng pattern removeMember/transferLeadership) để hai lượt approve
        // đồng thời — hoặc approve đua với register-event — không cùng qua được
        // check max size / trạng thái FORMING rồi ghi vượt sĩ số.
        Teams team = teamsRepository.findByIdForUpdate(joinRequest.getTeamId())
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        if (!team.getLeaderUserId().equals(leaderUserId)) {
            throw new AccessDeniedException("Only team leader can handle join request");
        }

        if (REQUEST_STATUS_APPROVED.equals(request.getAction())) {
            validateTeamCanReceiveJoinRequest(team);
            teamEventRegistrationService.assertEventOpenForRegistration(team.getEventId());
            teamEventRegistrationService.assertEligibleStudent(joinRequest.getUserId());
            assertRosterNotLocked(team);

            if (teamMembersRepository.existsByUserIdAndTeam_EventIdAndActiveTrue(
                    joinRequest.getUserId(),
                    team.getEventId()
            )) {
                throw new BusinessConflictException("User already belongs to an active team in this event");
            }

            LocalDateTime approvedAt = LocalDateTime.now();
            TeamMembers member = teamMembersRepository
                    .findByTeamIdAndUserId(team.getTeamId(), joinRequest.getUserId())
                    .orElseGet(() -> {
                        TeamMembers newMember = new TeamMembers();
                        newMember.setTeamId(team.getTeamId());
                        newMember.setUserId(joinRequest.getUserId());
                        return newMember;
                    });
            member.setJoinedAt(approvedAt);
            member.setLeftAt(null);
            member.setActive(true);

            teamMembersRepository.save(member);
            teamEventRegistrationService.ensurePendingRegistration(
                    team.getTeamId(),
                    team.getEventId(),
                    joinRequest.getUserId(),
                    leaderUserId);

            joinRequest.setRequestStatus(REQUEST_STATUS_APPROVED);

            // User đã có team: tự hủy các request PENDING khác của họ trong event
            // để leader các team khác không còn thấy request chết.
            teamJoinRequestCleaner.cancelOtherPendingRequestsForUser(
                    joinRequest.getUserId(),
                    team.getEventId(),
                    team.getTeamId()
            );

            eventPublisher.publishEvent(new TeamJoinApprovedEvent(
                    joinRequest.getUserId(),
                    leaderUserId,
                    team.getEventId(),
                    team.getTeamName()
            ));
        } else if (REQUEST_STATUS_REJECTED.equals(request.getAction())) {
            joinRequest.setRequestStatus(REQUEST_STATUS_REJECTED);
        } else {
            throw new BadRequestException("Invalid request action");
        }

        joinRequest.setRespondedAt(LocalDateTime.now());
        joinRequest.setRespondedById(leaderUserId);
        joinRequest.setResponseNote(request.getResponseNote());

        TeamJoinRequests savedRequest = teamJoinRequestsRepository.save(joinRequest);
        return TeamMapper.toJoinTeamRequestResponse(savedRequest);
    }

    @Override
    @Transactional
    public JoinTeamRequestResponse cancelJoinRequest(UUID requestId, UUID currentUserId) {
        // Người xin vào team tự hủy request PENDING của chính mình.
        // Không cho hủy request của người khác, không cho hủy request đã xử lý.
        TeamJoinRequests joinRequest = teamJoinRequestsRepository
                .findByRequestIdAndRequestStatus(requestId, REQUEST_STATUS_PENDING)
                .orElseThrow(() -> new EntityNotFoundException("Pending join request not found"));

        if (!joinRequest.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only cancel your own join request");
        }

        joinRequest.setRequestStatus(REQUEST_STATUS_CANCELLED);
        joinRequest.setRespondedAt(LocalDateTime.now());
        joinRequest.setResponseNote("Cancelled by requester");

        TeamJoinRequests savedRequest = teamJoinRequestsRepository.save(joinRequest);
        return TeamMapper.toJoinTeamRequestResponse(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JoinTeamRequestResponse> getMyPendingJoinRequests(UUID currentUserId) {
        // Người xin xem các request PENDING của chính mình (để hiển thị trạng thái + nút hủy).
        return teamJoinRequestsRepository
                .findByUserIdAndRequestStatus(currentUserId, REQUEST_STATUS_PENDING)
                .stream()
                .map(TeamMapper::toJoinTeamRequestResponse)
                .toList();
    }

    private void assertRosterNotLocked(Teams team) {
        if (!TEAM_STATUS_FORMING.equals(team.getTeamStatusId())) {
            throw new BusinessConflictException("Team roster can only be changed while the team is forming");
        }
    }

    private void validateTeamCanReceiveJoinRequest(Teams team) {
        if (!TEAM_STATUS_FORMING.equals(team.getTeamStatusId())) {
            throw new BusinessConflictException("Only forming teams can receive join requests");
        }

        validateTeamIsNotFull(team);
    }

    private void validateTeamIsNotFull(Teams team) {
        // MaxTeamSize nằm ở Event; trước khi tạo/duyệt request cần đếm member active hiện tại của team.
        Event event = requireActiveEvent(team.getEvent());

        Integer maxTeamSize = event.getMaxTeamSize();
        long activeMemberCount = teamMembersRepository.countByTeamIdAndActiveTrue(team.getTeamId());

        if (maxTeamSize != null && activeMemberCount >= maxTeamSize) {
            throw new BusinessConflictException("Team has reached maximum size");
        }
    }

    private Event requireActiveEvent(Event event) {
        // Join request chi duoc xu ly khi event cua team van ton tai.
        if (event == null || Boolean.TRUE.equals(event.getIsDeleted())) {
            throw new EntityNotFoundException("Event not found");
        }

        return event;
    }
}
