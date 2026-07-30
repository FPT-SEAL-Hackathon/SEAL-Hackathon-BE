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
    // Gui thong bao reject sau commit de member chi nhan tin khi quyet dinh da luu DB.

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
                    "Your team " + event.teamName() + " was rejected by the organizer." + reason
            );
        } catch (Exception exception) {
            // Loi notification chi ghi log; quyet dinh reject team van giu nguyen.
            log.error(
                    "Could not send team registration rejection notification for event {} to users {}",
                    event.eventId(),
                    event.recipientUserIds(),
                    exception
            );
        }
    }
}
