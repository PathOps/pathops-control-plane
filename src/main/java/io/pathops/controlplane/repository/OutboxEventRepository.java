package io.pathops.controlplane.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import io.pathops.controlplane.model.OutboxEvent;
import io.pathops.controlplane.model.OutboxEventStatus;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(
        OutboxEventStatus status,
        Pageable pageable
    );
}