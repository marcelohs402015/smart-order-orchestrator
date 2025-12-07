package com.marcelo.orchestrator.mcp.tools;

import com.marcelo.orchestrator.mcp.analyzer.PatternDetector;
import com.marcelo.orchestrator.mcp.server.McpTool;
import com.marcelo.orchestrator.mcp.server.McpToolResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Ferramenta MCP para análise de design patterns.
 *
 * <p>Detecta design patterns no código e verifica conformidade com SOLID.</p>
 *
 * @author Marcelo Hernandes da Silva
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PatternAnalysisTool {

    private final PatternDetector patternDetector;

    public McpTool getToolDefinition() {
        Map<String, Object> inputSchema = Map.of(
            "type", "object",
            "properties", Map.of(
                "file_path", Map.of(
                    "type", "string",
                    "description", "Caminho do arquivo Java a ser analisado"
                ),
                "check_solid", Map.of(
                    "type", "boolean",
                    "description", "Verificar princípios SOLID",
                    "default", true
                )
            ),
            "required", List.of("file_path")
        );

        return McpTool.builder()
            .name("analyze_patterns")
            .description("Analisa código Java para detectar design patterns (Factory, Adapter, Strategy, etc.) e verificar conformidade com princípios SOLID.")
            .inputSchema(inputSchema)
            .examples(java.util.Arrays.asList(
                "analyze_patterns(file_path='EventPublisherFactory.java', check_solid=true)"
            ))
            .build();
    }

    public McpToolResult execute(Map<String, Object> arguments) {
        try {
            String filePath = (String) arguments.get("file_path");
            boolean checkSolid = arguments.containsKey("check_solid") ? (Boolean) arguments.get("check_solid") : true;

            log.info("Analyzing patterns in: {}", filePath);

            Path path = resolveFilePath(filePath);
            if (!Files.exists(path)) {
                return McpToolResult.error("File not found: " + filePath);
            }

            String code = Files.readString(path);
            Map<String, Object> analysis = patternDetector.analyze(code, checkSolid);

            String content = buildPatternAnalysisContent(analysis);

            return McpToolResult.success(content, null, analysis);

        } catch (Exception e) {
            log.error("Error analyzing patterns", e);
            return McpToolResult.error("Error: " + e.getMessage());
        }
    }

    private Path resolveFilePath(String filePath) {
        if (Paths.get(filePath).isAbsolute()) {
            return Paths.get(filePath);
        }
        return Paths.get("..", filePath).normalize();
    }

    private String buildPatternAnalysisContent(Map<String, Object> analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Pattern Analysis Report\n\n");

        @SuppressWarnings("unchecked")
        List<String> patterns = (List<String>) analysis.getOrDefault("patterns", java.util.Collections.emptyList());
        if (!patterns.isEmpty()) {
            sb.append("## Design Patterns Detected\n");
            patterns.forEach(pattern -> sb.append("- ").append(pattern).append("\n"));
            sb.append("\n");
        }

        @SuppressWarnings("unchecked")
        Map<String, Boolean> solid = (Map<String, Boolean>) analysis.getOrDefault("solid", Map.of());
        if (!solid.isEmpty()) {
            sb.append("## SOLID Principles\n");
            solid.forEach((principle, compliant) -> 
                sb.append("- ").append(principle).append(": ").append(compliant ? "✅" : "❌").append("\n")
            );
        }

        return sb.toString();
    }
}

