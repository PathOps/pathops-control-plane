package io.pathops.controlplane.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pathops.controlplane.model.MembershipRole;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProvisioningService {

    private final TenantProvisioningService tenantProvisioningService;
    private final MembershipProvisioningService membershipProvisioningService;

    public void enqueueForLogin(Long userId, Long tenantId, MembershipRole membershipRole) {
        tenantProvisioningService.enqueueProvisioningForTenant(tenantId);

        if (membershipRole == MembershipRole.OWNER) {
            membershipProvisioningService.enqueueOwnerProvisioningForUser(userId, tenantId);
        }
    }
}