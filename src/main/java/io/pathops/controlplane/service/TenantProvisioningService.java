package io.pathops.controlplane.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pathops.controlplane.integration.jenkins.JenkinsClient;
import io.pathops.controlplane.integration.keycloak.KeycloakAdminClient;
import io.pathops.controlplane.model.SyncStatus;
import io.pathops.controlplane.model.Tenant;
import io.pathops.controlplane.model.TenantProvisioning;
import io.pathops.controlplane.model.TenantProvisioningTool;
import io.pathops.controlplane.repository.TenantProvisioningRepository;
import io.pathops.controlplane.repository.TenantRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantProvisioningService {

    private final TenantRepository tenantRepository;
    private final TenantProvisioningRepository tenantProvisioningRepository;
    private final KeycloakAdminClient keycloakAdminClient;
    private final JenkinsClient jenkinsClient;
    private final ProvisioningJobService provisioningJobService;

    public void enqueueProvisioningForTenant(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        provisioningJobService.enqueueTenantProvisioning(
            ensureProvisioningRow(tenant, TenantProvisioningTool.KEYCLOAK_GROUP)
        );
        provisioningJobService.enqueueTenantProvisioning(
            ensureProvisioningRow(tenant, TenantProvisioningTool.JENKINS_FOLDER)
        );
    }

    public TenantProvisioning ensureProvisioningRow(Tenant tenant, TenantProvisioningTool tool) {
        return tenantProvisioningRepository.findByTenantAndTool(tenant, tool)
            .orElseGet(() -> {
                TenantProvisioning provisioning = new TenantProvisioning();
                provisioning.setTenant(tenant);
                provisioning.setTool(tool);
                provisioning.setStatus(SyncStatus.PENDING);
                return tenantProvisioningRepository.save(provisioning);
            });
    }

    public String execute(TenantProvisioning provisioning) {
        String tenantSlug = provisioning.getTenant().getSlug();

        return switch (provisioning.getTool()) {
            case KEYCLOAK_GROUP -> "group:" + keycloakAdminClient.ensureTenantGroupExists(tenantSlug);
            case JENKINS_FOLDER -> jenkinsClient.ensureTenantFolderExists(tenantSlug);
        };
    }
    
    @Transactional(readOnly = true)
    public TenantProvisioning getById(Long id) {
        return tenantProvisioningRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Tenant provisioning not found: " + id));
    }    
}