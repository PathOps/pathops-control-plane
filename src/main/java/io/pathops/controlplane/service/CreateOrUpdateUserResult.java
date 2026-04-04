package io.pathops.controlplane.service;

import io.pathops.controlplane.model.Membership;
import io.pathops.controlplane.model.PathOpsUser;
import io.pathops.controlplane.model.Tenant;

public record CreateOrUpdateUserResult(
    PathOpsUser user,
    Tenant tenant,
    Membership membership,
    boolean identityChanged
) {
}