package io.pathops.controlplane.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pathops.controlplane.model.OutboxEvent;
import io.pathops.controlplane.model.OutboxEventStatus;
import io.pathops.controlplane.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxEventQueryService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional(readOnly = true)
    public List<OutboxEvent> findPendingBatch(int batchSize) {
        return outboxEventRepository.findByStatusOrderByCreatedAtAsc(
            OutboxEventStatus.PENDING,
            PageRequest.of(0, batchSize)
        );
    }
}