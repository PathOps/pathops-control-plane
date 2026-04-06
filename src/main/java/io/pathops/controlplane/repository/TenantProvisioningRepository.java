package io.pathops.controlplane.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.pathops.controlplane.model.Tenant;
import io.pathops.controlplane.model.TenantProvisioning;
import io.pathops.controlplane.model.TenantProvisioningTool;

public interface TenantProvisioningRepository extends JpaRepository<TenantProvisioning, Long> {

    List<TenantProvisioning> findByTenant(Tenant tenant);

    Optional<TenantProvisioning> findByTenantAndTool(Tenant tenant, TenantProvisioningTool tool);
}