package io.pathops.controlplane.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {
	
	public void publishTenantCreatedEvent(Long tenantId) {
		log.info("TODO");
	}
}
