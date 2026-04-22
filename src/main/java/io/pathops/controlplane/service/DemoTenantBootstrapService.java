package io.pathops.controlplane.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pathops.controlplane.model.Membership;
import io.pathops.controlplane.model.MembershipRole;
import io.pathops.controlplane.model.MembershipToolProjection;
import io.pathops.controlplane.model.Tenant;
import io.pathops.controlplane.model.TenantToolProjection;
import io.pathops.controlplane.model.Tool;
import io.pathops.controlplane.model.ToolProjectionConfig;
import io.pathops.controlplane.model.ToolProjectionScope;
import io.pathops.controlplane.model.ToolProjectionStatus;
import io.pathops.controlplane.model.User;
import io.pathops.controlplane.repository.MembershipRepository;
import io.pathops.controlplane.repository.MembershipToolProjectionRepository;
import io.pathops.controlplane.repository.TenantRepository;
import io.pathops.controlplane.repository.TenantToolProjectionRepository;
import io.pathops.controlplane.repository.ToolProjectionConfigRepository;
import io.pathops.controlplane.repository.ToolRepository;
import io.pathops.controlplane.repository.UserRepository;
import io.pathops.controlplane.service.defaults.DefaultToolDefinition;
import io.pathops.controlplane.service.defaults.DefaultToolFactory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DemoTenantBootstrapService {

    private final DefaultToolFactory defaultToolFactory;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final MembershipRepository membershipRepository;
    private final ToolRepository toolRepository;
    private final ToolProjectionConfigRepository toolProjectionConfigRepository;
    private final TenantToolProjectionRepository tenantToolProjectionRepository;
    private final MembershipToolProjectionRepository membershipToolProjectionRepository;

    @Transactional
    public void bootstrapTenant(Long tenantId, Long ownerUserId) {
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantId));

        User ownerUser = userRepository.findById(ownerUserId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + ownerUserId));

        Membership ownerMembership = membershipRepository
            .findFirstByUserAndRoleOrderByCreatedAtAsc(ownerUser, MembershipRole.OWNER)
            .orElseThrow(() -> new IllegalStateException(
                "Owner membership not found for user: " + ownerUserId
            ));

        List<DefaultToolDefinition> defaults = defaultToolFactory.createDefaults();

        for (DefaultToolDefinition definition : defaults) {
            Tool tool = new Tool();
            tool.setName(definition.name());
            tool.setTenant(tenant);
            tool.setCreatedBy(ownerUser);
            tool.setType(definition.type());
            tool.setProvider(definition.provider());
            tool.setBaseUrl(definition.baseUrl());
            tool = toolRepository.save(tool);

            for (var projectionDefinition : definition.projections()) {
                ToolProjectionConfig config = new ToolProjectionConfig();
                config.setTool(tool);
                config.setScope(projectionDefinition.scope());
                config.setEnabled(projectionDefinition.enabled());
                config = toolProjectionConfigRepository.save(config);

                createPendingProjectionIfEnabled(config, ownerMembership);
            }
        }
    }

    private void createPendingProjectionIfEnabled(
        ToolProjectionConfig config,
        Membership ownerMembership
    ) {
    	if (!config.getEnabled()) {
    	    return;
    	}

        if (config.getScope() == ToolProjectionScope.TENANT) {
            TenantToolProjection projection = new TenantToolProjection();
            projection.setToolProjectionConfig(config);
            projection.setStatus(ToolProjectionStatus.PENDING);
            projection.setAttemptCount(0);
            tenantToolProjectionRepository.save(projection);
            return;
        }

        if (config.getScope() == ToolProjectionScope.MEMBERSHIP) {
            MembershipToolProjection projection = new MembershipToolProjection();
            projection.setToolProjectionConfig(config);
            projection.setMembership(ownerMembership);
            projection.setStatus(ToolProjectionStatus.PENDING);
            projection.setAttemptCount(0);
            membershipToolProjectionRepository.save(projection);
        }
    }
}