package io.pathops.controlplane.service;

import org.springframework.stereotype.Service;

import io.pathops.controlplane.model.MembershipRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginProvisioningOrchestrator {
	
	public void enqueueForLogin(Long userId, Long tenantId, MembershipRole membershipRole) {
		log.info("TODO");
	}

}
