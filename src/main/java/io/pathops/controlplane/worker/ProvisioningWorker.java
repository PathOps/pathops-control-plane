package io.pathops.controlplane.worker;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.pathops.controlplane.dto.ClaimedProvisioningJob;
import io.pathops.controlplane.dto.MembershipProvisioningExecutionResult;
import io.pathops.controlplane.model.ProvisioningJob;
import io.pathops.controlplane.repository.ProvisioningJobRepository;
import io.pathops.controlplane.service.MembershipProvisioningService;
import io.pathops.controlplane.service.ProvisioningExecutionOutcome;
import io.pathops.controlplane.service.ProvisioningExecutionSupport;
import io.pathops.controlplane.service.ProvisioningJobService;
import io.pathops.controlplane.service.TenantProvisioningService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProvisioningWorker {

    private final ProvisioningJobRepository provisioningJobRepository;
    private final ProvisioningJobService provisioningJobService;
    private final TenantProvisioningService tenantProvisioningService;
    private final MembershipProvisioningService membershipProvisioningService;
    private final ProvisioningExecutionSupport provisioningExecutionSupport;

    private final String workerId = "worker-" + UUID.randomUUID();

    @Scheduled(fixedDelayString = "${pathops.provisioning.worker-delay-ms:5000}")
    public void run() {
        markTimedOutJobs();

        Instant cutoff = Instant.now().minus(Duration.ofSeconds(60));
        if (provisioningJobService.hasRecentInProgress(cutoff)) {
            return;
        }

        ClaimedProvisioningJob claimedJob = provisioningJobService.claimNextPendingJob(workerId);
        if (claimedJob == null) {
            return;
        }

        ProvisioningExecutionOutcome<WorkerExecutionResult> outcome = provisioningExecutionSupport.execute(() ->
            executeClaimedJob(claimedJob)
        );

        if (outcome.success()) {
            provisioningJobService.markSuccess(
                claimedJob.jobId(),
                outcome.startedAt(),
                outcome.finishedAt(),
                outcome.durationMs(),
                outcome.result().externalRef(),
                outcome.result().resolvedUserId()
            );
        } else {
            provisioningJobService.markFailure(
                claimedJob.jobId(),
                outcome.startedAt(),
                outcome.finishedAt(),
                outcome.durationMs(),
                outcome.errorMessage()
            );
        }
    }

    private void markTimedOutJobs() {
        Instant cutoff = Instant.now().minus(Duration.ofSeconds(60));

        for (ProvisioningJob job : provisioningJobRepository.findTimedOutInProgress(cutoff)) {
            provisioningJobService.markTimedOut(
                job.getId(),
                Instant.now(),
                "Provisioning job exceeded max runtime"
            );
        }
    }

    private WorkerExecutionResult executeClaimedJob(ClaimedProvisioningJob claimedJob) {
        return switch (claimedJob.scopeType()) {
            case TENANT -> {
                var provisioning = tenantProvisioningService.getById(claimedJob.tenantProvisioningId());
                String externalRef = tenantProvisioningService.execute(provisioning);
                yield new WorkerExecutionResult(externalRef, null);
            }
            case MEMBERSHIP -> {
                var provisioning = membershipProvisioningService.getById(claimedJob.membershipProvisioningId());
                MembershipProvisioningExecutionResult result = membershipProvisioningService.execute(provisioning);
                yield new WorkerExecutionResult(result.externalRef(), result.resolvedUserId());
            }
        };
    }

    private record WorkerExecutionResult(
        String externalRef,
        String resolvedUserId
    ) {
    }
}