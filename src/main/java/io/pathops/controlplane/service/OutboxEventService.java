package io.pathops.controlplane.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.pathops.controlplane.config.RabbitMqEventsConfig;
import io.pathops.controlplane.dto.LoginResult;
import io.pathops.controlplane.dto.events.TenantCreatedEventPayload;
import io.pathops.controlplane.model.OutboxEvent;
import io.pathops.controlplane.model.OutboxEventStatus;
import io.pathops.controlplane.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private static final String TENANT_CREATED = "TENANT_CREATED";
    private static final String TENANT = "TENANT";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final RabbitMqEventsConfig rabbitMqEventsConfig;

    public OutboxEvent createTenantCreatedEvent(LoginResult loginResult) {
        UUID eventId = UUID.randomUUID();
        Instant occurredAt = Instant.now();

        TenantCreatedEventPayload payload = TenantCreatedEventPayload.builder()
            .eventId(eventId)
            .eventType(TENANT_CREATED)
            .occurredAt(occurredAt)
            .tenant(TenantCreatedEventPayload.TenantInfo.builder()
                .id(loginResult.tenantId())
                .slug(loginResult.tenantSlug())
                .name(loginResult.tenantName())
                .createdAt(occurredAt)
                .build())
            .ownerMembership(TenantCreatedEventPayload.OwnerMembershipInfo.builder()
                .id(loginResult.membershipId())
                .role(loginResult.membershipRole())
                .userId(loginResult.userId())
                .build())
            .build();

        OutboxEvent event = new OutboxEvent();
        event.setEventId(eventId);
        event.setEventType(TENANT_CREATED);
        event.setAggregateType(TENANT);
        event.setAggregateId(String.valueOf(loginResult.tenantId()));
        event.setExchangeName(rabbitMqEventsConfig.getExchangeName());
        event.setRoutingKey(rabbitMqEventsConfig.getTenantCreatedRoutingKey());
        event.setPayload(toJson(payload));
        event.setStatus(OutboxEventStatus.PENDING);
        event.setAttempts(0);

        return outboxEventRepository.save(event);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox event payload", e);
        }
    }
}