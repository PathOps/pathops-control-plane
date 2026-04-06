package io.pathops.controlplane.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.pathops.controlplane.model.Membership;
import io.pathops.controlplane.model.MembershipProvisioning;
import io.pathops.controlplane.model.MembershipProvisioningTool;

public interface MembershipProvisioningRepository extends JpaRepository<MembershipProvisioning, Long> {

    List<MembershipProvisioning> findByMembership(Membership membership);

    Optional<MembershipProvisioning> findByMembershipAndTool(Membership membership, MembershipProvisioningTool tool);
}