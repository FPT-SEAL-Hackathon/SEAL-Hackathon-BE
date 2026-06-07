package com.fpt.swp.sealhackathonbe.team.service;

import com.fpt.swp.sealhackathonbe.team.dto.DisqualificationResponse;
import com.fpt.swp.sealhackathonbe.team.dto.JoinTeamRequestResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamMemberResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamResponse;
import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import com.fpt.swp.sealhackathonbe.team.entity.TeamJoinRequests;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;

import java.util.Collections;
import java.util.List;

public class TeamMapper {
    private TeamMapper() {
    }

    public static TeamResponse toTeamResponse(Teams team, List<TeamMembers> members) {
        TeamResponse response = new TeamResponse();
        response.setTeamId(team.getTeamId());
        response.setEventId(team.getEventId());
        response.setCategoryId(team.getCategoryId());
        response.setTeamName(team.getTeamName());
        response.setTeamStatusId(team.getTeamStatusId());
        response.setLeaderUserId(team.getLeaderUserId());
        response.setCreatedAt(team.getCreatedAt());
        response.setUpdatedAt(team.getUpdatedAt());

        List<TeamMemberResponse> memberResponses = (members == null ? Collections.<TeamMembers>emptyList() : members)
                .stream()
                .map(TeamMapper::toTeamMemberResponse)
                .toList();

        response.setMembers(memberResponses);
        return response;
    }

    public static TeamMemberResponse toTeamMemberResponse(TeamMembers member) {
        TeamMemberResponse response = new TeamMemberResponse();
        response.setTeamMemberId(member.getTeamMemberId());
        response.setUserId(member.getUserId());
        response.setJoinedAt(member.getJoinedAt());
        response.setActive(member.getActive());
        return response;
    }

    public static JoinTeamRequestResponse toJoinTeamRequestResponse(TeamJoinRequests request) {
        JoinTeamRequestResponse response = new JoinTeamRequestResponse();
        response.setRequestId(request.getRequestId());
        response.setTeamId(request.getTeamId());
        response.setUserId(request.getUserId());
        response.setRequestStatus(request.getRequestStatus());
        response.setRequestedAt(request.getRequestedAt());
        response.setRespondedAt(request.getRespondedAt());
        response.setRespondedById(request.getRespondedById());
        response.setResponseNote(request.getResponseNote());
        return response;
    }

    public static DisqualificationResponse toDisqualificationResponse(Disqualifications disqualification) {
        DisqualificationResponse response = new DisqualificationResponse();
        response.setDisqualificationId(disqualification.getDisqualificationId());
        response.setTeamId(disqualification.getTeamId());
        response.setReason(disqualification.getReason());
        response.setDisqualifiedById(disqualification.getDisqualifiedById());
        response.setDisqualifiedAt(disqualification.getDisqualifiedAt());
        response.setReversed(disqualification.getReversed());
        return response;
    }
}
