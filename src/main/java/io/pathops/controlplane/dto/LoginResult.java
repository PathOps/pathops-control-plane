package io.pathops.controlplane.dto;

import io.pathops.controlplane.model.MembershipRole;
import lombok.Builder;

@Builder
public record LoginResult(
    Long userId,
    Long tenantId,
    String tenantName,
    String tenantSlug,
    MembershipRole membershipRole
) {
}