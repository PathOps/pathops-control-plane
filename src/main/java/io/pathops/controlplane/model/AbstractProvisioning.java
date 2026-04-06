package io.pathops.controlplane.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractProvisioning {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SyncStatus status = SyncStatus.PENDING;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "last_duration_ms")
    private Long lastDurationMs;

    @Column(name = "last_error", length = 4000)
    private String lastError;

    @Column(name = "external_ref")
    private String externalRef;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}