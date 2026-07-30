package com.fpt.swp.sealhackathonbe.team.service.mapper;

import com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants;
import com.fpt.swp.sealhackathonbe.team.dto.TeamResponse;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.user.entity.AccountStatus;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamMapperTest {

    @Test
    void pendingEligibleTeamDoesNotExposeFormingOnlyApprovalIssue() {
        Teams team = team(TeamStatusConstants.PENDING);
        List<TeamMembers> members = List.of(member(team), member(team));

        TeamResponse response = TeamMapper.toTeamResponse(team, members);

        assertFalse(response.getCanRequestApproval());
        assertTrue(response.getApprovalIssues().isEmpty());
    }

    @Test
    void formingEligibleTeamCanRequestApproval() {
        Teams team = team(TeamStatusConstants.FORMING);
        List<TeamMembers> members = List.of(member(team), member(team));

        TeamResponse response = TeamMapper.toTeamResponse(team, members);

        assertTrue(response.getCanRequestApproval());
        assertTrue(response.getApprovalIssues().isEmpty());
    }

    @Test
    void mapsEventNameAndCategoryNameWhenPresent() {
        Teams team = team(TeamStatusConstants.FORMING);
        com.fpt.swp.sealhackathonbe.event.entity.Event event = new com.fpt.swp.sealhackathonbe.event.entity.Event();
        event.setEventName("AI Hackathon 2026");
        team.setEvent(event);

        com.fpt.swp.sealhackathonbe.category.entity.Category category = new com.fpt.swp.sealhackathonbe.category.entity.Category();
        category.setCategoryName("AI / ML");
        team.setCategory(category);

        List<TeamMembers> members = List.of(member(team), member(team));
        TeamResponse response = TeamMapper.toTeamResponse(team, members);

        org.junit.jupiter.api.Assertions.assertEquals("AI Hackathon 2026", response.getEventName());
        org.junit.jupiter.api.Assertions.assertEquals("AI / ML", response.getCategoryName());
    }

    private Teams team(UUID statusId) {
        Teams team = new Teams();
        team.setTeamId(UUID.randomUUID());
        team.setEventId(UUID.randomUUID());
        team.setCategoryId(UUID.randomUUID());
        team.setTeamName("TeamVN");
        team.setTeamStatusId(statusId);
        team.setLeaderUserId(UUID.randomUUID());
        return team;
    }

    private TeamMembers member(Teams team) {
        TeamMembers member = new TeamMembers();
        member.setTeamMemberId(UUID.randomUUID());
        member.setTeamId(team.getTeamId());
        member.setUserId(UUID.randomUUID());
        member.setActive(true);
        member.setUser(completeUser(member.getUserId()));
        return member;
    }

    private User completeUser(UUID userId) {
        AccountStatus accountStatus = new AccountStatus();
        accountStatus.setStatusName("Active");

        User user = new User();
        user.setUserId(userId);
        user.setFullName("Nam Anh");
        user.setPhone("0814113135");
        user.setUniversityName("FPT University");
        user.setFptStudentCode("SE201169");
        user.setAccountStatus(accountStatus);
        return user;
    }
}
