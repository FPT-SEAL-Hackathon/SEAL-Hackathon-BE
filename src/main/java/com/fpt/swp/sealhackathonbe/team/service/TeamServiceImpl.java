package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.team.dto.CreateTeamRequest;
import com.fpt.swp.sealhackathonbe.team.dto.TeamResponse;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TeamServiceImpl implements TeamService {
    private static final UUID TEAM_STATUS_FORMING =
            UUID.fromString("60000000-0000-0000-0000-000000000001");
    private static final UUID TEAM_STATUS_ACTIVE =
            UUID.fromString("60000000-0000-0000-0000-000000000002");

    private final TeamsRepository teamsRepository;
    private final TeamMembersRepository teamMembersRepository;

    public TeamServiceImpl(
            TeamsRepository teamsRepository,
            TeamMembersRepository teamMembersRepository
    ) {
        this.teamsRepository = teamsRepository;
        this.teamMembersRepository = teamMembersRepository;
    }

    @Override
    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request, UUID currentUserId) {
        if (teamsRepository.existsByEventIdAndTeamName(request.getEventId(), request.getTeamName())) {
            throw new RuntimeException("Team name already exists in this event");
        }

        if (teamMembersRepository.existsByUserIdAndActiveTrue(currentUserId)) {
            throw new RuntimeException("User already belongs to an active team");
        }

        LocalDateTime now = LocalDateTime.now();

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

        List<TeamMembers> members = teamMembersRepository.findByTeamIdAndActiveTrue(savedTeam.getTeamId());
        return TeamMapper.toTeamResponse(savedTeam, members);
    }

    @Override
    public TeamResponse getMyTeam(UUID currentUserId) {
        TeamMembers member = teamMembersRepository.findByUserIdAndActiveTrue(currentUserId)
                .orElseThrow(() -> new RuntimeException("User does not belong to any active team"));

        Teams team = teamsRepository.findById(member.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));

        List<TeamMembers> members = teamMembersRepository.findByTeamIdAndActiveTrue(team.getTeamId());
        return TeamMapper.toTeamResponse(team, members);
    }

    @Override
    @Transactional
    public void removeMember(UUID userId, UUID currentUserId) {
        TeamMembers member = teamMembersRepository.findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("Active team member not found"));

        Teams team = teamsRepository.findById(member.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));

        boolean isLeader = team.getLeaderUserId().equals(currentUserId);
        boolean isSelfLeaving = userId.equals(currentUserId);

        if (!isLeader && !isSelfLeaving) {
            throw new RuntimeException("You do not have permission to remove this member");
        }

        if (team.getLeaderUserId().equals(userId)) {
            throw new RuntimeException("Team leader cannot be removed");
        }

        member.setActive(false);
        member.setLeftAt(LocalDateTime.now());

        teamMembersRepository.save(member);
    }
}
