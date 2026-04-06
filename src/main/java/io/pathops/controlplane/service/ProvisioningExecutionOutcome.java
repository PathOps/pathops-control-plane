package io.pathops.controlplane.service;

import java.time.Instant;

public record ProvisioningExecutionOutcome<T>(
    boolean success,
    T result,
    String errorMessage,
    Instant startedAt,
    Instant finishedAt,
    long durationMs
) {
    public static <T> ProvisioningExecutionOutcome<T> success(
        T result,
        Instant startedAt,
        Instant finishedAt,
        long durationMs
    ) {
        return new ProvisioningExecutionOutcome<>(
            true,
            result,
            null,
            startedAt,
            finishedAt,
            durationMs
        );
    }

    public static <T> ProvisioningExecutionOutcome<T> failure(
        String errorMessage,
        Instant startedAt,
        Instant finishedAt,
        long durationMs
    ) {
        return new ProvisioningExecutionOutcome<>(
            false,
            null,
            errorMessage,
            startedAt,
            finishedAt,
            durationMs
        );
    }
}