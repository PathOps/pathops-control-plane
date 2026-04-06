package io.pathops.controlplane.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.pathops.controlplane.model.MembershipProvisioningJob;

public interface MembershipProvisioningJobRepository extends JpaRepository<MembershipProvisioningJob, Long> {
}