package io.pathops.controlplane.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.pathops.controlplane.model.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}