package io.pathops.controlplane.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.pathops.controlplane.model.PathOpsUser;

public interface PathOpsUserRepository extends JpaRepository<PathOpsUser, Long> {

	Optional<PathOpsUser> findByIssuerAndSubject(String issuer, String subject);
	
    @Query("""
            select m.user
            from Membership m
            where m.tenant.id = :tenantId
              and m.role = io.pathops.controlplane.model.MembershipRole.OWNER
            order by m.createdAt asc
        """)
    Optional<PathOpsUser> findFirstOwnerByTenantId(Long tenantId);
}