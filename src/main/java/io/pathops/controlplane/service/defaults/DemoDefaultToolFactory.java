package io.pathops.controlplane.service.defaults;

import java.util.List;

import org.springframework.stereotype.Component;

import io.pathops.controlplane.config.DemoToolDefaultsProperties;
import io.pathops.controlplane.model.ToolProjectionScope;
import io.pathops.controlplane.model.ToolProvider;
import io.pathops.controlplane.model.ToolType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DemoDefaultToolFactory implements DefaultToolFactory {

    private final DemoToolDefaultsProperties properties;

    @Override
    public List<DefaultToolDefinition> createDefaults() {
        return List.of(
            keycloakDefinition(),
            jenkinsDefinition()
        );
    }

    private DefaultToolDefinition keycloakDefinition() {
        return new DefaultToolDefinition(
            "keycloak",
            ToolType.IDENTITY_PROVIDER,
            ToolProvider.KEYCLOAK,
            properties.getKeycloakBaseUrl(),
            List.of(
                new DefaultToolProjectionDefinition(ToolProjectionScope.TENANT, true),
                new DefaultToolProjectionDefinition(ToolProjectionScope.MEMBERSHIP, true)
            )
        );
    }

    private DefaultToolDefinition jenkinsDefinition() {
        return new DefaultToolDefinition(
            "jenkins",
            ToolType.CI_CD,
            ToolProvider.JENKINS,
            properties.getJenkinsBaseUrl(),
            List.of(
                new DefaultToolProjectionDefinition(ToolProjectionScope.TENANT, true),
                new DefaultToolProjectionDefinition(ToolProjectionScope.MEMBERSHIP, true)
            )
        );
    }
}