package io.pathops.controlplane.dto;

public enum ProvisioningState {
    NOT_REQUIRED,
    PENDING,
    IN_PROGRESS,
    PARTIAL_FAILURE,
    FAILED,
    COMPLETED
}