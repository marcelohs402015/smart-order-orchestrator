package com.marcelo.orchestrator.mcp.controller;

import com.marcelo.orchestrator.mcp.server.McpServer;
import com.marcelo.orchestrator.mcp.server.McpTool;
import com.marcelo.orchestrator.mcp.server.McpToolResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST para expor o servidor MCP via HTTP.
 *
 * <p>Este controller permite que clientes MCP (Claude, GPT-4, GitHub Copilot)
 * interajam com o servidor via JSON-RPC 2.0 sobre HTTP.</p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li>{@code GET /mcp/tools} - Lista ferramentas disponíveis</li>
 *   <li>{@code POST /mcp/tools/execute} - Executa uma ferramenta</li>
 * </ul>
 *
 * @author Marcelo Hernandes da Silva
 */
@Slf4j
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpServer mcpServer;

    /**
     * Lista todas as ferramentas disponíveis no servidor MCP.
     */
    @GetMapping("/tools")
    public ResponseEntity<List<McpTool>> listTools() {
        return ResponseEntity.ok(mcpServer.listTools());
    }

    /**
     * Executa uma ferramenta MCP.
     *
     * @param request Requisição com nome da ferramenta e argumentos
     * @return Resultado da execução
     */
    @PostMapping("/tools/execute")
    public ResponseEntity<McpToolResult> executeTool(@RequestBody Map<String, Object> request) {
        String toolName = (String) request.get("tool");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) request.getOrDefault("arguments", Map.of());

        McpToolResult result = mcpServer.executeTool(toolName, arguments);
        return ResponseEntity.ok(result);
    }
}

