package io.pathops.controlplane.service;

import org.springframework.stereotype.Service;

import io.pathops.controlplane.model.OutboxEvent;
import io.pathops.controlplane.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxEventStoreService {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxEvent save(OutboxEvent event) {
        return outboxEventRepository.save(event);
    }
}