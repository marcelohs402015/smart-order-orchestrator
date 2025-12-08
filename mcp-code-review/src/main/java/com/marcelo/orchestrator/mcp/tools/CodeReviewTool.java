package com.marcelo.orchestrator.mcp.tools;

import com.marcelo.orchestrator.mcp.analyzer.CodeAnalyzer;
import com.marcelo.orchestrator.mcp.ai.AiFeedbackService;
import com.marcelo.orchestrator.mcp.server.McpTool;
import com.marcelo.orchestrator.mcp.server.McpToolResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ferramenta MCP para code review completo.
 *
 * <p>Analisa código Java e fornece feedback estruturado sobre:
 * - Qualidade do código
 * - Padrões SOLID
 * - Boas práticas
 * - Sugestões de melhoria</p>
 *
 * <h3>Uso via MCP:</h3>
 * <pre>
 * {
 *   "tool": "review_code",
 *   "arguments": {
 *     "file_path": "backend/src/main/java/.../OrderSagaOrchestrator.java",
 *     "focus": "SOLID principles"
 *   }
 * }
 * </pre>
 *
 * @author Marcelo Hernandes da Silva
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodeReviewTool {

    private final CodeAnalyzer codeAnalyzer;
    private final AiFeedbackService aiFeedbackService;

    /**
     * Retorna a definição da ferramenta para o protocolo MCP.
     */
    public McpTool getToolDefinition() {
        Map<String, Object> inputSchema = Map.of(
            "type", "object",
            "properties", Map.of(
                "file_path", Map.of(
                    "type", "string",
                    "description", "Caminho do arquivo Java a ser analisado"
                ),
                "focus", Map.of(
                    "type", "string",
                    "description", "Foco da análise (ex: 'SOLID', 'patterns', 'architecture')",
                    "enum", List.of("SOLID", "patterns", "architecture", "all")
                )
            ),
            "required", List.of("file_path")
        );

        return McpTool.builder()
            .name("review_code")
            .description("Realiza code review completo de arquivos Java, analisando qualidade, padrões SOLID, design patterns e arquitetura. Retorna feedback estruturado com sugestões de melhoria.")
            .inputSchema(inputSchema)
            .examples(List.of(
                "review_code(file_path='OrderSagaOrchestrator.java', focus='SOLID')",
                "review_code(file_path='Order.java', focus='patterns')"
            ))
            .build();
    }

    /**
     * Executa a análise de código.
     *
     * @param arguments Argumentos da ferramenta (file_path, focus)
     * @return Resultado da análise
     */
    public McpToolResult execute(Map<String, Object> arguments) {
        try {
            String filePath = (String) arguments.get("file_path");
            String focus = (String) arguments.getOrDefault("focus", "all");

            log.info("Reviewing code: {} with focus: {}", filePath, focus);

            // Resolver caminho relativo ao projeto
            Path path = resolveFilePath(filePath);
            if (!Files.exists(path)) {
                return McpToolResult.error("File not found: " + filePath);
            }

            // Ler conteúdo do arquivo
            String code = Files.readString(path);

            // Análise estática com JavaParser
            Map<String, Object> analysis = codeAnalyzer.analyze(code, focus);

            // Feedback contextualizado com IA
            String aiFeedback = aiFeedbackService.generateFeedback(code, analysis, focus);

            // Construir resultado
            List<String> issues = extractIssues(analysis);
            Map<String, Object> metadata = new HashMap<>(analysis);
            metadata.put("file_path", filePath);
            metadata.put("focus", focus);

            String content = buildReviewContent(analysis, aiFeedback, issues);

            return McpToolResult.success(content, issues, metadata);

        } catch (Exception e) {
            log.error("Error executing code review", e);
            return McpToolResult.error("Error: " + e.getMessage());
        }
    }

    private Path resolveFilePath(String filePath) {
        // Se for caminho absoluto, usar diretamente
        if (Paths.get(filePath).isAbsolute()) {
            return Paths.get(filePath);
        }
        // Caso contrário, assumir relativo ao diretório raiz do projeto
        return Paths.get("..", filePath).normalize();
    }

    private List<String> extractIssues(Map<String, Object> analysis) {
        List<String> issues = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> violations = (List<Map<String, Object>>) analysis.getOrDefault("violations", List.of());
        for (Map<String, Object> violation : violations) {
            issues.add((String) violation.getOrDefault("message", "Unknown issue"));
        }

        return issues;
    }

    private String buildReviewContent(Map<String, Object> analysis, String aiFeedback, List<String> issues) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Code Review Report\n\n");
        
        if (!issues.isEmpty()) {
            sb.append("## Issues Found\n");
            issues.forEach(issue -> sb.append("- ").append(issue).append("\n"));
            sb.append("\n");
        }

        sb.append("## Analysis Summary\n");
        sb.append(analysis.getOrDefault("summary", "No summary available")).append("\n\n");

        sb.append("## AI Feedback\n");
        sb.append(aiFeedback);

        return sb.toString();
    }
}

