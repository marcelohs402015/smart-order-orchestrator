package com.marcelo.orchestrator.mcp.server;

import com.marcelo.orchestrator.mcp.tools.CodeReviewTool;
import com.marcelo.orchestrator.mcp.tools.PatternAnalysisTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class McpServer {

    private final CodeReviewTool codeReviewTool;
    private final PatternAnalysisTool patternAnalysisTool;

    public List<McpTool> listTools() {
        return List.of(
            codeReviewTool.getToolDefinition(),
            patternAnalysisTool.getToolDefinition()
        );
    }

    public McpToolResult executeTool(String toolName, Map<String, Object> arguments) {
        log.info("Executing MCP tool: {} with arguments: {}", toolName, arguments);

        return switch (toolName) {
            case "review_code" -> codeReviewTool.execute(arguments);
            case "analyze_patterns" -> patternAnalysisTool.execute(arguments);
            default -> {
                log.error("Unknown tool: {}", toolName);
                yield McpToolResult.error("Unknown tool: " + toolName);
            }
        };
    }
}
