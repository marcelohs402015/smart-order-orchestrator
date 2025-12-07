package com.marcelo.orchestrator.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicação Spring Boot para o MCP Code Review Server.
 *
 * <p>Este servidor implementa o Model Context Protocol (MCP) para fornecer
 * ferramentas de code review automatizado usando IA.</p>
 *
 * <h3>O que é MCP?</h3>
 * <p>MCP (Model Context Protocol) é um protocolo padrão desenvolvido pela Anthropic
 * que permite que modelos de IA (Claude, GPT-4, GitHub Copilot) interajam com
 * ferramentas externas via JSON-RPC 2.0.</p>
 *
 * <h3>Funcionalidades:</h3>
 * <ul>
 *   <li>Análise de código Java usando JavaParser (AST)</li>
 *   <li>Verificação de padrões SOLID</li>
 *   <li>Detecção de design patterns</li>
 *   <li>Análise arquitetural (Hexagonal, Clean Architecture)</li>
 *   <li>Feedback contextualizado com IA (OpenAI/Claude)</li>
 * </ul>
 *
 * @author Marcelo Hernandes da Silva
 */
@SpringBootApplication
public class McpCodeReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpCodeReviewApplication.class, args);
    }
}

