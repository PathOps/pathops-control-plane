package io.pathops.controlplane.dto.events;

import java.time.Instant;
import java.util.UUID;

import io.pathops.controlplane.model.MembershipRole;
import lombok.Builder;

@Builder
public record TenantCreatedEventPayload(
    UUID eventId,
    String eventType,
    Instant occurredAt,
    TenantInfo tenant,
    OwnerMembershipInfo ownerMembership,
    OwnerIdentity ownerIdentity
) {
    @Builder
    public record TenantInfo(
        Long id,
        String slug,
        String name,
        Instant createdAt
    ) {
    }

    @Builder
    public record OwnerMembershipInfo(
        Long id,
        MembershipRole role,
        Long userId
    ) {
    }
    @Builder
    public record OwnerIdentity(
    	String issuer,
    	String subject,
    	String preferredUsername,
    	String email
    ) {
    }
}