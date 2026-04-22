package io.pathops.controlplane.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.pathops.controlplane.model.ToolProjectionConfig;

public interface ToolProjectionConfigRepository extends JpaRepository<ToolProjectionConfig, Long>  {

}
