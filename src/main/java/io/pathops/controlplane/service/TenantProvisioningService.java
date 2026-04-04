package io.pathops.controlplane.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.pathops.controlplane.integration.jenkins.JenkinsClient;
import io.pathops.controlplane.integration.keycloak.KeycloakAdminClient;
import io.pathops.controlplane.model.MembershipRole;
import io.pathops.controlplane.model.PathOpsUser;
import io.pathops.controlplane.model.ProvisioningTool;
import io.pathops.controlplane.model.SyncStatus;
import io.pathops.controlplane.model.Tenant;
import io.pathops.controlplane.model.TenantProvisioning;
import io.pathops.controlplane.repository.PathOpsUserRepository;
import io.pathops.controlplane.repository.TenantProvisioningRepository;
import io.pathops.controlplane.repository.TenantRepository;
import io.pathops.controlplane.utils.StringUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantProvisioningService {

    private final KeycloakAdminClient keycloakAdminClient;
    private final JenkinsClient jenkinsClient;
    private final PathOpsUserRepository userRepository;
    private final TenantProvisioningRepository provisioningRepository;
    private final TenantRepository tenantRepository;

    public void provisionTenantForUser(PathOpsUser user, Tenant tenant) {
    	// Http requests
        ProvisioningExecutionResult keycloakResult = executeKeycloakProvisioning(user, tenant);
        ProvisioningExecutionResult jenkinsResult = executeJenkinsProvisioning(tenant);
        
        List<ProvisioningExecutionResult> results = List.of(keycloakResult, jenkinsResult);
        
        // Database new transaction
        persistProvisioningsInNewTx(user.getId(), tenant.getId(), results);
    }

    private ProvisioningExecutionResult executeKeycloakProvisioning(PathOpsUser user, Tenant tenant) {
        try {
            String userId = keycloakAdminClient.resolveUserId(user);
            String groupId = keycloakAdminClient.ensureTenantGroupExists(tenant.getSlug());
            keycloakAdminClient.addUserToTenantGroup(userId, groupId);

            return new ProvisioningExecutionResult(
            	ProvisioningTool.KEYCLOAK,
                true,
                groupId,
                userId,
                null
            );
        } catch (Exception ex) {
            return new ProvisioningExecutionResult(
            	ProvisioningTool.KEYCLOAK,
                false,
                null,
                null,
                StringUtils.truncateErrorMessage(ex.getMessage())
            );
        }
    }

    private ProvisioningExecutionResult executeJenkinsProvisioning(Tenant tenant) {
        try {
            String folder = jenkinsClient.ensureTenantFolderExists(tenant.getSlug());
            jenkinsClient.ensureTenantAccessProjection(tenant.getSlug(), MembershipRole.OWNER);

            return new ProvisioningExecutionResult(
            	ProvisioningTool.JENKINS,
                true,
                folder,
                null,
                null
            );
        } catch (Exception ex) {
            return new ProvisioningExecutionResult(
            	ProvisioningTool.JENKINS,
                false,
                null,
                null,
                StringUtils.truncateErrorMessage(ex.getMessage())
            );
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void persistProvisioningsInNewTx(
        Long userId,
        Long tenantId,
        List<ProvisioningExecutionResult> results
    ) {
        PathOpsUser managedUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("PathOpsUser not found: " + userId));

        Tenant managedTenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantId));

        for (ProvisioningExecutionResult result : results) {
        	if (result.success() 
        		&& result.tool() == ProvisioningTool.KEYCLOAK
                && result.resolvedUserId() != null
                && !result.resolvedUserId().equals(managedUser.getKeycloakUserId())) {
        		
        		managedUser.setKeycloakUserId(result.resolvedUserId());
                userRepository.save(managedUser);
        	}
        	persistProvisioning(managedTenant, result);
        }
    }
    

    public void persistProvisioning(
        Tenant tenant,
        ProvisioningExecutionResult result
    ) {
  
        TenantProvisioning tp = provisioningRepository.findByTenantAndTool(tenant, result.tool())
            .orElseGet(() -> {
                TenantProvisioning p = new TenantProvisioning();
                p.setTenant(tenant);
                p.setTool(result.tool());
                return p;
            });

        tp.setLastAttemptAt(Instant.now());

        if (result.success()) {
            tp.setStatus(SyncStatus.SUCCESS);
            tp.setLastError(null);
            tp.setExternalRef(result.externalRef());
        } else {
            tp.setStatus(SyncStatus.FAILED);
            tp.setLastError(result.errorMessage());
        }

        provisioningRepository.save(tp);
    }
}