package io.pathops.controlplane.service;

import org.springframework.stereotype.Service;

import io.pathops.controlplane.dto.LoginResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final OutboxEventService outboxEventService;

    public void publishTenantCreatedEvent(LoginResult loginResult) {
        var event = outboxEventService.createTenantCreatedEvent(loginResult);

        log.info(
            "Created outbox event. eventId={}, eventType={}, aggregateType={}, aggregateId={}",
            event.getEventId(),
            event.getEventType(),
            event.getAggregateType(),
            event.getAggregateId()
        );
    }
}