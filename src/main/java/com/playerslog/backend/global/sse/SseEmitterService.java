package com.playerslog.backend.global.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class SseEmitterService {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1 hour
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long gollId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        this.emitters.computeIfAbsent(gollId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> {
            log.info("SSE connection is completed for goll: {}", gollId);
            removeEmitter(gollId, emitter);
        });

        emitter.onTimeout(() -> {
            log.info("SSE connection has timed out for goll: {}", gollId);
            emitter.complete();
        });

        emitter.onError(throwable -> {
            log.error("SSE connection error for goll: {}", gollId, throwable);
            emitter.complete();
        });

        // Send a dummy event to establish the connection
        sendToEmitter(emitter, "connect", "connected to goll " + gollId);

        return emitter;
    }

    public void sendToGollSubscribers(Long gollId, String eventName, Object data) {
        List<SseEmitter> gollEmitters = emitters.get(gollId);
        if (gollEmitters != null) {
            for (SseEmitter emitter : gollEmitters) {
                sendToEmitter(emitter, eventName, data);
            }
        }
    }

    private void sendToEmitter(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        } catch (IOException e) {
            log.error("Failed to send SSE event for goll", e);
            // No gollId available here, so we can't remove the emitter from the map directly.
            // The onCompletion/onError callbacks will handle the removal.
            emitter.complete();
        }
    }

    private void removeEmitter(Long gollId, SseEmitter emitter) {
        List<SseEmitter> gollEmitters = this.emitters.get(gollId);
        if (gollEmitters != null) {
            gollEmitters.remove(emitter);
            if (gollEmitters.isEmpty()) {
                this.emitters.remove(gollId);
            }
        }
    }
}
