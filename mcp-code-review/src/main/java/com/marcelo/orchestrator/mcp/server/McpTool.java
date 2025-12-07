package com.marcelo.orchestrator.mcp.server;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Definição de uma ferramenta MCP.
 *
 * <p>Representa uma ferramenta que pode ser chamada por modelos de IA
 * através do protocolo MCP.</p>
 *
 * @author Marcelo Hernandes da Silva
 */
@Getter
@Builder
public class McpTool {
    private final String name;
    private final String description;
    private final Map<String, Object> inputSchema; // JSON Schema
    private final List<String> examples;
}

