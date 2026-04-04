package io.pathops.controlplane.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.pathops.controlplane.model.ProvisioningTool;
import io.pathops.controlplane.model.Tenant;
import io.pathops.controlplane.model.TenantProvisioning;

public interface TenantProvisioningRepository extends JpaRepository<TenantProvisioning, Long> {

    Optional<TenantProvisioning> findByTenantAndTool(Tenant tenant, ProvisioningTool tool);

    List<TenantProvisioning> findByTenant(Tenant tenant);
}