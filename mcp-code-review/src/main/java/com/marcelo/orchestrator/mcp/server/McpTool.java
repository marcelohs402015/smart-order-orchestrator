package com.marcelo.orchestrator.mcp.server;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class McpTool {
    private final String name;
    private final String description;
    private final Map<String, Object> inputSchema;
    private final List<String> examples;
}
