package io.pathops.controlplane.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.pathops.controlplane.config.RabbitMqEventsConfig;
import io.pathops.controlplane.dto.LoginResult;
import io.pathops.controlplane.dto.events.TenantCreatedEventPayload;
import io.pathops.controlplane.model.OutboxEvent;
import io.pathops.controlplane.model.OutboxEventStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantEventService {

    private static final String TENANT_CREATED = "TENANT_CREATED";
    private static final String TENANT = "TENANT";

    private final ObjectMapper objectMapper;
    private final RabbitMqEventsConfig rabbitMqEventsConfig;
    private final OutboxEventStoreService outboxEventStoreService;

    public void tenantCreated(LoginResult loginResult) {
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
            .ownerIdentity(TenantCreatedEventPayload.OwnerIdentity.builder()
            	    .issuer(loginResult.issuer())
            	    .subject(loginResult.subject())
            	    .preferredUsername(loginResult.preferredUsername())
            	    .email(loginResult.email())
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

        event = outboxEventStoreService.save(event);

        log.info(
            "Created tenant event in outbox. eventId={}, eventType={}, tenantId={}",
            event.getEventId(),
            event.getEventType(),
            loginResult.tenantId()
        );
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize tenant event payload", e);
        }
    }
}