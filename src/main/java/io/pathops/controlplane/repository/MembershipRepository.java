package io.pathops.controlplane.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.pathops.controlplane.model.Membership;
import io.pathops.controlplane.model.MembershipRole;
import io.pathops.controlplane.model.PathOpsUser;
import io.pathops.controlplane.model.Tenant;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByUserOrderByCreatedAtAsc(PathOpsUser user);

    Optional<Membership> findByUserAndTenant(PathOpsUser user, Tenant tenant);

    Optional<Membership> findByUserIdAndTenantIdAndRole(Long userId, Long tenantId, MembershipRole role);
}