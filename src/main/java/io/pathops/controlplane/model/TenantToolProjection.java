package io.pathops.controlplane.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "tenant_tool_projections")
@Getter
@Setter
@NoArgsConstructor
public class TenantToolProjection extends BaseEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_projection_config_id", nullable = false)
    private ToolProjectionConfig toolProjectionConfig;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ToolProjectionStatus status;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "attempt_count")
    private Integer attemptCount;
    
    @Column(name = "error_message", length = 4000)
    private String errorMessage;
}
