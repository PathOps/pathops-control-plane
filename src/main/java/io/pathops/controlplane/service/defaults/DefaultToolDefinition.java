package io.pathops.controlplane.service.defaults;

import java.util.List;

import io.pathops.controlplane.model.ToolProvider;
import io.pathops.controlplane.model.ToolType;

public record DefaultToolDefinition(
    String name,
    ToolType type,
    ToolProvider provider,
    String baseUrl,
    List<DefaultToolProjectionDefinition> projections
) {
}