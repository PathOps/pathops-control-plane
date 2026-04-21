package io.pathops.controlplane.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.pathops.controlplane.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByIssuerAndSubject(String issuer, String subject);
}