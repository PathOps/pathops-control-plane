package io.pathops.controlplane.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pathops.controlplane.integration.jenkins.JenkinsClient;
import io.pathops.controlplane.integration.keycloak.KeycloakAdminClient;
import io.pathops.controlplane.model.MembershipRole;
import io.pathops.controlplane.model.PathOpsUser;
import io.pathops.controlplane.model.SyncStatus;
import io.pathops.controlplane.model.Tenant;
import io.pathops.controlplane.repository.PathOpsUserRepository;
import io.pathops.controlplane.repository.TenantRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantProvisioningService {

    private final KeycloakAdminClient keycloakAdminClient;
    private final JenkinsClient jenkinsClient;
    private final PathOpsUserRepository pathOpsUserRepository;
    private final TenantRepository tenantRepository;

    @Transactional(noRollbackFor = Exception.class)
    public void provisionTenantForUser(PathOpsUser user, Tenant tenant) {
        provisionKeycloak(user, tenant);
        provisionJenkins(user, tenant);
    }

    private void provisionKeycloak(PathOpsUser user, Tenant tenant) {
        tenant.setKeycloakSyncStatus(SyncStatus.IN_PROGRESS);
        tenant.setKeycloakSyncLastAttemptAt(Instant.now());
        tenant.setKeycloakSyncLastError(null);
        tenantRepository.save(tenant);

        try {
            String keycloakUserId = keycloakAdminClient.resolveUserId(user);
            if (!keycloakUserId.equals(user.getKeycloakUserId())) {
                user.setKeycloakUserId(keycloakUserId);
                pathOpsUserRepository.save(user);
            }

            String groupId = keycloakAdminClient.ensureTenantGroupExists(tenant.getSlug());
            keycloakAdminClient.addUserToTenantGroup(keycloakUserId, groupId);

            tenant.setKeycloakGroupId(groupId);
            tenant.setKeycloakSyncStatus(SyncStatus.SUCCESS);
            tenant.setKeycloakSyncLastError(null);
            tenantRepository.save(tenant);
        } catch (Exception ex) {
            tenant.setKeycloakSyncStatus(SyncStatus.FAILED);
            tenant.setKeycloakSyncLastError(truncate(ex.getMessage()));
            tenantRepository.save(tenant);
        }
    }

    private void provisionJenkins(PathOpsUser user, Tenant tenant) {
        tenant.setJenkinsSyncStatus(SyncStatus.IN_PROGRESS);
        tenant.setJenkinsSyncLastAttemptAt(Instant.now());
        tenant.setJenkinsSyncLastError(null);
        tenantRepository.save(tenant);

        try {
            String folderPath = jenkinsClient.ensureTenantFolderExists(tenant.getSlug());
            jenkinsClient.ensureTenantAccessProjection(tenant.getSlug(), MembershipRole.OWNER);

            tenant.setJenkinsFolderPath(folderPath);
            tenant.setJenkinsSyncStatus(SyncStatus.SUCCESS);
            tenant.setJenkinsSyncLastError(null);
            tenantRepository.save(tenant);
        } catch (Exception ex) {
            tenant.setJenkinsSyncStatus(SyncStatus.FAILED);
            tenant.setJenkinsSyncLastError(truncate(ex.getMessage()));
            tenantRepository.save(tenant);
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 4000 ? value : value.substring(0, 4000);
    }
}