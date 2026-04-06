package io.pathops.controlplane.dto;

import lombok.Builder;

@Builder
public record MeResult(
) {
}	

/*
@Builder
public record MeResult(
    MeUser user,
    MeTenant currentTenant,
    MeMembership currentMembership,
    List<MembershipSummary> memberships
) {
}
*/