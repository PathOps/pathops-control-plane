package io.pathops.controlplane.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.pathops.controlplane.model.Tool;

public interface ToolRepository extends JpaRepository<Tool, Long>  {
}