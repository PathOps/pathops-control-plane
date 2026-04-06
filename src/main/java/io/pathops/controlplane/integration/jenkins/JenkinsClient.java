package io.pathops.controlplane.integration.jenkins;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.pathops.controlplane.integration.vault.JenkinsCredentials;
import io.pathops.controlplane.integration.vault.VaultSecretService;
import io.pathops.controlplane.model.MembershipRole;

@Component
public class JenkinsClient {

    private final RestTemplate restTemplate;
    private final VaultSecretService vaultSecretService;

    public JenkinsClient(RestTemplate pathopsRestTemplate, VaultSecretService vaultSecretService) {
        this.restTemplate = pathopsRestTemplate;
        this.vaultSecretService = vaultSecretService;
    }

    public String ensureTenantFolderExists(String tenantSlug) {
        JenkinsCredentials creds = vaultSecretService.readJenkinsCredentials();

        String rootFolder = "tenants";
        ensureFolderExists(creds, rootFolder);

        String folderPath = "tenants/job/" + tenantSlug;
        if (!folderExists(creds, folderPath)) {
            createFolder(creds, "tenants", tenantSlug);
        }

        return folderPath;
    }

    public void ensureTenantAccessProjection(String tenantSlug, MembershipRole membershipRole) {
        String principalName = "tenant:" + tenantSlug;
        String itemRoleName = itemRoleName(tenantSlug, membershipRole);
        String itemPattern = "^tenants/job/" + tenantSlug + "(/.*)?$";

        // Placeholder intencional:
        // acá luego integrarás Role Strategy Plugin o JCasC según cómo termines
        // materializando groups/authorities desde OIDC hacia Jenkins.
        //
        // Por ahora dejamos el contrato listo y el folder creado.
        //
        // Ejemplo conceptual:
        // ensureItemRoleExists(itemRoleName, itemPattern, permissionsFor(membershipRole));
        // assignRoleToPrincipal(itemRoleName, principalName);
    }

    private String itemRoleName(String tenantSlug, MembershipRole membershipRole) {
        return "tenant-" + tenantSlug + "-" + membershipRole.name().toLowerCase();
    }

    private void ensureFolderExists(JenkinsCredentials creds, String folderName) {
        if (!folderExists(creds, folderName)) {
            createFolder(creds, "", folderName);
        }
    }

    private boolean folderExists(JenkinsCredentials creds, String folderPath) {
        String url = creds.baseUrl() + "/" + folderPath + "/api/json";

        try {
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(creds)),
                String.class
            );
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void createFolder(JenkinsCredentials creds, String parentPath, String folderName) {
        String url = parentPath == null || parentPath.isBlank()
            ? creds.baseUrl() + "/createItem?name=" + folderName
            : creds.baseUrl() + "/" + parentPath + "/createItem?name=" + folderName;

        String xml = """
            <com.cloudbees.hudson.plugins.folder.Folder plugin="cloudbees-folder">
              <description></description>
              <properties/>
              <folderViews class="com.cloudbees.hudson.plugins.folder.views.DefaultFolderViewHolder">
                <views>
                  <hudson.model.AllView>
                    <owner class="com.cloudbees.hudson.plugins.folder.Folder" reference="../.."/>
                    <name>All</name>
                    <filterExecutors>false</filterExecutors>
                    <filterQueue>false</filterQueue>
                    <properties class="hudson.model.View$PropertyList"/>
                  </hudson.model.AllView>
                </views>
                <tabBar class="hudson.views.DefaultViewsTabBar"/>
              </folderViews>
              <healthMetrics/>
              <icon class="com.cloudbees.hudson.plugins.folder.icons.StockFolderIcon"/>
            </com.cloudbees.hudson.plugins.folder.Folder>
            """;

        HttpHeaders headers = authHeaders(creds);
        headers.setContentType(MediaType.APPLICATION_XML);

        restTemplate.exchange(
            url,
            HttpMethod.POST,
            new HttpEntity<>(xml, headers),
            String.class
        );
    }

    private HttpHeaders authHeaders(JenkinsCredentials creds) {
        String raw = creds.username() + ":" + creds.apiToken();
        String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encoded);
        return headers;
    }
}