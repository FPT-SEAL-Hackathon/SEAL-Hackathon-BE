package com.fpt.swp.sealhackathonbe.team.service.impl;

import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.eventparticipant.service.EventParticipantService;
import com.fpt.swp.sealhackathonbe.team.dto.HandleJoinRequest;
import com.fpt.swp.sealhackathonbe.team.dto.JoinTeamRequestResponse;
import com.fpt.swp.sealhackathonbe.team.entity.TeamJoinRequests;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamJoinRequestsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.service.TeamJoinRequestService;
import com.fpt.swp.sealhackathonbe.team.service.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamJoinRequestServiceImpl implements TeamJoinRequestService {
    private static final UUID TEAM_STATUS_DISQUALIFIED =
            UUID.fromString("60000000-0000-0000-0000-000000000003");
    private static final UUID TEAM_STATUS_WITHDRAWN =
            UUID.fromString("60000000-0000-0000-0000-000000000004");

    private static final String REQUEST_STATUS_PENDING = "PENDING";
    private static final String REQUEST_STATUS_APPROVED = "APPROVED";
    private static final String REQUEST_STATUS_REJECTED = "REJECTED";

    private final TeamsRepository teamsRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final TeamJoinRequestsRepository teamJoinRequestsRepository;
    private final EventParticipantService eventParticipantService;

    @Override
    @Transactional
    public JoinTeamRequestResponse requestToJoinTeam(UUID teamId, UUID currentUserId) {
        // Luồng xin vào team: user chọn team -> kiểm tra team hợp lệ/chưa đầy
        // -> kiểm tra user chưa thuộc team active -> tạo request PENDING -> trả DTO.
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        validateTeamCanReceiveJoinRequest(team);
        eventParticipantService.assertActiveParticipant(team.getEventId(), currentUserId);

        if (teamMembersRepository.existsByUserIdAndTeam_EventIdAndActiveTrue(currentUserId, team.getEventId())) {
            throw new RuntimeException("User already belongs to an active team in this event");
        }

        if (teamJoinRequestsRepository.existsByTeamIdAndUserIdAndRequestStatus(
                teamId,
                currentUserId,
                REQUEST_STATUS_PENDING
        )) {
            throw new RuntimeException("User already has a pending request for this team");
        }

        TeamJoinRequests joinRequest = new TeamJoinRequests();
        joinRequest.setTeamId(teamId);
        joinRequest.setUserId(currentUserId);
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
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!team.getLeaderUserId().equals(leaderUserId)) {
            throw new RuntimeException("Only team leader can view join requests");
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
                .orElseThrow(() -> new RuntimeException("Pending join request not found"));

        Teams team = joinRequest.getTeam();

        if (!team.getLeaderUserId().equals(leaderUserId)) {
            throw new RuntimeException("Only team leader can handle join request");
        }

        if (REQUEST_STATUS_APPROVED.equals(request.getAction())) {
            validateTeamCanReceiveJoinRequest(team);
            eventParticipantService.assertActiveParticipant(team.getEventId(), joinRequest.getUserId());

            if (teamMembersRepository.existsByUserIdAndTeam_EventIdAndActiveTrue(
                    joinRequest.getUserId(),
                    team.getEventId()
            )) {
                throw new RuntimeException("User already belongs to an active team in this event");
            }

            TeamMembers member = new TeamMembers();
            member.setTeamId(team.getTeamId());
            member.setUserId(joinRequest.getUserId());
            member.setJoinedAt(LocalDateTime.now());
            member.setActive(true);

            teamMembersRepository.save(member);

            joinRequest.setRequestStatus(REQUEST_STATUS_APPROVED);
        } else if (REQUEST_STATUS_REJECTED.equals(request.getAction())) {
            joinRequest.setRequestStatus(REQUEST_STATUS_REJECTED);
        } else {
            throw new RuntimeException("Invalid request action");
        }

        joinRequest.setRespondedAt(LocalDateTime.now());
        joinRequest.setRespondedById(leaderUserId);
        joinRequest.setResponseNote(request.getResponseNote());

        TeamJoinRequests savedRequest = teamJoinRequestsRepository.save(joinRequest);
        return TeamMapper.toJoinTeamRequestResponse(savedRequest);
    }

    private void validateTeamCanReceiveJoinRequest(Teams team) {
        // Team da bi loai/rut lui khong duoc nhan don moi; team hop le van phai con cho.
        if (TEAM_STATUS_DISQUALIFIED.equals(team.getTeamStatusId())
                || TEAM_STATUS_WITHDRAWN.equals(team.getTeamStatusId())) {
            throw new RuntimeException("Cannot join this team");
        }

        validateTeamIsNotFull(team);
    }

    private void validateTeamIsNotFull(Teams team) {
        // MaxTeamSize nằm ở Event; trước khi tạo/duyệt request cần đếm member active hiện tại của team.
        Event event = requireActiveEvent(team.getEvent());

        Integer maxTeamSize = event.getMaxTeamSize();
        long activeMemberCount = teamMembersRepository.countByTeamIdAndActiveTrue(team.getTeamId());

        if (maxTeamSize != null && activeMemberCount >= maxTeamSize) {
            throw new RuntimeException("Team has reached maximum size");
        }
    }

    private Event requireActiveEvent(Event event) {
        // Join request chi duoc xu ly khi event cua team van ton tai.
        if (event == null || Boolean.TRUE.equals(event.getIsDeleted())) {
            throw new RuntimeException("Event not found");
        }

        return event;
    }
}
