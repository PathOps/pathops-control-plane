package io.pathops.controlplane.integration.vault;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class VaultSecretService {

    private final RestTemplate restTemplate;

    @Value("${pathops.vault.base-url}")
    private String vaultBaseUrl;

    @Value("${pathops.vault.token}")
    private String vaultToken;

    @Value("${pathops.vault.kv-mount:secret}")
    private String kvMount;

    public VaultSecretService() {
        this.restTemplate = new RestTemplate();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> readSecret(String secretPath) {
        String url = String.format("%s/v1/%s/data/%s", vaultBaseUrl, kvMount, secretPath);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Vault-Token", vaultToken);

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("data")) {
            throw new IllegalStateException("Vault response missing data");
        }

        Map<String, Object> outerData = (Map<String, Object>) body.get("data");
        if (outerData == null || !outerData.containsKey("data")) {
            throw new IllegalStateException("Vault KV v2 response missing nested data");
        }

        return (Map<String, Object>) outerData.get("data");
    }

    public KeycloakAdminCredentials readKeycloakAdminCredentials() {
        Map<String, Object> data = readSecret("pathops/control-plane/keycloak-admin");

        return new KeycloakAdminCredentials(
            stringValue(data, "client_id"),
            stringValue(data, "client_secret"),
            stringValue(data, "token_url"),
            stringValue(data, "admin_api_base_url"),
            stringValue(data, "realm")
        );
    }

    public JenkinsCredentials readJenkinsCredentials() {
        Map<String, Object> data = readSecret("pathops/control-plane/jenkins");

        return new JenkinsCredentials(
            stringValue(data, "base_url"),
            stringValue(data, "username"),
            stringValue(data, "api_token")
        );
    }

    private String stringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalStateException("Vault secret missing key: " + key);
        }
        return value.toString();
    }
}