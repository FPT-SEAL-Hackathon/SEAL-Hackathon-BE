package com.fpt.swp.sealhackathonbe.event.scheduler;

import com.fpt.swp.sealhackathonbe.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventStatusScheduler {
    private final EventService eventService;

    @Scheduled(cron = "0 * * * * *") //Run every one minute
    public void updateStatuses() {
        eventService.updateEventStatuses();
    }

}
