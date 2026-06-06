package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.team.dto.HandleJoinRequest;
import com.fpt.swp.sealhackathonbe.team.dto.JoinTeamRequestResponse;
import com.fpt.swp.sealhackathonbe.team.entity.TeamJoinRequests;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamJoinRequestsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
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

    public TeamJoinRequestServiceImpl(
            TeamsRepository teamsRepository,
            TeamMembersRepository teamMembersRepository,
            TeamJoinRequestsRepository teamJoinRequestsRepository
    ) {
        this.teamsRepository = teamsRepository;
        this.teamMembersRepository = teamMembersRepository;
        this.teamJoinRequestsRepository = teamJoinRequestsRepository;
    }

    @Override
    @Transactional
    public JoinTeamRequestResponse requestToJoinTeam(UUID teamId, UUID currentUserId) {
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (TEAM_STATUS_DISQUALIFIED.equals(team.getTeamStatusId())
                || TEAM_STATUS_WITHDRAWN.equals(team.getTeamStatusId())) {
            throw new RuntimeException("Cannot join this team");
        }

        if (teamMembersRepository.existsByUserIdAndActiveTrue(currentUserId)) {
            throw new RuntimeException("User already belongs to an active team");
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
    public List<JoinTeamRequestResponse> getPendingJoinRequests(UUID teamId, UUID leaderUserId) {
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
        TeamJoinRequests joinRequest = teamJoinRequestsRepository
                .findByRequestIdAndRequestStatus(requestId, REQUEST_STATUS_PENDING)
                .orElseThrow(() -> new RuntimeException("Pending join request not found"));

        Teams team = teamsRepository.findById(joinRequest.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!team.getLeaderUserId().equals(leaderUserId)) {
            throw new RuntimeException("Only team leader can handle join request");
        }

        if (REQUEST_STATUS_APPROVED.equals(request.getAction())) {
            if (teamMembersRepository.existsByUserIdAndActiveTrue(joinRequest.getUserId())) {
                throw new RuntimeException("User already belongs to an active team");
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
}
