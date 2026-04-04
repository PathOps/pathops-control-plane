package io.pathops.controlplane.service;

import io.pathops.controlplane.model.ProvisioningTool;

public record ProvisioningExecutionResult(
    ProvisioningTool tool,
    boolean success,
    String externalRef,
    String resolvedUserId,
    String errorMessage
) {
}