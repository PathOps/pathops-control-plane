package io.pathops.controlplane.model;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "provisioning_jobs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class ProvisioningJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false)
    private ProvisioningScopeType scopeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_provisioning_id")
    private TenantProvisioning tenantProvisioning;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_provisioning_id")
    private MembershipProvisioning membershipProvisioning;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProvisioningJobStatus status;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "heartbeat_at")
    private Instant heartbeatAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error_message", length = 4000)
    private String errorMessage;

    @Column(name = "external_ref")
    private String externalRef;

    @Column(name = "claimed_by")
    private String claimedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}