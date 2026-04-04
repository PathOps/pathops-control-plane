package io.pathops.controlplane.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import io.pathops.controlplane.dto.LoginResult;
import io.pathops.controlplane.model.ProvisioningTool;
import io.pathops.controlplane.model.Tenant;
import io.pathops.controlplane.model.TenantProvisioning;
import io.pathops.controlplane.repository.TenantProvisioningRepository;
import io.pathops.controlplane.utils.JwtUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserService userService;
    private final TenantProvisioningService tenantProvisioningService;
    private final TenantProvisioningRepository provisioningRepository;

    public LoginResult login(Jwt jwt) {
        String subject = JwtUtils.getSubject(jwt);
        String issuer = JwtUtils.getIssuer(jwt);
        String preferredUsername = JwtUtils.getPreferredUsername(jwt);
        String email = JwtUtils.getEmail(jwt);

        CreateOrUpdateUserResult createOrUpdateUserResult = userService.createOrUpdateUser(
            issuer,
            subject,
            preferredUsername,
            email
        );

        Set<ProvisioningTool> missingTools = getProvisioningToolsNeeded(createOrUpdateUserResult.tenant());

        boolean provisioningTriggered = !missingTools.isEmpty();
        if (provisioningTriggered) {
            tenantProvisioningService.provisionTenantForUser(
                createOrUpdateUserResult.user(),
                createOrUpdateUserResult.tenant()
            );
        }

        return LoginResult.builder()
            .userId(createOrUpdateUserResult.user().getId())
            .tenantId(createOrUpdateUserResult.tenant().getId())
            .tenantName(createOrUpdateUserResult.tenant().getName())
            .tenantSlug(createOrUpdateUserResult.tenant().getSlug())
            .membershipRole(createOrUpdateUserResult.membership().getRole())
            .identityChanged(createOrUpdateUserResult.identityChanged())
            .requiresTokenRefresh(missingTools.contains(ProvisioningTool.KEYCLOAK))
            .requiresToolRelogin(provisioningTriggered)
            .build();
    }

    private Set<ProvisioningTool> getProvisioningToolsNeeded(Tenant tenant) {
        List<TenantProvisioning> existing = provisioningRepository.findByTenant(tenant);

        if (existing.isEmpty()) {
            return EnumSet.allOf(ProvisioningTool.class);
        }

        Set<ProvisioningTool> needed = EnumSet.noneOf(ProvisioningTool.class);
        Set<ProvisioningTool> present = EnumSet.noneOf(ProvisioningTool.class);

        for (TenantProvisioning tp : existing) {
            present.add(tp.getTool());
            if (tp.needsProvisioning()) {
                needed.add(tp.getTool());
            }
        }

        for (ProvisioningTool tool : ProvisioningTool.values()) {
            if (!present.contains(tool)) {
                needed.add(tool);
            }
        }

        return needed;
    }
}