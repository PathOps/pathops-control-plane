package io.pathops.controlplane.dto;

public record MembershipProvisioningExecutionResult(
    String externalRef,
    String resolvedUserId
) {
}