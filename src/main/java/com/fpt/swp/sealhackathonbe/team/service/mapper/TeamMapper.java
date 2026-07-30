package com.fpt.swp.sealhackathonbe.team.service.mapper;

import com.fpt.swp.sealhackathonbe.core.constant.TeamStatusConstants;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualificationResponse;
import com.fpt.swp.sealhackathonbe.team.dto.DisqualifiedTeamResponse;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TeamMapper {
    // Mapper chỉ chuyển entity nội bộ sang DTO trả về API, không gọi repository và không chứa nghiệp vụ.
    public static TeamResponse toTeamResponse(Teams team, List<TeamMembers> members) {
        Integer minTeamSize = team.getEvent() != null ? team.getEvent().getMinTeamSize() : null;
        Integer maxTeamSize = team.getEvent() != null ? team.getEvent().getMaxTeamSize() : null;
        return toTeamResponse(team, members, minTeamSize, maxTeamSize);
    }

    public static TeamResponse toTeamResponse(
            Teams team,
            List<TeamMembers> members,
            Integer minTeamSize,
            Integer maxTeamSize
    ) {
        TeamResponse response = new TeamResponse();
        response.setTeamId(team.getTeamId());
        response.setEventId(team.getEventId());
        response.setEventName(team.getEvent() != null ? team.getEvent().getEventName() : null);
        response.setCategoryId(team.getCategoryId());
        response.setCategoryName(team.getCategory() != null ? team.getCategory().getCategoryName() : null);
        response.setTeamName(team.getTeamName());
        response.setTeamStatusId(team.getTeamStatusId());
        response.setTeamStatusName(team.getTeamStatus() != null ? team.getTeamStatus().getStatusName() : null);
        response.setLeaderUserId(team.getLeaderUserId());
        response.setCreatedAt(team.getCreatedAt());
        response.setUpdatedAt(team.getUpdatedAt());

        List<TeamMemberResponse> memberResponses = (members == null ? Collections.<TeamMembers>emptyList() : members)
                .stream()
                // Mỗi TeamMembers entity chỉ expose các trường cần thiết ra TeamMemberResponse.
                .map(TeamMapper::toTeamMemberResponse)
                .toList();

        response.setMembers(memberResponses);
        long activeMemberCount = memberResponses.size();
        boolean minOk = minTeamSize == null || activeMemberCount >= minTeamSize;
        boolean maxOk = maxTeamSize == null || activeMemberCount <= maxTeamSize;
        List<String> approvalIssues = buildApprovalIssues(team, members, activeMemberCount, minTeamSize, maxTeamSize);
        boolean membersInfoComplete = membersInfoComplete(members);

        response.setMinTeamSize(minTeamSize);
        response.setMaxTeamSize(maxTeamSize);
        response.setActiveMemberCount(activeMemberCount);
        response.setTeamSizeEligible(minOk && maxOk);
        response.setMembersInfoComplete(membersInfoComplete);
        response.setApprovalIssues(approvalIssues);
        response.setCanRequestApproval(isForming(team.getTeamStatusId()) && minOk && maxOk && membersInfoComplete);
        return response;
    }

    private static boolean isForming(UUID teamStatusId) {
        return TeamStatusConstants.FORMING.equals(teamStatusId);
    }

    private static List<String> buildApprovalIssues(
            Teams team,
            List<TeamMembers> members,
            long activeMemberCount,
            Integer minTeamSize,
            Integer maxTeamSize
    ) {
        List<String> issues = new ArrayList<>();

        if (minTeamSize != null && activeMemberCount < minTeamSize) {
            issues.add("Team has fewer active members than the event minimum");
        }
        if (maxTeamSize != null && activeMemberCount > maxTeamSize) {
            issues.add("Team has more active members than the event maximum");
        }
        if (!membersInfoComplete(members)) {
            issues.add("One or more members have incomplete profile information");
        }
        return issues;
    }

    private static boolean membersInfoComplete(List<TeamMembers> members) {
        return (members == null ? Collections.<TeamMembers>emptyList() : members)
                .stream()
                .allMatch(member -> isProfileComplete(member.getUser()));
    }

    private static boolean isProfileComplete(User user) {
        if (user == null) {
            return false;
        }

        String accountStatusName = user.getAccountStatus() != null
                ? user.getAccountStatus().getStatusName()
                : null;

        boolean hasFptCode = !isBlank(user.getFptStudentCode());
        boolean hasExternalCode = !isBlank(user.getExternalStudentCode());
        boolean hasUniversity = !isBlank(user.getUniversityName());

        boolean studentInfoValid = (hasFptCode) || (hasExternalCode && hasUniversity);

        return !isBlank(user.getFullName())
                && hasValidPhoneLength(user.getPhone())
                && studentInfoValid
                && "Active".equalsIgnoreCase(accountStatusName);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean hasValidPhoneLength(String phone) {
        if (isBlank(phone)) {
            return false;
        }
        String digits = phone.replaceAll("\\D", "");
        return digits.length() >= 9 && digits.length() <= 15;
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
        User user = request.getUser();
        response.setFullName(user != null ? user.getFullName() : null);
        response.setUniversityName(user != null ? user.getUniversityName() : null);
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

    public static DisqualifiedTeamResponse toDisqualifiedTeamResponse(Disqualifications disqualification) {
        DisqualifiedTeamResponse response = new DisqualifiedTeamResponse();
        response.setDisqualificationId(disqualification.getDisqualificationId());
        response.setTeamId(disqualification.getTeamId());
        response.setReason(disqualification.getReason());
        response.setDisqualifiedById(disqualification.getDisqualifiedById());
        response.setDisqualifiedAt(disqualification.getDisqualifiedAt());
        return response;
    }
}
