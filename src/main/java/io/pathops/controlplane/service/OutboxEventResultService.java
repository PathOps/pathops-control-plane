package io.pathops.controlplane.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.pathops.controlplane.model.OutboxEventStatus;
import io.pathops.controlplane.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxEventResultService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublished(Long outboxEventId) {
        var event = outboxEventRepository.findById(outboxEventId)
            .orElseThrow(() -> new IllegalStateException("Outbox event not found: " + outboxEventId));

        event.setStatus(OutboxEventStatus.PUBLISHED);
        event.setPublishedAt(Instant.now());
        event.setLastError(null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublishFailed(Long outboxEventId, String errorMessage) {
        var event = outboxEventRepository.findById(outboxEventId)
            .orElseThrow(() -> new IllegalStateException("Outbox event not found: " + outboxEventId));

        event.setAttempts(event.getAttempts() + 1);
        event.setLastError(truncate(errorMessage, 4000));
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}