package io.pathops.controlplane.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.pathops.controlplane.model.Membership;
import io.pathops.controlplane.model.MembershipRole;
import io.pathops.controlplane.model.User;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByUserOrderByCreatedAtAsc(User user);
    
    Optional<Membership> findFirstByUserAndRoleOrderByCreatedAtAsc(User user, MembershipRole role);
}