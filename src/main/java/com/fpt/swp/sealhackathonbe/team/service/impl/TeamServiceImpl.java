package com.fpt.swp.sealhackathonbe.team.service.impl;

import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.team.dto.CreateTeamRequest;
import com.fpt.swp.sealhackathonbe.team.dto.TeamMemberDetailResponse;
import com.fpt.swp.sealhackathonbe.team.dto.TeamResponse;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.team.service.TeamService;
import com.fpt.swp.sealhackathonbe.team.service.mapper.TeamMapper;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
    private static final UUID TEAM_STATUS_FORMING =
            UUID.fromString("60000000-0000-0000-0000-000000000001");
    // Dư thừa hiện tại: chưa có nghiệp vụ nào trong class này chuyển team sang ACTIVE.
    // Giữ comment để khi bổ sung luồng kích hoạt team có thể dùng lại đúng status ID.
    // private static final UUID TEAM_STATUS_ACTIVE =
    //         UUID.fromString("60000000-0000-0000-0000-000000000002");

    private final EventRepository eventRepository;
    private final TeamsRepository teamsRepository;
    private final TeamMembersRepository teamMembersRepository;

    @Override
    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request, UUID currentUserId) {
        // Luồng tạo team: client gửi event/category/name -> kiểm tra event còn hoạt động
        // và cấu hình size -> kiểm tra trùng tên/team active -> lưu Teams -> lưu leader vào TeamMembers -> map ra DTO.
        Event event = getActiveEvent(request.getEventId());
        validateTeamSizeConfig(event);

        if (teamsRepository.existsByEventIdAndTeamName(request.getEventId(), request.getTeamName())) {
            throw new RuntimeException("Team name already exists in this event");
        }

        if (teamMembersRepository.existsByUserIdAndTeam_EventIdAndActiveTrue(currentUserId, event.getEventId())) {
            throw new RuntimeException("User already belongs to an active team in this event");
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
    @Transactional(readOnly = true)
    public TeamResponse getMyTeam(UUID currentUserId) {
        // Luồng xem team của tôi: userId -> TeamMembers active -> Teams -> danh sách member active -> TeamResponse.
        TeamMembers member = teamMembersRepository.findByUserIdAndActiveTrue(currentUserId)
                .orElseThrow(() -> new RuntimeException("User does not belong to any active team"));

        Teams team = member.getTeam();

        List<TeamMembers> members = teamMembersRepository.findByTeamIdAndActiveTrue(team.getTeamId());
        return TeamMapper.toTeamResponse(team, members);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getByEventId(UUID eventId) {
        return teamsRepository.findByEventId(eventId)
                .stream()
                .map(team -> {
                    List<TeamMembers> members =
                            teamMembersRepository.findByTeamIdAndActiveTrue(team.getTeamId());
                    return TeamMapper.toTeamResponse(team, members);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getById(UUID teamId) {
        // Luồng xem team theo ID: teamId -> Teams -> danh sách member active -> TeamResponse.
        Teams team = teamsRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        List<TeamMembers> members = teamMembersRepository.findByTeamIdAndActiveTrue(team.getTeamId());
        return TeamMapper.toTeamResponse(team, members);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamMemberDetailResponse getTeamMemberDetail(UUID teamId, UUID userId, UUID currentUserId) {
        // Luồng xem chi tiết member: xác nhận user đang active trong team -> lấy hồ sơ User
        // -> mapper ghép dữ liệu TeamMembers + User thành DTO, không trả passwordHash.
        teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, currentUserId)
                .orElseThrow(() -> new RuntimeException("You do not belong to this team"));

        TeamMembers member = teamMembersRepository.findByTeamIdAndUserIdAndActiveTrue(teamId, userId)
                .orElseThrow(() -> new RuntimeException("Active team member not found"));

        User user = member.getUser();

        return TeamMapper.toTeamMemberDetailResponse(member, user);
    }

    @Override
    @Transactional
    public void removeMember(UUID userId, UUID currentUserId) {
        // Luồng rời/kick member: tìm membership active -> kiểm tra quyền leader hoặc tự rời
        // -> kiểm tra MinTeamSize của event -> đánh dấu inactive, không xóa cứng.
        TeamMembers member = teamMembersRepository.findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("Active team member not found"));

        Teams team = member.getTeam();

        boolean isLeader = team.getLeaderUserId().equals(currentUserId);
        boolean isSelfLeaving = userId.equals(currentUserId);

        if (!isLeader && !isSelfLeaving) {
            throw new RuntimeException("You do not have permission to remove this member");
        }

        if (team.getLeaderUserId().equals(userId)) {
            throw new RuntimeException("Team leader cannot be removed");
        }

        validateTeamWillNotBeBelowMinimum(team);

        member.setActive(false);
        member.setLeftAt(LocalDateTime.now());

        teamMembersRepository.save(member);
    }

    private Event getActiveEvent(UUID eventId) {
        // Team chi duoc tao trong event ton tai va chua bi soft delete.
        return eventRepository.findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    private void validateTeamSizeConfig(Event event) {
        // Chan cau hinh event khong hop le truoc khi ap dung gioi han thanh vien.
        Integer minTeamSize = event.getMinTeamSize();
        Integer maxTeamSize = event.getMaxTeamSize();

        if (minTeamSize != null && minTeamSize < 1) {
            throw new RuntimeException("Minimum team size must be at least 1");
        }

        if (maxTeamSize != null && maxTeamSize < 1) {
            throw new RuntimeException("Maximum team size must be at least 1");
        }

        if (minTeamSize != null && maxTeamSize != null && minTeamSize > maxTeamSize) {
            throw new RuntimeException("Minimum team size cannot be greater than maximum team size");
        }
    }

    private void validateTeamWillNotBeBelowMinimum(Teams team) {
        // MinTeamSize nằm ở Event, nên cần đi từ team -> event để kiểm tra trước khi xóa/rời member.
        Event event = requireActiveEvent(team.getEvent());
        validateTeamSizeConfig(event);

        Integer minTeamSize = event.getMinTeamSize();
        long activeMemberCount = teamMembersRepository.countByTeamIdAndActiveTrue(team.getTeamId());

        if (minTeamSize != null && activeMemberCount - 1 < minTeamSize) {
            throw new RuntimeException("Cannot remove member because team would be below minimum size");
        }
    }

    private Event requireActiveEvent(Event event) {
        // Quan he lazy co the null; nghiep vu team khong xu ly event da bi soft delete.
        if (event == null || Boolean.TRUE.equals(event.getIsDeleted())) {
            throw new RuntimeException("Event not found");
        }

        return event;
    }
}
