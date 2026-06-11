package com.fpt.swp.sealhackathonbe.notification.service;

import com.fpt.swp.sealhackathonbe.notification.dto.NotificationPushEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationRealtimeService {
    private static final long TIMEOUT = 30L * 60L * 1000L;

    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(error -> removeEmitter(userId, emitter));

        sendEvent(emitter, "connected", "notification-stream-connected");
        return emitter;
    }

    public void publish(UUID userId, NotificationPushEvent event) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            return;
        }

        userEmitters.forEach(emitter -> sendEvent(emitter, "notification", event));
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        } catch (IOException | IllegalStateException e) {
            emitter.completeWithError(e);
        }
    }

    private void removeEmitter(UUID userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }

        userEmitters.remove(emitter);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }
}
