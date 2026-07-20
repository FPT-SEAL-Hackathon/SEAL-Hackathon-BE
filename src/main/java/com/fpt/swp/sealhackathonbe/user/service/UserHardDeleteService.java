package com.fpt.swp.sealhackathonbe.user.service;

import com.fpt.swp.sealhackathonbe.auth.entity.AuditLog;
import com.fpt.swp.sealhackathonbe.auth.repository.AccountLinkTicketRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.AuditLogRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.PasswordResetTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.repository.VerificationTokenRepository;
import com.fpt.swp.sealhackathonbe.award.repository.AwardRepository;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryMentorRepository;
import com.fpt.swp.sealhackathonbe.consultation.repository.ConsultationMessageRepository;
import com.fpt.swp.sealhackathonbe.consultation.repository.ConsultationRequestRepository;
import com.fpt.swp.sealhackathonbe.core.exception.BadRequestException;
import com.fpt.swp.sealhackathonbe.core.exception.BusinessConflictException;
import com.fpt.swp.sealhackathonbe.criteria.repository.CriterionTemplateRepository;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.eventparticipant.repository.EventParticipantRepository;
import com.fpt.swp.sealhackathonbe.judging.repository.EvaluationAuditLogRepository;
import com.fpt.swp.sealhackathonbe.notification.Repository.NotificationRepository;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import com.fpt.swp.sealhackathonbe.research.repository.DataExportLogRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamJoinRequestsRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMilestoneRepository;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.user.entity.DeletedUserTombstone;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.DeletedUserTombstoneRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserOAuthAccountRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import com.fpt.swp.sealhackathonbe.team.repository.DisqualificationsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Xóa CỨNG tài khoản theo email (organizer, phục vụ giai đoạn phát triển):
 * xóa mọi tài khoản trùng email cùng dữ liệu cá nhân, GIỮ dữ liệu tập thể
 * (submission gán lại cho leader, audit log gỡ actor), thông báo cho team
 * còn lại và ghi tombstone 7 ngày để login cũ nhận thông báo tạo tài khoản mới.
 * Email được phép đăng ký lại NGAY sau khi xóa.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserHardDeleteService {

    public static final String DEFAULT_REASON = "Removed during development";
    private static final int TOMBSTONE_RETENTION_DAYS = 7;
    // Khớp cột DeletedUserTombstones.Reason nvarchar(500) — chặn tràn cột (500 rollback).
    private static final int MAX_REASON_LENGTH = 500;
    private static final String ROLE_ORGANIZER = "Organizer";
    private static final String ROLE_ADMIN = "Admin";

    private final UserRepository userRepository;
    private final DeletedUserTombstoneRepository tombstoneRepository;
    private final AuditLogRepository auditLogRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AccountLinkTicketRepository accountLinkTicketRepository;
    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final NotificationRepository notificationRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final TeamsRepository teamsRepository;
    private final TeamJoinRequestsRepository teamJoinRequestsRepository;
    private final EventParticipantRepository eventParticipantRepository;
    private final SubmissionsRepository submissionsRepository;
    private final ConsultationRequestRepository consultationRequestRepository;
    private final ConsultationMessageRepository consultationMessageRepository;
    private final EventRepository eventRepository;
    private final AwardRepository awardRepository;
    private final DisqualificationsRepository disqualificationsRepository;
    private final EvaluationAuditLogRepository evaluationAuditLogRepository;
    private final RoundJudgeRepository roundJudgeRepository;
    private final CategoryMentorRepository categoryMentorRepository;
    private final CriterionTemplateRepository criterionTemplateRepository;
    private final DataExportLogRepository dataExportLogRepository;
    private final TeamMilestoneRepository teamMilestoneRepository;
    private final NotificationService notificationService;

    /**
     * Xóa cứng mọi tài khoản trùng email. Trả về số tài khoản đã xóa.
     */
    @Transactional
    public int hardDeleteByEmail(String email, UUID actorUserId, String reason) {
        String normalizedEmail = email == null ? "" : email.trim();
        if (normalizedEmail.isBlank()) {
            throw new BadRequestException("Email is required.");
        }

        if (reason != null && reason.length() > MAX_REASON_LENGTH) {
            throw new BadRequestException("Reason must not exceed " + MAX_REASON_LENGTH + " characters.");
        }

        List<User> accounts = userRepository.findAllByEmailIgnoreCase(normalizedEmail);
        if (accounts.isEmpty()) {
            throw new EntityNotFoundException("No account found for email " + normalizedEmail);
        }

        User actor = userRepository.findByUserIdAndIsDeletedFalse(actorUserId)
                .orElseThrow(() -> new EntityNotFoundException("Acting organizer not found"));

        for (User account : accounts) {
            validateDeletable(account, actorUserId);
        }

        String resolvedReason = reason == null || reason.isBlank() ? DEFAULT_REASON : reason.trim();

        for (User account : accounts) {
            notifyTeamsAndTransferLeadership(account, actor, resolvedReason);
            reassignSubmissionsToTeamLeader(account);
            deletePersonalData(account.getUserId(), actor);
        }

        for (User account : accounts) {
            String snapshot = snapshot(account);
            userRepository.delete(account);
            writeAudit(account.getUserId(), actorUserId, snapshot, resolvedReason);
        }

        LocalDateTime now = LocalDateTime.now();
        tombstoneRepository.save(DeletedUserTombstone.builder()
                .email(normalizedEmail)
                .fullName(accounts.get(0).getFullName())
                .deletedByUserId(actorUserId)
                .reason(resolvedReason)
                .deletedAt(now)
                .expiresAt(now.plusDays(TOMBSTONE_RETENTION_DAYS))
                .build());

        return accounts.size();
    }

    private void validateDeletable(User account, UUID actorUserId) {
        if (account.getUserId().equals(actorUserId)) {
            throw new AccessDeniedException("Organizer cannot hard-delete their own account");
        }

        String roleName = account.getUserType() != null ? account.getUserType().getTypeName() : null;
        if (ROLE_ORGANIZER.equalsIgnoreCase(roleName) || ROLE_ADMIN.equalsIgnoreCase(roleName)) {
            throw new AccessDeniedException("Organizer and Admin accounts cannot be hard-deleted");
        }

        // Dữ liệu "staff" (event/giải thưởng/chấm điểm/mentor...) không thể xóa
        // hay gán lại an toàn — yêu cầu xử lý thủ công trước, tránh phá dữ liệu tập thể.
        List<String> blockers = collectBlockers(account.getUserId());
        if (!blockers.isEmpty()) {
            throw new BusinessConflictException(
                    "Cannot hard-delete " + account.getEmail() + ": user still owns "
                            + String.join(", ", blockers)
                            + ". Reassign or remove this data first.");
        }
    }

    private List<String> collectBlockers(UUID userId) {
        List<String> blockers = new ArrayList<>();
        if (eventRepository.existsByCreatedBy_UserId(userId)) {
            blockers.add("events created");
        }
        if (awardRepository.existsByAwardedBy_UserId(userId)) {
            blockers.add("awards granted");
        }
        if (disqualificationsRepository.existsByDisqualifiedBy_UserId(userId)
                || disqualificationsRepository.existsByReversedBy_UserId(userId)) {
            blockers.add("disqualification decisions");
        }
        if (evaluationAuditLogRepository.existsByActor_UserId(userId)) {
            blockers.add("evaluation audit logs");
        }
        if (roundJudgeRepository.existsByJudge_UserId(userId)
                || roundJudgeRepository.existsByAssignedBy_UserId(userId)) {
            blockers.add("round judge assignments");
        }
        if (categoryMentorRepository.existsByMentor_UserId(userId)) {
            blockers.add("category mentor assignments");
        }

        if (teamMilestoneRepository.existsByMentorUserId(userId)) {
            blockers.add("team milestones as mentor");
        }
        if (criterionTemplateRepository.existsByCreatedBy_UserId(userId)) {
            blockers.add("criterion templates created");
        }
        if (dataExportLogRepository.existsByExportedBy_UserId(userId)) {
            blockers.add("data export logs");
        }
        if (userRepository.countCalibrationSamplesAddedBy(userId) > 0) {
            blockers.add("calibration samples added");
        }
        return blockers;
    }

    /**
     * Với mỗi team user còn active: thông báo các thành viên còn lại;
     * nếu user là leader thì chuyển quyền cho thành viên active sớm nhất,
     * hoặc xóa team nếu không còn ai (cùng logic với xóa team FORMING rỗng).
     */
    private void notifyTeamsAndTransferLeadership(User account, User actor, String reason) {
        List<TeamMembers> memberships =
                teamMembersRepository.findAllByUserIdAndActiveTrue(account.getUserId());

        for (TeamMembers membership : memberships) {
            Teams team = teamsRepository.findByIdForUpdate(membership.getTeamId()).orElse(null);
            if (team == null) {
                continue;
            }

            List<TeamMembers> remaining = teamMembersRepository
                    .findByTeamIdAndActiveTrueOrderByJoinedAtAscTeamMemberIdAsc(team.getTeamId())
                    .stream()
                    .filter(member -> !member.getUserId().equals(account.getUserId()))
                    .toList();

            if (team.getLeaderUserId().equals(account.getUserId())) {
                if (remaining.isEmpty()) {
                    teamJoinRequestsRepository.deleteByTeamId(team.getTeamId());
                    teamMembersRepository.deleteByTeamId(team.getTeamId());
                    teamsRepository.delete(team);
                    continue;
                }
                team.setLeaderUserId(remaining.get(0).getUserId());
                team.setUpdatedAt(LocalDateTime.now());
                teamsRepository.save(team);
            }

            for (TeamMembers member : remaining) {
                notifyAfterCommit(
                        member.getUserId(),
                        actor.getUserId(),
                        team.getEventId(),
                        "Member Removed From System",
                        "Thành viên " + account.getFullName()
                                + " đã bị gỡ hoàn toàn khỏi hệ thống (" + reason + ").");
            }
        }
    }

    // Notification (persist + bắn realtime SSE) chỉ gửi SAU khi transaction xóa
    // commit thành công: lỗi publish không làm rollback cả cụm xóa, và team
    // không nhận thông báo "đã gỡ thành viên" khi việc xóa thực ra bị rollback.
    private void notifyAfterCommit(UUID recipientId, UUID actorId, UUID eventId, String title, String body) {
        Runnable send = () -> {
            try {
                notificationService.sendNotification(recipientId, actorId, eventId, title, body);
            } catch (RuntimeException ex) {
                log.warn("Failed to send hard-delete notification to {}", recipientId, ex);
            }
        };
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            send.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                send.run();
            }
        });
    }

    /**
     * Submission thuộc về team nên được giữ lại: gán lại người nộp sang
     * leader hiện tại của team (đã chuyển quyền trước đó nếu cần).
     */
    private void reassignSubmissionsToTeamLeader(User account) {
        List<Submissions> submissions =
                submissionsRepository.findBySubmittedByUserId(account.getUserId());

        for (Submissions submission : submissions) {
            UUID newOwner = teamsRepository.findById(submission.getTeamId())
                    .map(Teams::getLeaderUserId)
                    .orElse(null);
            if (newOwner == null || newOwner.equals(account.getUserId())) {
                throw new BusinessConflictException(
                        "Cannot hard-delete " + account.getEmail()
                                + ": a team submission has no remaining leader to take ownership.");
            }
            submission.setSubmittedByUserId(newOwner);
        }
        submissionsRepository.saveAll(submissions);
    }

    private void deletePersonalData(UUID userId, User actor) {
        refreshTokenRepository.deleteByUser_UserId(userId);
        verificationTokenRepository.deleteByUser_UserId(userId);
        passwordResetTokenRepository.deleteByUser_UserId(userId);
        accountLinkTicketRepository.deleteByUser_UserId(userId);
        userOAuthAccountRepository.deleteByUser_UserId(userId);

        notificationRepository.deleteByRecipientUserID_UserId(userId);
        notificationRepository.reassignSender(userId, actor);

        teamJoinRequestsRepository.clearRespondedBy(userId);
        teamJoinRequestsRepository.deleteByUserId(userId);
        teamMembersRepository.deleteByUserId(userId);

        eventParticipantRepository.clearApprovedBy(userId);
        eventParticipantRepository.deleteByUserId(userId);

        // Tin nhắn user gửi trong thread người khác xóa trước; các thread do
        // user tạo xóa sau (ConsultationMessages theo cascade FK trong DB).
        consultationMessageRepository.deleteBySender_UserId(userId);
        consultationRequestRepository.deleteByCreatedBy_UserId(userId);

        auditLogRepository.clearActor(userId);
        userRepository.clearApprovedBy(userId);
    }

    private String snapshot(User user) {
        return "{\"email\":\"" + user.getEmail()
                + "\",\"role\":\"" + (user.getUserType() != null ? user.getUserType().getTypeName() : null)
                + "\",\"status\":\""
                + (user.getAccountStatus() != null ? user.getAccountStatus().getStatusName() : null)
                + "\",\"isDeleted\":" + user.getIsDeleted() + "}";
    }

    private void writeAudit(UUID userId, UUID actorUserId, String oldValue, String reason) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActionType("USER_HARD_DELETED");
        auditLog.setEntityType("Users");
        auditLog.setEntityId(userId);
        auditLog.setActorUserId(actorUserId);
        auditLog.setOldValueJson(oldValue);
        auditLog.setNotes(reason);
        auditLog.setOccurredAt(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }
}
