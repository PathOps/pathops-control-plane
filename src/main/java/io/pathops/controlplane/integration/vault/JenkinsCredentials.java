package io.pathops.controlplane.integration.vault;

public record JenkinsCredentials(
    String baseUrl,
    String username,
    String apiToken
) {
}