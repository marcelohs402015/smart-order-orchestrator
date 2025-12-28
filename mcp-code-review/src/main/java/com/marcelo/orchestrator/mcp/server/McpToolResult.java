package com.marcelo.orchestrator.mcp.server;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class McpToolResult {
    private final boolean success;
    private final String content;
    private final List<String> issues;
    private final Map<String, Object> metadata;

    public static McpToolResult success(String content) {
        return McpToolResult.builder()
            .success(true)
            .content(content)
            .build();
    }

    public static McpToolResult success(String content, List<String> issues, Map<String, Object> metadata) {
        return McpToolResult.builder()
            .success(true)
            .content(content)
            .issues(issues != null ? issues : List.of())
            .metadata(metadata != null ? metadata : Map.of())
            .build();
    }

    public static McpToolResult error(String errorMessage) {
        return McpToolResult.builder()
            .success(false)
            .content("Error: " + errorMessage)
            .build();
    }
}
