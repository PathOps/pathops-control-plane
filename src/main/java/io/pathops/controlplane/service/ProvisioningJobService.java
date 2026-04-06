package io.pathops.controlplane.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.pathops.controlplane.dto.ClaimedProvisioningJob;
import io.pathops.controlplane.model.MembershipProvisioning;
import io.pathops.controlplane.model.PathOpsUser;
import io.pathops.controlplane.model.ProvisioningJob;
import io.pathops.controlplane.model.ProvisioningJobStatus;
import io.pathops.controlplane.model.ProvisioningScopeType;
import io.pathops.controlplane.model.SyncStatus;
import io.pathops.controlplane.model.TenantProvisioning;
import io.pathops.controlplane.repository.MembershipProvisioningRepository;
import io.pathops.controlplane.repository.PathOpsUserRepository;
import io.pathops.controlplane.repository.ProvisioningJobRepository;
import io.pathops.controlplane.repository.TenantProvisioningRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProvisioningJobService {

    private final ProvisioningJobRepository provisioningJobRepository;
    private final TenantProvisioningRepository tenantProvisioningRepository;
    private final MembershipProvisioningRepository membershipProvisioningRepository;
    private final PathOpsUserRepository pathOpsUserRepository;

    public ProvisioningJob enqueueTenantProvisioning(TenantProvisioning provisioning) {
        Optional<ProvisioningJob> existing = provisioningJobRepository
            .findFirstByStatusAndScopeTypeAndTenantProvisioningId(
                ProvisioningJobStatus.PENDING,
                ProvisioningScopeType.TENANT,
                provisioning.getId()
            );

        if (existing.isPresent()) {
            return existing.get();
        }

        ProvisioningJob job = new ProvisioningJob();
        job.setScopeType(ProvisioningScopeType.TENANT);
        job.setTenantProvisioning(provisioning);
        job.setStatus(ProvisioningJobStatus.PENDING);
        return provisioningJobRepository.save(job);
    }

    public ProvisioningJob enqueueMembershipProvisioning(MembershipProvisioning provisioning) {
        Optional<ProvisioningJob> existing = provisioningJobRepository
            .findFirstByStatusAndScopeTypeAndMembershipProvisioningId(
                ProvisioningJobStatus.PENDING,
                ProvisioningScopeType.MEMBERSHIP,
                provisioning.getId()
            );

        if (existing.isPresent()) {
            return existing.get();
        }

        ProvisioningJob job = new ProvisioningJob();
        job.setScopeType(ProvisioningScopeType.MEMBERSHIP);
        job.setMembershipProvisioning(provisioning);
        job.setStatus(ProvisioningJobStatus.PENDING);
        return provisioningJobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public boolean hasRecentInProgress(Instant cutoff) {
        return provisioningJobRepository.existsRecentInProgress(cutoff);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ClaimedProvisioningJob claimNextPendingJob(String workerId) {
        Optional<ProvisioningJob> nextOpt = provisioningJobRepository.findNextPending();
        if (nextOpt.isEmpty()) {
            return null;
        }

        Long jobId = nextOpt.get().getId();

        ProvisioningJob job = provisioningJobRepository.findByIdForUpdate(jobId)
            .orElseThrow(() -> new IllegalStateException("Provisioning job not found: " + jobId));

        if (job.getStatus() != ProvisioningJobStatus.PENDING) {
            return null;
        }

        Instant startedAt = Instant.now();
        job.setStatus(ProvisioningJobStatus.IN_PROGRESS);
        job.setStartedAt(startedAt);
        job.setHeartbeatAt(startedAt);
        job.setClaimedBy(workerId);
        provisioningJobRepository.save(job);

        Long tenantProvisioningId = job.getTenantProvisioning() != null
            ? job.getTenantProvisioning().getId()
            : null;

        Long membershipProvisioningId = job.getMembershipProvisioning() != null
            ? job.getMembershipProvisioning().getId()
            : null;

        return new ClaimedProvisioningJob(
            job.getId(),
            job.getScopeType(),
            tenantProvisioningId,
            membershipProvisioningId
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(
        Long jobId,
        Instant startedAt,
        Instant finishedAt,
        long durationMs,
        String externalRef,
        String resolvedUserId
    ) {
        ProvisioningJob job = provisioningJobRepository.findByIdForUpdate(jobId)
            .orElseThrow(() -> new IllegalStateException("Provisioning job not found: " + jobId));

        job.setStatus(ProvisioningJobStatus.SUCCESS);
        job.setFinishedAt(finishedAt);
        job.setDurationMs(durationMs);
        job.setExternalRef(externalRef);
        job.setErrorMessage(null);
        provisioningJobRepository.save(job);

        if (job.getScopeType() == ProvisioningScopeType.TENANT) {
            TenantProvisioning provisioning = job.getTenantProvisioning();
            provisioning.setStatus(SyncStatus.SUCCESS);
            provisioning.setLastAttemptAt(startedAt);
            provisioning.setLastDurationMs(durationMs);
            provisioning.setLastError(null);
            provisioning.setExternalRef(externalRef);
            tenantProvisioningRepository.save(provisioning);
            return;
        }

        MembershipProvisioning provisioning = job.getMembershipProvisioning();
        provisioning.setStatus(SyncStatus.SUCCESS);
        provisioning.setLastAttemptAt(startedAt);
        provisioning.setLastDurationMs(durationMs);
        provisioning.setLastError(null);
        provisioning.setExternalRef(externalRef);
        membershipProvisioningRepository.save(provisioning);

        if (resolvedUserId != null) {
            PathOpsUser user = provisioning.getMembership().getUser();
            if (!resolvedUserId.equals(user.getKeycloakUserId())) {
                user.setKeycloakUserId(resolvedUserId);
                pathOpsUserRepository.save(user);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailure(Long jobId, Instant startedAt, Instant finishedAt, long durationMs, String errorMessage) {
        ProvisioningJob job = provisioningJobRepository.findByIdForUpdate(jobId)
            .orElseThrow(() -> new IllegalStateException("Provisioning job not found: " + jobId));

        job.setStatus(ProvisioningJobStatus.FAILED);
        job.setFinishedAt(finishedAt);
        job.setDurationMs(durationMs);
        job.setErrorMessage(errorMessage);
        provisioningJobRepository.save(job);

        if (job.getScopeType() == ProvisioningScopeType.TENANT) {
            TenantProvisioning provisioning = job.getTenantProvisioning();
            provisioning.setStatus(SyncStatus.FAILED);
            provisioning.setLastAttemptAt(startedAt);
            provisioning.setLastDurationMs(durationMs);
            provisioning.setLastError(errorMessage);
            tenantProvisioningRepository.save(provisioning);
        } else {
            MembershipProvisioning provisioning = job.getMembershipProvisioning();
            provisioning.setStatus(SyncStatus.FAILED);
            provisioning.setLastAttemptAt(startedAt);
            provisioning.setLastDurationMs(durationMs);
            provisioning.setLastError(errorMessage);
            membershipProvisioningRepository.save(provisioning);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markTimedOut(Long jobId, Instant finishedAt, String errorMessage) {
        ProvisioningJob job = provisioningJobRepository.findByIdForUpdate(jobId)
            .orElseThrow(() -> new IllegalStateException("Provisioning job not found: " + jobId));

        if (job.getStatus() != ProvisioningJobStatus.IN_PROGRESS) {
            return;
        }

        job.setStatus(ProvisioningJobStatus.TIMED_OUT);
        job.setFinishedAt(finishedAt);

        Long durationMs = null;
        if (job.getStartedAt() != null) {
            durationMs = Duration.between(job.getStartedAt(), finishedAt).toMillis();
        }

        job.setDurationMs(durationMs);
        job.setErrorMessage(errorMessage);
        provisioningJobRepository.save(job);

        if (job.getScopeType() == ProvisioningScopeType.TENANT) {
            TenantProvisioning provisioning = job.getTenantProvisioning();
            provisioning.setStatus(SyncStatus.FAILED);
            provisioning.setLastAttemptAt(job.getStartedAt());
            provisioning.setLastDurationMs(durationMs);
            provisioning.setLastError(errorMessage);
            tenantProvisioningRepository.save(provisioning);
        } else {
            MembershipProvisioning provisioning = job.getMembershipProvisioning();
            provisioning.setStatus(SyncStatus.FAILED);
            provisioning.setLastAttemptAt(job.getStartedAt());
            provisioning.setLastDurationMs(durationMs);
            provisioning.setLastError(errorMessage);
            membershipProvisioningRepository.save(provisioning);
        }
    }
}