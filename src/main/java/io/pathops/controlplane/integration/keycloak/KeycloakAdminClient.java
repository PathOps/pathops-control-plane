package io.pathops.controlplane.integration.keycloak;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import io.pathops.controlplane.integration.vault.KeycloakAdminCredentials;
import io.pathops.controlplane.integration.vault.VaultSecretService;
import io.pathops.controlplane.model.PathOpsUser;

@Component
public class KeycloakAdminClient {

    private final RestTemplate restTemplate;
    private final VaultSecretService vaultSecretService;

    public KeycloakAdminClient(RestTemplate pathopsRestTemplate, VaultSecretService vaultSecretService) {
        this.restTemplate = pathopsRestTemplate;
        this.vaultSecretService = vaultSecretService;
    }

    public String ensureTenantGroupExists(String tenantSlug) {
        String token = obtainAccessToken();
        KeycloakAdminCredentials creds = vaultSecretService.readKeycloakAdminCredentials();

        String groupName = "tenant:" + tenantSlug;
        String groupId = findGroupId(groupName, token, creds);

        if (groupId != null) {
            return groupId;
        }

        HttpHeaders headers = bearerHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of("name", groupName);

        restTemplate.exchange(
            creds.adminApiBaseUrl() + "/groups",
            HttpMethod.POST,
            new HttpEntity<>(payload, headers),
            Void.class
        );

        groupId = findGroupId(groupName, token, creds);

        if (groupId == null) {
            throw new IllegalStateException("Keycloak group was not found after creation: " + groupName);
        }

        return groupId;
    }

    public void addUserToTenantGroup(String userId, String groupId) {
        String token = obtainAccessToken();
        KeycloakAdminCredentials creds = vaultSecretService.readKeycloakAdminCredentials();

        HttpHeaders headers = bearerHeaders(token);

        restTemplate.exchange(
            creds.adminApiBaseUrl() + "/users/" + userId + "/groups/" + groupId,
            HttpMethod.PUT,
            new HttpEntity<>(headers),
            Void.class
        );
    }

    public String resolveUserId(PathOpsUser user) {
        if (hasText(user.getKeycloakUserId())) {
            return user.getKeycloakUserId();
        }

        if (isPathopsRealmUser(user) && hasText(user.getSubject())) {
            return user.getSubject();
        }

        String token = obtainAccessToken();
        KeycloakAdminCredentials creds = vaultSecretService.readKeycloakAdminCredentials();

        String byUsername = findUserIdByUsername(user.getPreferredUsername(), token, creds);
        if (byUsername != null) {
            return byUsername;
        }

        String byEmail = findUserIdByEmail(user.getEmail(), token, creds);
        if (byEmail != null) {
            return byEmail;
        }

        throw new IllegalStateException(
            "Could not resolve Keycloak user id for user issuer=" + user.getIssuer() + ", subject=" + user.getSubject()
        );
    }

    private boolean isPathopsRealmUser(PathOpsUser user) {
        return user.getIssuer() != null
            && user.getIssuer().equals("https://keycloak.demo.pathops.io/realms/pathops");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @SuppressWarnings("unchecked")
    private String findUserIdByUsername(String preferredUsername, String token, KeycloakAdminCredentials creds) {
        if (preferredUsername == null || preferredUsername.isBlank()) {
            return null;
        }

        HttpHeaders headers = bearerHeaders(token);

        String encoded = URLEncoder.encode(preferredUsername, StandardCharsets.UTF_8);

        ResponseEntity<List> response = restTemplate.exchange(
            creds.adminApiBaseUrl() + "/users?username=" + encoded + "&exact=true",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            List.class
        );

        List<Map<String, Object>> users = response.getBody();
        if (users == null) {
            return null;
        }

        return users.stream()
            .filter(u -> preferredUsername.equals(u.get("username")))
            .map(u -> Objects.toString(u.get("id"), null))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private String findUserIdByEmail(String email, String token, KeycloakAdminCredentials creds) {
        if (email == null || email.isBlank()) {
            return null;
        }

        HttpHeaders headers = bearerHeaders(token);

        String encoded = URLEncoder.encode(email, StandardCharsets.UTF_8);

        ResponseEntity<List> response = restTemplate.exchange(
            creds.adminApiBaseUrl() + "/users?email=" + encoded,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            List.class
        );

        List<Map<String, Object>> users = response.getBody();
        if (users == null) {
            return null;
        }

        return users.stream()
            .filter(u -> email.equalsIgnoreCase(Objects.toString(u.get("email"), null)))
            .map(u -> Objects.toString(u.get("id"), null))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private String findGroupId(String groupName, String token, KeycloakAdminCredentials creds) {
        HttpHeaders headers = bearerHeaders(token);

        ResponseEntity<List> response = restTemplate.exchange(
            creds.adminApiBaseUrl() + "/groups?search=" + URLEncoder.encode(groupName, StandardCharsets.UTF_8),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            List.class
        );

        List<Map<String, Object>> groups = response.getBody();
        if (groups == null) {
            return null;
        }

        return groups.stream()
            .filter(g -> groupName.equals(g.get("name")))
            .map(g -> Objects.toString(g.get("id"), null))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private String obtainAccessToken() {
        KeycloakAdminCredentials creds = vaultSecretService.readKeycloakAdminCredentials();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", creds.clientId());
        form.add("client_secret", creds.clientSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> response = restTemplate.exchange(
            creds.tokenUrl(),
            HttpMethod.POST,
            new HttpEntity<>(form, headers),
            Map.class
        );

        Object token = response.getBody() != null ? response.getBody().get("access_token") : null;
        if (token == null) {
            throw new IllegalStateException("Could not obtain Keycloak admin token");
        }

        return token.toString();
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}