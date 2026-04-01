package io.pathops.controlplane.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pathops.controlplane.dto.LoginResult;
import io.pathops.controlplane.model.Membership;
import io.pathops.controlplane.model.MembershipRole;
import io.pathops.controlplane.model.PathOpsUser;
import io.pathops.controlplane.model.Tenant;
import io.pathops.controlplane.repository.MembershipRepository;
import io.pathops.controlplane.repository.PathOpsUserRepository;
import io.pathops.controlplane.repository.TenantRepository;
import io.pathops.controlplane.utils.JwtUtility;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final PathOpsUserRepository pathOpsUserRepository;
    private final TenantRepository tenantRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public LoginResult login(Jwt jwt) {
        String subject = JwtUtility.getSubject(jwt);
        String issuer = JwtUtility.getIssuer(jwt);
        String preferredUsername = JwtUtility.getPreferredUsername(jwt);
        String email = JwtUtility.getEmail(jwt);

        boolean identityChanged = false;

        PathOpsUser user = pathOpsUserRepository
            .findByIssuerAndSubject(issuer, subject)
            .orElse(null);

        if (user == null) {
            user = new PathOpsUser();
            user.setIssuer(issuer);
            user.setSubject(subject);
            user.setPreferredUsername(preferredUsername);
            user.setEmail(email);
            user = pathOpsUserRepository.save(user);
            identityChanged = true;
        } else {
            boolean userUpdated = updateUserIfNeeded(user, preferredUsername, email);
            if (userUpdated) {
                user = pathOpsUserRepository.save(user);
                identityChanged = true;
            }
        }

        List<Membership> memberships = membershipRepository.findByUser(user);

        if (memberships.isEmpty()) {
            Tenant tenant = new Tenant();
            tenant.setName(defaultTenantName(preferredUsername));
            tenant.setSlug(generateTenantSlug(preferredUsername));
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
                .identityChanged(true)
                .requiresTokenRefresh(true)
                .requiresToolRelogin(true)
                .build();
        }

        Membership currentMembership = memberships.get(0);

        return LoginResult.builder()
            .userId(user.getId())
            .tenantId(currentMembership.getTenant().getId())
            .tenantName(currentMembership.getTenant().getName())
            .tenantSlug(currentMembership.getTenant().getSlug())
            .membershipRole(currentMembership.getRole())
            .identityChanged(identityChanged)
            .requiresTokenRefresh(false)
            .requiresToolRelogin(false)
            .build();
    }

    private boolean updateUserIfNeeded(
        PathOpsUser user,
        String preferredUsername,
        String email
    ) {
        boolean changed = false;

        if (!equalsNullable(user.getPreferredUsername(), preferredUsername)) {
            user.setPreferredUsername(preferredUsername);
            changed = true;
        }

        if (!equalsNullable(user.getEmail(), email)) {
            user.setEmail(email);
            changed = true;
        }

        return changed;
    }

    private String defaultTenantName(String preferredUsername) {
        if (preferredUsername == null || preferredUsername.isBlank()) {
            return "Personal Tenant";
        }
        return preferredUsername + " Personal Tenant";
    }

    private String generateTenantSlug(String preferredUsername) {
        String base = preferredUsername == null || preferredUsername.isBlank()
            ? "tenant"
            : preferredUsername.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        if (base.isBlank()) {
            base = "tenant";
        }

        return base + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private boolean equalsNullable(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}