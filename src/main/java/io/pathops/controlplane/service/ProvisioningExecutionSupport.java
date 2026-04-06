package io.pathops.controlplane.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;

import io.pathops.controlplane.utils.StringUtils;

@Component
public class ProvisioningExecutionSupport {

    public <T> ProvisioningExecutionOutcome<T> execute(CheckedSupplier<T> supplier) {
        Instant startedAt = Instant.now();

        try {
            T result = supplier.get();
            Instant finishedAt = Instant.now();

            return ProvisioningExecutionOutcome.success(
                result,
                startedAt,
                finishedAt,
                Duration.between(startedAt, finishedAt).toMillis()
            );
        } catch (Exception ex) {
            Instant finishedAt = Instant.now();

            return ProvisioningExecutionOutcome.failure(
                StringUtils.truncateErrorMessage(ex.getMessage()),
                startedAt,
                finishedAt,
                Duration.between(startedAt, finishedAt).toMillis()
            );
        }
    }

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}