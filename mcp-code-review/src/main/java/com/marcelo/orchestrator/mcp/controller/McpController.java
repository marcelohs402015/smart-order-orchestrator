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

@Slf4j
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpServer mcpServer;

    @GetMapping("/tools")
    public ResponseEntity<List<McpTool>> listTools() {
        return ResponseEntity.ok(mcpServer.listTools());
    }

    @PostMapping("/tools/execute")
    public ResponseEntity<McpToolResult> executeTool(@RequestBody Map<String, Object> request) {
        String toolName = (String) request.get("tool");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) request.getOrDefault("arguments", Map.of());

        McpToolResult result = mcpServer.executeTool(toolName, arguments);
        return ResponseEntity.ok(result);
    }
}
