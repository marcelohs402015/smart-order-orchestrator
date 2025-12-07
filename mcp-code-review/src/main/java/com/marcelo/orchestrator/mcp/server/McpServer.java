package com.marcelo.orchestrator.mcp.server;

import com.marcelo.orchestrator.mcp.tools.CodeReviewTool;
import com.marcelo.orchestrator.mcp.tools.PatternAnalysisTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Servidor MCP que expõe ferramentas de code review.
 *
 * <p>Implementa o protocolo MCP (Model Context Protocol) para permitir que
 * modelos de IA (Claude, GPT-4, GitHub Copilot) realizem code review automatizado.</p>
 *
 * <h3>Protocolo MCP:</h3>
 * <ul>
 *   <li><strong>JSON-RPC 2.0:</strong> Protocolo de comunicação</li>
 *   <li><strong>Tools:</strong> Ferramentas expostas para o modelo de IA</li>
 *   <li><strong>Resources:</strong> Recursos acessíveis (código fonte, documentação)</li>
 *   <li><strong>Prompts:</strong> Templates de prompts para análise</li>
 * </ul>
 *
 * <h3>Ferramentas Disponíveis:</h3>
 * <ul>
 *   <li>{@code review_code}: Análise completa de código com feedback estruturado</li>
 *   <li>{@code analyze_patterns}: Detecção de design patterns e verificação SOLID</li>
 * </ul>
 *
 * @author Marcelo Hernandes da Silva
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpServer {

    private final CodeReviewTool codeReviewTool;
    private final PatternAnalysisTool patternAnalysisTool;

    /**
     * Lista todas as ferramentas disponíveis no servidor MCP.
     *
     * @return Lista de ferramentas com suas definições
     */
    public List<McpTool> listTools() {
        return List.of(
            codeReviewTool.getToolDefinition(),
            patternAnalysisTool.getToolDefinition()
        );
    }

    /**
     * Executa uma ferramenta MCP.
     *
     * @param toolName Nome da ferramenta a ser executada
     * @param arguments Argumentos da ferramenta
     * @return Resultado da execução da ferramenta
     */
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

