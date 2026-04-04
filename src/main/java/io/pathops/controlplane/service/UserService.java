package io.pathops.controlplane.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.pathops.controlplane.model.Membership;
import io.pathops.controlplane.model.MembershipRole;
import io.pathops.controlplane.model.PathOpsUser;
import io.pathops.controlplane.model.Tenant;
import io.pathops.controlplane.repository.MembershipRepository;
import io.pathops.controlplane.repository.PathOpsUserRepository;
import io.pathops.controlplane.repository.TenantRepository;
import io.pathops.controlplane.utils.StringUtils;
import io.pathops.controlplane.utils.TenantUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PathOpsUserRepository pathOpsUserRepository;
    private final TenantRepository tenantRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public CreateOrUpdateUserResult createOrUpdateUser(
        String issuer,
        String subject,
        String preferredUsername,
        String email
    ) {
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

            return new CreateOrUpdateUserResult(
                user,
                tenant,
                membership,
                true
            );
        }

        Membership currentMembership = memberships.get(0);

        return new CreateOrUpdateUserResult(
            user,
            currentMembership.getTenant(),
            currentMembership,
            identityChanged
        );
    }

    private boolean updateUserIfNeeded(
        PathOpsUser user,
        String preferredUsername,
        String email
    ) {
        boolean changed = false;

        if (!StringUtils.equalsNullable(user.getPreferredUsername(), preferredUsername)) {
            user.setPreferredUsername(preferredUsername);
            changed = true;
        }

        if (!StringUtils.equalsNullable(user.getEmail(), email)) {
            user.setEmail(email);
            changed = true;
        }

        return changed;
    }
}