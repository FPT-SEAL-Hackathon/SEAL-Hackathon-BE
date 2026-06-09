package com.fpt.swp.sealhackathonbe.team.service.mapper;

import com.fpt.swp.sealhackathonbe.team.dto.DisqualificationResponse;
import com.fpt.swp.sealhackathonbe.team.dto.JoinTeamRequestResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamMemberDetailResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamMemberResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamResponse;
import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import com.fpt.swp.sealhackathonbe.team.entity.TeamJoinRequests;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamMapper {
    // Mapper chỉ chuyển entity nội bộ sang DTO trả về API, không gọi repository và không chứa nghiệp vụ.
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
                // Mỗi TeamMembers entity chỉ expose các trường cần thiết ra TeamMemberResponse.
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

    public static TeamMemberDetailResponse toTeamMemberDetailResponse(TeamMembers member, User user) {
        // Mapper chi tiết member chỉ lấy thông tin an toàn từ User, tuyệt đối không map passwordHash.
        TeamMemberDetailResponse response = new TeamMemberDetailResponse();
        response.setTeamMemberId(member.getTeamMemberId());
        response.setTeamId(member.getTeamId());
        response.setUserId(user.getUserId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setFptStudentCode(user.getFptStudentCode());
        response.setExternalStudentCode(user.getExternalStudentCode());
        response.setUniversityName(user.getUniversityName());
        response.setUserTypeName(user.getUserType() != null ? user.getUserType().getTypeName() : null);
        response.setAccountStatusName(user.getAccountStatus() != null ? user.getAccountStatus().getStatusName() : null);
        response.setJoinedAt(member.getJoinedAt());
        response.setActive(member.getActive());
        return response;
    }

    public static JoinTeamRequestResponse toJoinTeamRequestResponse(TeamJoinRequests request) {
        // Dữ liệu request join được trả lại sau khi tạo, xem danh sách pending hoặc xử lý APPROVED/REJECTED.
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
        // Response loại team chỉ trả thông tin chính, các trường reverse hiện chưa có API xử lý trong package team.
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
