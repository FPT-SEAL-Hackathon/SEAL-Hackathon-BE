package com.fpt.swp.sealhackathonbe.team.event;

import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeamJoinApprovedNotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendApprovalNotification(TeamJoinApprovedEvent event) {
        try {
            notificationService.sendNotification(
                    event.recipientUserId(),
                    event.leaderUserId(),
                    event.eventId(),
                    "Team Join Request Approved",
                    "Your request to join team " + event.teamName() + " has been approved."
            );
        } catch (Exception exception) {
            log.error(
                    "Could not send team join approval notification to user {} for event {}",
                    event.recipientUserId(),
                    event.eventId(),
                    exception
            );
        }
    }
}
