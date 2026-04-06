package io.pathops.controlplane.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.pathops.controlplane.model.ProvisioningJob;
import io.pathops.controlplane.model.ProvisioningJobStatus;
import io.pathops.controlplane.model.ProvisioningScopeType;
import jakarta.persistence.LockModeType;

public interface ProvisioningJobRepository extends JpaRepository<ProvisioningJob, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select pj
        from ProvisioningJob pj
        left join fetch pj.tenantProvisioning tp
        left join fetch tp.tenant
        left join fetch pj.membershipProvisioning mp
        left join fetch mp.membership m
        left join fetch m.user
        left join fetch m.tenant
        where pj.id = :id
    """)
    Optional<ProvisioningJob> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        select pj
        from ProvisioningJob pj
        where pj.status = 'PENDING'
        order by pj.createdAt asc
    """)
    List<ProvisioningJob> findPendingOrdered(Pageable pageable);

    default Optional<ProvisioningJob> findNextPending() {
        return findPendingOrdered(PageRequest.of(0, 1)).stream().findFirst();
    }

    @Query("""
        select count(pj) > 0
        from ProvisioningJob pj
        where pj.status = 'IN_PROGRESS'
          and pj.startedAt >= :cutoff
    """)
    boolean existsRecentInProgress(@Param("cutoff") Instant cutoff);

    @Query("""
        select pj
        from ProvisioningJob pj
        where pj.status = 'IN_PROGRESS'
          and pj.startedAt < :cutoff
    """)
    List<ProvisioningJob> findTimedOutInProgress(@Param("cutoff") Instant cutoff);

    Optional<ProvisioningJob> findFirstByStatusAndScopeTypeAndTenantProvisioningId(
        ProvisioningJobStatus status,
        ProvisioningScopeType scopeType,
        Long tenantProvisioningId
    );

    Optional<ProvisioningJob> findFirstByStatusAndScopeTypeAndMembershipProvisioningId(
        ProvisioningJobStatus status,
        ProvisioningScopeType scopeType,
        Long membershipProvisioningId
    );
}