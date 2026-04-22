package io.pathops.controlplane.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.pathops.controlplane.model.MembershipToolProjection;

public interface MembershipToolProjectionRepository extends JpaRepository<MembershipToolProjection, Long>{

}
