package io.pathops.controlplane.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import io.pathops.controlplane.dto.LoginResult;
import io.pathops.controlplane.model.Membership;
import io.pathops.controlplane.model.MembershipRole;
import io.pathops.controlplane.model.User;
import io.pathops.controlplane.model.Tenant;
import io.pathops.controlplane.repository.MembershipRepository;
import io.pathops.controlplane.repository.UserRepository;
import io.pathops.controlplane.repository.TenantRepository;
import io.pathops.controlplane.utils.PathOpsUtils;
import io.pathops.controlplane.utils.TenantUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginUserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final MembershipRepository membershipRepository;

    public LoginResult createOrUpdateUser(
        String issuer,
        String subject,
        String preferredUsername,
        String email
    ) {
        User user = userRepository
            .findByIssuerAndSubject(issuer, subject)
            .orElse(null);

        if (user == null) {
            user = new User();
            user.setIssuer(issuer);
            user.setSubject(subject);
            user.setPreferredUsername(preferredUsername);
            user.setEmail(email);

            if (PathOpsUtils.isPathopsRealmIssuer(issuer)) {
                user.setKeycloakUserId(subject);
            }

            user = userRepository.save(user);
        } else {
            boolean userUpdated = updateUserIfNeeded(
            		user,
            		issuer,
            		subject,
            		preferredUsername,
            		email);
            if (userUpdated) {
                user = userRepository.save(user);
            }
        }

        List<Membership> memberships = membershipRepository.findByUserOrderByCreatedAtAsc(user);

        if (memberships.isEmpty()) {
            Tenant tenant = new Tenant();
            tenant.setName(TenantUtils.defaultTenantName(preferredUsername));
            tenant.setSlug(TenantUtils.generateTenantSlug(preferredUsername));
            tenant = tenantRepository.save(tenant);

            Membership membership = new Membership();
            membership.setUser(user);
            membership.setTenant(tenant);
            membership.setRole(MembershipRole.OWNER);
            membership = membershipRepository.save(membership);

            return LoginResult.builder()
	            .userId(user.getId())
	            .tenantId(tenant.getId())
	            .tenantName(tenant.getName())
	            .tenantSlug(tenant.getSlug())
	            .membershipRole(membership.getRole())
	            .build();
        }

        Membership currentMembership = memberships.get(0);

	    return LoginResult.builder()
	        .userId(user.getId())
	        .tenantId(currentMembership.getTenant().getId())
	        .tenantName(currentMembership.getTenant().getName())
	        .tenantSlug(currentMembership.getTenant().getSlug())
	        .membershipRole(currentMembership.getRole())
	        .build();
    }

    private boolean updateUserIfNeeded(
        User user,
        String issuer,
        String subject,
        String preferredUsername,
        String email
    ) {
    	boolean changed = false;

    	if (!Objects.equals(user.getEmail(), email)) {
    	    user.setEmail(email);
    	    changed = true;
    	}

    	if (!Objects.equals(user.getPreferredUsername(), preferredUsername)) {
    	    user.setPreferredUsername(preferredUsername);
    	    changed = true;
    	}

    	if (user.getKeycloakUserId() == null && PathOpsUtils.isPathopsRealmIssuer(issuer)) {
    	    user.setKeycloakUserId(subject);
    	    changed = true;
    	}

    	return changed;
    }
}