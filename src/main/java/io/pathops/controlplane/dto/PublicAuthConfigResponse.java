package io.pathops.controlplane.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record PublicAuthConfigResponse(
    String controlPlaneBaseUrl,
    String issuer,
    String clientId,
    List<String> scopes,
    String loginPath
) {
}