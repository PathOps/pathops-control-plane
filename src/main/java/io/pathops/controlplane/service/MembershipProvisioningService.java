package io.pathops.controlplane.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pathops.controlplane.dto.MembershipProvisioningExecutionResult;
import io.pathops.controlplane.integration.jenkins.JenkinsClient;
import io.pathops.controlplane.integration.keycloak.KeycloakAdminClient;
import io.pathops.controlplane.model.Membership;
import io.pathops.controlplane.model.MembershipProvisioning;
import io.pathops.controlplane.model.MembershipProvisioningTool;
import io.pathops.controlplane.model.MembershipRole;
import io.pathops.controlplane.model.PathOpsUser;
import io.pathops.controlplane.model.SyncStatus;
import io.pathops.controlplane.repository.MembershipProvisioningRepository;
import io.pathops.controlplane.repository.MembershipRepository;
import io.pathops.controlplane.repository.PathOpsUserRepository;
import io.pathops.controlplane.repository.TenantRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MembershipProvisioningService {

    private final PathOpsUserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final MembershipRepository membershipRepository;
    private final MembershipProvisioningRepository membershipProvisioningRepository;
    private final KeycloakAdminClient keycloakAdminClient;
    private final JenkinsClient jenkinsClient;
    private final ProvisioningJobService provisioningJobService;

    public void enqueueOwnerProvisioningForUser(Long userId, Long tenantId) {
        PathOpsUser user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        var tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        Membership membership = membershipRepository.findByUserAndTenant(user, tenant)
            .orElseThrow(() -> new IllegalStateException(
                "Membership not found for user=" + userId + ", tenant=" + tenantId));

        if (membership.getRole() != MembershipRole.OWNER) {
            return;
        }

        provisioningJobService.enqueueMembershipProvisioning(
            ensureProvisioningRow(membership, MembershipProvisioningTool.KEYCLOAK_GROUP_MEMBERSHIP)
        );
        provisioningJobService.enqueueMembershipProvisioning(
            ensureProvisioningRow(membership, MembershipProvisioningTool.JENKINS_OWNER_ACCESS)
        );
    }

    public MembershipProvisioning ensureProvisioningRow(Membership membership, MembershipProvisioningTool tool) {
        return membershipProvisioningRepository.findByMembershipAndTool(membership, tool)
            .orElseGet(() -> {
                MembershipProvisioning provisioning = new MembershipProvisioning();
                provisioning.setMembership(membership);
                provisioning.setTool(tool);
                provisioning.setStatus(SyncStatus.PENDING);
                return membershipProvisioningRepository.save(provisioning);
            });
    }

    public MembershipProvisioningExecutionResult execute(MembershipProvisioning provisioning) {
        Membership membership = provisioning.getMembership();
        PathOpsUser user = membership.getUser();
        String tenantSlug = membership.getTenant().getSlug();

        return switch (provisioning.getTool()) {
            case KEYCLOAK_GROUP_MEMBERSHIP -> {
                String resolvedUserId = keycloakAdminClient.resolveUserId(user);
                String groupId = keycloakAdminClient.ensureTenantGroupExists(tenantSlug);
                keycloakAdminClient.addUserToTenantGroup(resolvedUserId, groupId);

                yield new MembershipProvisioningExecutionResult(
                    "user:" + resolvedUserId + "->group:" + groupId,
                    resolvedUserId
                );
            }
            case JENKINS_OWNER_ACCESS -> {
                jenkinsClient.ensureTenantAccessProjection(tenantSlug, membership.getRole());

                yield new MembershipProvisioningExecutionResult(
                    "tenant:" + tenantSlug + ":role:" + membership.getRole().name(),
                    null
                );
            }
        };
    }

    @Transactional(readOnly = true)
    public MembershipProvisioning getById(Long id) {
        return membershipProvisioningRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Membership provisioning not found: " + id));
    }
}