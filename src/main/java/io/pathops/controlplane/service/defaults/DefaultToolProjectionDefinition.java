package io.pathops.controlplane.service.defaults;

import io.pathops.controlplane.model.ToolProjectionScope;

public record DefaultToolProjectionDefinition(
    ToolProjectionScope scope,
    boolean enabled
) {
}