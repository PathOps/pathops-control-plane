package io.pathops.controlplane.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DemoToolDefaultsProperties {

    @Value("${pathops.demo.tools.keycloak.base-url}")
    private String keycloakBaseUrl;

    @Value("${pathops.demo.tools.jenkins.base-url}")
    private String jenkinsBaseUrl;

    public String getKeycloakBaseUrl() {
        return keycloakBaseUrl;
    }

    public String getJenkinsBaseUrl() {
        return jenkinsBaseUrl;
    }
}