package io.pathops.controlplane.model;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED
}