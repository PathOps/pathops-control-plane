package io.pathops.controlplane.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.pathops.controlplane.model.TenantToolProjection;

public interface TenantToolProjectionRepository extends JpaRepository<TenantToolProjection, Long>{

}
