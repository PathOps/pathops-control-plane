package io.pathops.controlplane.integration.vault;

public record KeycloakAdminCredentials(
    String clientId,
    String clientSecret,
    String tokenUrl,
    String adminApiBaseUrl,
    String realm
) {
}