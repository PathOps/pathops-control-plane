package io.pathops.controlplane.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.pathops.controlplane.model.Membership;
import io.pathops.controlplane.model.PathOpsUser;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByUserOrderByCreatedAtAsc(PathOpsUser user);
}