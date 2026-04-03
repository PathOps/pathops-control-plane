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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenants")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "keycloak_sync_status", nullable = false)
    private SyncStatus keycloakSyncStatus = SyncStatus.PENDING;

    @Column(name = "keycloak_sync_last_attempt_at")
    private Instant keycloakSyncLastAttemptAt;

    @Column(name = "keycloak_sync_last_error", length = 4000)
    private String keycloakSyncLastError;

    @Column(name = "keycloak_group_id")
    private String keycloakGroupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "jenkins_sync_status", nullable = false)
    private SyncStatus jenkinsSyncStatus = SyncStatus.PENDING;

    @Column(name = "jenkins_sync_last_attempt_at")
    private Instant jenkinsSyncLastAttemptAt;

    @Column(name = "jenkins_sync_last_error", length = 4000)
    private String jenkinsSyncLastError;

    @Column(name = "jenkins_folder_path")
    private String jenkinsFolderPath;
}