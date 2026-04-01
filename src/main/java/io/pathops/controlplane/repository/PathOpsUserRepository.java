package io.pathops.controlplane.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.pathops.controlplane.model.PathOpsUser;

public interface PathOpsUserRepository extends JpaRepository<PathOpsUser, Long> {

	Optional<PathOpsUser> findByIssuerAndSubject(String issuer, String subject);
}