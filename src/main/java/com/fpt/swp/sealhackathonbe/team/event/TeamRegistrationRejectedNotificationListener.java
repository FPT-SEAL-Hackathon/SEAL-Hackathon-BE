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
public class TeamRegistrationRejectedNotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendRejectedNotification(TeamRegistrationRejectedEvent event) {
        try {
            String reason = event.note() == null || event.note().isBlank()
                    ? ""
                    : " Reason: " + event.note().trim();
            notificationService.sendBroadcastNotification(
                    event.recipientUserIds(),
                    event.organizerUserId(),
                    event.eventId(),
                    "Team Registration Rejected",
                    "Your team " + event.teamName()
                            + " was rejected by the organizer. You can create or join another team for this event."
                            + reason
            );
        } catch (Exception exception) {
            log.error(
                    "Could not send team registration rejection notification for event {} to users {}",
                    event.eventId(),
                    event.recipientUserIds(),
                    exception
            );
        }
    }
}
