package io.pathops.controlplane.dto;

import io.pathops.controlplane.model.ProvisioningScopeType;

public record ClaimedProvisioningJob(
    Long jobId,
    ProvisioningScopeType scopeType,
    Long tenantProvisioningId,
    Long membershipProvisioningId
) {
}