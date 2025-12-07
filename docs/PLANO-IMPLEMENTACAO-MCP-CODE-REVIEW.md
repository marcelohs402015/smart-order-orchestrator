# Plano de Implementação - MCP Code Review Server

> **🤖 Integração de IA para Code Review**  
> Este documento apresenta o plano completo para implementar um servidor MCP (Model Context Protocol) que realiza code review automatizado usando IA, demonstrando expertise em Engenharia de IA.

---

## 📊 Resumo Executivo

### O que é MCP Code Review?

**MCP (Model Context Protocol)** é um protocolo da Anthropic que permite que modelos de IA (Claude, GPT-4, GitHub Copilot) interajam com ferramentas externas. Um **MCP Code Review Server** é um servidor que expõe ferramentas de análise de código para esses modelos de IA.

### Por que Implementar?

1. **Demonstra Expertise em IA** - Alinhado com sua pós-graduação em Engenharia de IA
2. **Diferencial Competitivo** - Poucos projetos demonstram MCP implementado
3. **Valor Prático** - Code review automatizado e feedback consistente
4. **Tecnologia Emergente** - MCP é padrão usado por GitHub Copilot e Claude

### Como Funciona?

```
Desenvolvedor → Claude/Copilot → MCP Server → Análise de Código → Feedback Estruturado
```

**Exemplo:**
- Desenvolvedor: "Review OrderSagaOrchestrator.java focusing on SOLID"
- Claude chama MCP Server via JSON-RPC
- MCP Server analisa código (AST + IA) e retorna feedback estruturado
- Claude apresenta feedback ao desenvolvedor

### Tempo de Implementação

- **Total:** 15-21 horas
- **Fases:** 6 fases bem definidas
- **Complexidade:** Média (requer conhecimento de Spring AI, JavaParser, MCP)

### Estrutura Proposta

```
smart-order-orchestrator/
├── backend/              # Backend principal (existente)
├── frontend/             # Frontend (existente)
└── mcp-code-review/      # 🆕 Servidor MCP (novo módulo)
    ├── src/main/java/
    │   └── com/marcelo/orchestrator/mcp/
    │       ├── server/      # Servidor MCP
    │       ├── tools/       # Ferramentas (review_code, analyze_patterns)
    │       ├── analyzer/    # Analisadores (SOLID, Patterns, Architecture)
    │       └── ai/          # Integração IA (OpenAI, Claude)
    └── README.md
```

### Tecnologias

- **Spring Boot 3.2+** - Framework base
- **Spring AI** - Suporte nativo para MCP Server
- **JavaParser** - Análise AST de código Java
- **OpenAI/Claude API** - Feedback contextualizado com IA

### Benefícios para Entrevistas

- ✅ Demonstra conhecimento prático de IA em engenharia
- ✅ Mostra capacidade de integrar tecnologias emergentes
- ✅ Alinhado com pós-graduação em Engenharia de IA
- ✅ Diferencial competitivo (poucos projetos têm MCP)

---

## 📋 Índice

1. [O que é MCP e Como Funciona](#o-que-é-mcp-e-como-funciona)
2. [Por que Implementar MCP Code Review](#por-que-implementar-mcp-code-review)
3. [Arquitetura da Solução](#arquitetura-da-solução)
4. [Plano de Implementação Detalhado](#plano-de-implementação-detalhado)
5. [Integração com o Projeto Atual](#integração-com-o-projeto-atual)
6. [Benefícios e Diferenciais](#benefícios-e-diferenciais)
7. [Como Funciona na Prática](#como-funciona-na-prática)

---

## 🔍 O que é MCP e Como Funciona

### Model Context Protocol (MCP)

**Definição:**
MCP é um protocolo padrão desenvolvido pela Anthropic que permite que modelos de IA (Claude, GPT-4, etc.) interajam com ferramentas, APIs e sistemas externos de forma estruturada e segura.

### Componentes do MCP

```
┌─────────────────────────────────────────────────────────┐
│                    Cliente IA (Claude, GPT-4)             │
│                    ou GitHub Copilot                     │
└────────────────────┬────────────────────────────────────┘
                     │ JSON-RPC 2.0
                     │ (tools, prompts, resources)
                     ▼
┌─────────────────────────────────────────────────────────┐
│              Servidor MCP (Nossa Implementação)          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │   Tools      │  │   Prompts    │  │  Resources   │ │
│  │ (Code Review)│  │ (Templates)  │  │  (Codebase)  │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│              Sistema Externo (Nosso Projeto)            │
│  - Análise de código                                    │
│  - Verificação de padrões                               │
│  - Sugestões de melhoria                                │
└─────────────────────────────────────────────────────────┘
```

### Fluxo de Funcionamento

1. **Cliente IA solicita code review:**
   ```json
   {
     "method": "tools/call",
     "params": {
       "name": "review_code",
       "arguments": {
         "file_path": "OrderSagaOrchestrator.java",
         "focus": "SOLID principles"
       }
     }
   }
   ```

2. **Servidor MCP processa:**
   - Lê o arquivo do código
   - Analisa usando regras e padrões
   - Gera feedback estruturado

3. **Resposta ao cliente IA:**
   ```json
   {
     "result": {
       "score": 8.5,
       "issues": [
         {
           "type": "SOLID",
           "severity": "info",
           "message": "SRP bem aplicado",
           "suggestion": "..."
         }
       ],
       "strengths": [...],
       "improvements": [...]
     }
   }
   ```

---

## 🎯 Por que Implementar MCP Code Review

### 1. **Demonstra Expertise em IA**
- ✅ Aplicação prática de IA em engenharia de software
- ✅ Integração de modelos de IA com sistemas reais
- ✅ Alinhado com sua pós-graduação em Engenharia de IA

### 2. **Diferencial Competitivo**
- ✅ Poucos projetos demonstram MCP implementado
- ✅ Mostra conhecimento de tecnologias emergentes
- ✅ Demonstra capacidade de inovação

### 3. **Valor Prático**
- ✅ Code review automatizado
- ✅ Feedback consistente e objetivo
- ✅ Aprendizado contínuo de padrões

### 4. **Alinhamento com Big Techs**
- ✅ GitHub Copilot usa MCP
- ✅ Anthropic Claude usa MCP
- ✅ Prática moderna de desenvolvimento

---

## 🏗️ Arquitetura da Solução

### Estrutura de Pastas Proposta

```
smart-order-orchestrator/
├── backend/                    # Backend existente
│   └── ...
├── frontend/                   # Frontend existente
│   └── ...
├── mcp-code-review/            # 🆕 NOVO: Servidor MCP
│   ├── pom.xml                 # Maven com Spring AI
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/marcelo/orchestrator/mcp/
│   │       │       ├── McpCodeReviewApplication.java
│   │       │       ├── server/              # Servidor MCP
│   │       │       │   ├── McpServerConfig.java
│   │       │       │   └── McpServerController.java
│   │       │       ├── tools/               # Ferramentas MCP
│   │       │       │   ├── CodeReviewTool.java
│   │       │       │   ├── PatternAnalysisTool.java
│   │       │       │   ├── SecurityAnalysisTool.java
│   │       │       │   └── PerformanceAnalysisTool.java
│   │       │       ├── analyzer/            # Analisadores
│   │       │       │   ├── CodeAnalyzer.java
│   │       │       │   ├── SolidAnalyzer.java
│   │       │       │   ├── PatternAnalyzer.java
│   │       │       │   └── ArchitectureAnalyzer.java
│   │       │       ├── ai/                  # Integração IA
│   │       │       │   ├── AiReviewService.java
│   │       │       │   ├── PromptTemplates.java
│   │       │       │   └── adapter/
│   │       │       │       ├── OpenAIService.java
│   │       │       │       └── ClaudeService.java
│   │       │       └── model/               # Modelos de resposta
│   │       │           ├── ReviewResult.java
│   │       │           ├── CodeIssue.java
│   │       │           └── ReviewScore.java
│   │       └── resources/
│   │           ├── application.yml
│   │           └── prompts/                # Templates de prompts
│   │               ├── code-review.txt
│   │               ├── solid-analysis.txt
│   │               └── pattern-check.txt
│   └── README.md
└── docs/
    └── PLANO-IMPLEMENTACAO-MCP-CODE-REVIEW.md (este arquivo)
```

### Camadas da Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│              Cliente MCP (Claude, Copilot, etc.)         │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP/WebSocket
                     │ JSON-RPC 2.0
                     ▼
┌─────────────────────────────────────────────────────────┐
│         MCP Server (Spring Boot + Spring AI)             │
│  ┌──────────────────────────────────────────────────┐  │
│  │  MCP Controller (JSON-RPC Handler)               │  │
│  └──────────────┬───────────────────────────────────┘  │
│                 │                                       │
│  ┌──────────────▼───────────────────────────────────┐  │
│  │  Tools Registry (Code Review Tools)              │  │
│  │  - review_code                                   │  │
│  │  - analyze_patterns                              │  │
│  │  - check_security                                │  │
│  │  - analyze_performance                           │  │
│  └──────────────┬───────────────────────────────────┘  │
│                 │                                       │
│  ┌──────────────▼───────────────────────────────────┐  │
│  │  Analyzers (Análise de Código)                   │  │
│  │  - CodeAnalyzer (AST parsing)                    │  │
│  │  - SolidAnalyzer (SOLID principles)               │  │
│  │  - PatternAnalyzer (Design patterns)             │  │
│  │  - ArchitectureAnalyzer (Hexagonal, Clean)        │  │
│  └──────────────┬───────────────────────────────────┘  │
│                 │                                       │
│  ┌──────────────▼───────────────────────────────────┐  │
│  │  AI Service (OpenAI/Claude Integration)          │  │
│  │  - Gera feedback contextualizado                │  │
│  │  - Sugestões de melhoria                        │  │
│  │  - Explicações didáticas                        │  │
│  └──────────────┬───────────────────────────────────┘  │
└─────────────────┼───────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────┐
│         Codebase (Backend do Projeto)                     │
│  - Lê arquivos Java                                       │
│  - Analisa estrutura                                      │
│  - Verifica padrões                                       │
└─────────────────────────────────────────────────────────┘
```

---

## 📝 Plano de Implementação Detalhado

### Fase 1: Setup e Configuração Base (2-3 horas)

#### 1.1. Criar Módulo MCP
```bash
cd smart-order-orchestrator
mkdir mcp-code-review
cd mcp-code-review
```

#### 1.2. Configurar `pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.marcelo.orchestrator</groupId>
    <artifactId>mcp-code-review</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    
    <properties>
        <java.version>21</java.version>
        <spring-ai.version>1.0.0</spring-ai.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring AI MCP Server -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
            <version>${spring-ai.version}</version>
        </dependency>
        
        <!-- OpenAI Integration (opcional) -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
            <version>${spring-ai.version}</version>
        </dependency>
        
        <!-- JavaParser para análise AST -->
        <dependency>
            <groupId>com.github.javaparser</groupId>
            <artifactId>javaparser-core</artifactId>
            <version>3.25.7</version>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Testes -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

#### 1.3. Configurar `application.yml`
```yaml
server:
  port: 8081  # Porta diferente do backend principal

spring:
  application:
    name: mcp-code-review-server
  
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4-turbo-preview
          temperature: 0.3  # Mais determinístico para code review

mcp:
  code-review:
    # Caminho para o código a ser analisado
    codebase-path: ../backend/src/main/java
    # Regras de análise
    rules:
      solid: true
      patterns: true
      security: true
      performance: true
    # Limites
    max-file-size-kb: 500
    supported-extensions: .java,.kt
```

---

### Fase 2: Implementar Servidor MCP Base (3-4 horas)

#### 2.1. Classe Principal
```java
package com.marcelo.orchestrator.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class McpCodeReviewApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpCodeReviewApplication.class, args);
    }
}
```

#### 2.2. Configuração do Servidor MCP
```java
package com.marcelo.orchestrator.mcp.server;

import org.springframework.ai.mcp.server.McpServer;
import org.springframework.ai.mcp.server.McpServerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {
    
    @Bean
    public McpServerConfigurer mcpServerConfigurer() {
        return server -> {
            // Registrar tools (ferramentas)
            server.tool("review_code", "Analisa código Java e fornece feedback sobre qualidade, padrões e melhorias");
            server.tool("analyze_patterns", "Identifica padrões de design no código");
            server.tool("check_solid", "Verifica aplicação de princípios SOLID");
            server.tool("analyze_architecture", "Analisa arquitetura (Hexagonal, Clean)");
            
            // Registrar resources (recursos)
            server.resource("codebase", "Acesso ao código fonte do projeto");
        };
    }
}
```

#### 2.3. Controller MCP (JSON-RPC Handler)
```java
package com.marcelo.orchestrator.mcp.server;

import com.marcelo.orchestrator.mcp.tools.CodeReviewTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.server.McpTool;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class McpServerController {
    
    private final CodeReviewTool codeReviewTool;
    
    @PostMapping("/tools/call")
    public Map<String, Object> callTool(@RequestBody Map<String, Object> request) {
        String toolName = (String) request.get("name");
        Map<String, Object> arguments = (Map<String, Object>) request.get("arguments");
        
        return switch (toolName) {
            case "review_code" -> codeReviewTool.reviewCode(arguments);
            case "analyze_patterns" -> codeReviewTool.analyzePatterns(arguments);
            case "check_solid" -> codeReviewTool.checkSolid(arguments);
            case "analyze_architecture" -> codeReviewTool.analyzeArchitecture(arguments);
            default -> Map.of("error", "Unknown tool: " + toolName);
        };
    }
}
```

---

### Fase 3: Implementar Ferramentas de Code Review (4-5 horas)

#### 3.1. Code Review Tool Principal
```java
package com.marcelo.orchestrator.mcp.tools;

import com.marcelo.orchestrator.mcp.analyzer.CodeAnalyzer;
import com.marcelo.orchestrator.mcp.ai.AiReviewService;
import com.marcelo.orchestrator.mcp.model.ReviewResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CodeReviewTool {
    
    private final CodeAnalyzer codeAnalyzer;
    private final AiReviewService aiReviewService;
    
    /**
     * Ferramenta MCP: review_code
     * Analisa código Java e fornece feedback estruturado.
     */
    public Map<String, Object> reviewCode(Map<String, Object> arguments) {
        try {
            String filePath = (String) arguments.get("file_path");
            String focus = (String) arguments.getOrDefault("focus", "all");
            
            log.info("Reviewing code: {} (focus: {})", filePath, focus);
            
            // Ler arquivo
            Path path = Paths.get(filePath);
            String code = Files.readString(path);
            
            // Análise estática
            ReviewResult staticAnalysis = codeAnalyzer.analyze(code, filePath);
            
            // Análise com IA (contextualizada)
            ReviewResult aiAnalysis = aiReviewService.reviewWithAI(code, filePath, focus);
            
            // Combinar resultados
            ReviewResult combined = ReviewResult.combine(staticAnalysis, aiAnalysis);
            
            return Map.of(
                "score", combined.getScore(),
                "issues", combined.getIssues(),
                "strengths", combined.getStrengths(),
                "improvements", combined.getImprovements(),
                "patterns_found", combined.getPatternsFound(),
                "solid_compliance", combined.getSolidCompliance()
            );
            
        } catch (Exception e) {
            log.error("Error reviewing code", e);
            return Map.of("error", e.getMessage());
        }
    }
    
    public Map<String, Object> analyzePatterns(Map<String, Object> arguments) {
        // Implementação similar
    }
    
    public Map<String, Object> checkSolid(Map<String, Object> arguments) {
        // Implementação similar
    }
    
    public Map<String, Object> analyzeArchitecture(Map<String, Object> arguments) {
        // Implementação similar
    }
}
```

#### 3.2. Code Analyzer (Análise Estática)
```java
package com.marcelo.orchestrator.mcp.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.marcelo.orchestrator.mcp.model.CodeIssue;
import com.marcelo.orchestrator.mcp.model.ReviewResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CodeAnalyzer {
    
    private final SolidAnalyzer solidAnalyzer;
    private final PatternAnalyzer patternAnalyzer;
    private final ArchitectureAnalyzer architectureAnalyzer;
    
    public ReviewResult analyze(String code, String filePath) {
        try {
            CompilationUnit cu = new JavaParser().parse(code).getResult().orElseThrow();
            
            List<CodeIssue> issues = new ArrayList<>();
            List<String> strengths = new ArrayList<>();
            List<String> improvements = new ArrayList<>();
            
            // Análise SOLID
            solidAnalyzer.analyze(cu, issues, strengths);
            
            // Análise de Padrões
            patternAnalyzer.analyze(cu, issues, strengths);
            
            // Análise de Arquitetura
            architectureAnalyzer.analyze(cu, filePath, issues, strengths);
            
            // Calcular score
            double score = calculateScore(issues, strengths);
            
            return ReviewResult.builder()
                .score(score)
                .issues(issues)
                .strengths(strengths)
                .improvements(improvements)
                .build();
                
        } catch (Exception e) {
            log.error("Error analyzing code", e);
            throw new RuntimeException("Failed to analyze code", e);
        }
    }
    
    private double calculateScore(List<CodeIssue> issues, List<String> strengths) {
        // Lógica de cálculo de score
        double baseScore = 10.0;
        
        // Penalizar por issues
        for (CodeIssue issue : issues) {
            baseScore -= issue.getSeverity().getWeight();
        }
        
        // Bonificar por strengths
        baseScore += strengths.size() * 0.1;
        
        return Math.max(0, Math.min(10, baseScore));
    }
}
```

#### 3.3. SOLID Analyzer
```java
package com.marcelo.orchestrator.mcp.analyzer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.marcelo.orchestrator.mcp.model.CodeIssue;
import com.marcelo.orchestrator.mcp.model.IssueSeverity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SolidAnalyzer {
    
    public void analyze(CompilationUnit cu, List<CodeIssue> issues, List<String> strengths) {
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
            // Single Responsibility Principle
            checkSRP(clazz, issues, strengths);
            
            // Open/Closed Principle
            checkOCP(clazz, issues, strengths);
            
            // Liskov Substitution Principle
            checkLSP(clazz, issues, strengths);
            
            // Interface Segregation Principle
            checkISP(clazz, issues, strengths);
            
            // Dependency Inversion Principle
            checkDIP(clazz, issues, strengths);
        });
    }
    
    private void checkSRP(ClassOrInterfaceDeclaration clazz, List<CodeIssue> issues, List<String> strengths) {
        long methodCount = clazz.getMethods().size();
        
        if (methodCount > 15) {
            issues.add(CodeIssue.builder()
                .type("SOLID")
                .principle("SRP")
                .severity(IssueSeverity.WARNING)
                .message("Classe tem muitos métodos (" + methodCount + "). Pode violar SRP.")
                .suggestion("Considere dividir em classes menores com responsabilidades únicas.")
                .build());
        } else {
            strengths.add("SRP bem aplicado: classe tem responsabilidade única");
        }
    }
    
    // Implementar outros checks (OCP, LSP, ISP, DIP)
}
```

---

### Fase 4: Integração com IA (3-4 horas)

#### 4.1. AI Review Service
```java
package com.marcelo.orchestrator.mcp.ai;

import com.marcelo.orchestrator.mcp.model.ReviewResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiReviewService {
    
    private final ChatClient chatClient;
    private final PromptTemplates promptTemplates;
    
    public ReviewResult reviewWithAI(String code, String filePath, String focus) {
        try {
            // Carregar template de prompt
            PromptTemplate template = promptTemplates.getCodeReviewTemplate(focus);
            
            // Criar prompt com contexto
            Prompt prompt = template.create(Map.of(
                "code", code,
                "filePath", filePath,
                "focus", focus,
                "architecture", "Hexagonal Architecture",
                "patterns", "Saga, Factory, Adapter, Repository"
            ));
            
            // Chamar IA
            String aiResponse = chatClient.call(prompt).getResult().getOutput().getContent();
            
            // Parsear resposta estruturada
            return parseAIResponse(aiResponse);
            
        } catch (Exception e) {
            log.error("Error in AI review", e);
            throw new RuntimeException("Failed to get AI review", e);
        }
    }
    
    private ReviewResult parseAIResponse(String response) {
        // Parsear JSON ou texto estruturado da IA
        // Implementar parser baseado no formato de resposta
    }
}
```

#### 4.2. Prompt Templates
```java
package com.marcelo.orchestrator.mcp.ai;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class PromptTemplates {
    
    private final ResourceLoader resourceLoader;
    
    public PromptTemplate getCodeReviewTemplate(String focus) throws IOException {
        String templatePath = switch (focus) {
            case "SOLID" -> "classpath:prompts/solid-analysis.txt";
            case "patterns" -> "classpath:prompts/pattern-check.txt";
            default -> "classpath:prompts/code-review.txt";
        };
        
        Resource resource = resourceLoader.getResource(templatePath);
        String template = resource.getContentAsString(StandardCharsets.UTF_8);
        
        return new PromptTemplate(template);
    }
}
```

#### 4.3. Template de Prompt (`prompts/code-review.txt`)
```
Você é um especialista em code review para sistemas Java enterprise.

Analise o seguinte código Java e forneça feedback estruturado:

**Código:**
{code}

**Contexto:**
- Arquivo: {filePath}
- Arquitetura: {architecture}
- Padrões esperados: {patterns}

**Foco da análise:** {focus}

**Forneça:**
1. Score de 0-10
2. Issues encontradas (tipo, severidade, mensagem, sugestão)
3. Pontos fortes
4. Melhorias sugeridas
5. Padrões identificados
6. Conformidade SOLID

**Formato de resposta (JSON):**
{
  "score": 8.5,
  "issues": [
    {
      "type": "SOLID",
      "severity": "info",
      "message": "...",
      "suggestion": "..."
    }
  ],
  "strengths": [...],
  "improvements": [...],
  "patterns_found": [...],
  "solid_compliance": {
    "SRP": true,
    "OCP": true,
    "LSP": true,
    "ISP": true,
    "DIP": true
  }
}
```

---

### Fase 5: Modelos de Dados (1-2 horas)

#### 5.1. ReviewResult
```java
package com.marcelo.orchestrator.mcp.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReviewResult {
    private double score;  // 0-10
    private List<CodeIssue> issues;
    private List<String> strengths;
    private List<String> improvements;
    private List<String> patternsFound;
    private SolidCompliance solidCompliance;
    
    public static ReviewResult combine(ReviewResult staticAnalysis, ReviewResult aiAnalysis) {
        // Combinar resultados de análise estática e IA
        return ReviewResult.builder()
            .score((staticAnalysis.getScore() + aiAnalysis.getScore()) / 2)
            .issues(combineLists(staticAnalysis.getIssues(), aiAnalysis.getIssues()))
            .strengths(combineLists(staticAnalysis.getStrengths(), aiAnalysis.getStrengths()))
            .improvements(combineLists(staticAnalysis.getImprovements(), aiAnalysis.getImprovements()))
            .build();
    }
}
```

#### 5.2. CodeIssue
```java
package com.marcelo.orchestrator.mcp.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CodeIssue {
    private String type;  // SOLID, PATTERN, SECURITY, PERFORMANCE
    private String principle;  // SRP, OCP, etc.
    private IssueSeverity severity;  // INFO, WARNING, ERROR
    private String message;
    private String suggestion;
    private int lineNumber;  // Opcional
}

public enum IssueSeverity {
    INFO(0.1),
    WARNING(0.5),
    ERROR(1.0);
    
    private final double weight;
    
    IssueSeverity(double weight) {
        this.weight = weight;
    }
    
    public double getWeight() {
        return weight;
    }
}
```

---

### Fase 6: Testes e Documentação (2-3 horas)

#### 6.1. Testes Unitários
```java
package com.marcelo.orchestrator.mcp.tools;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
class CodeReviewToolTest {
    
    @Autowired
    private CodeReviewTool codeReviewTool;
    
    @Test
    void shouldReviewCode() {
        Map<String, Object> arguments = Map.of(
            "file_path", "../backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaOrchestrator.java",
            "focus", "SOLID"
        );
        
        Map<String, Object> result = codeReviewTool.reviewCode(arguments);
        
        assertNotNull(result.get("score"));
        assertNotNull(result.get("issues"));
    }
}
```

#### 6.2. README do Módulo
```markdown
# MCP Code Review Server

Servidor MCP (Model Context Protocol) para code review automatizado usando IA.

## Como Usar

1. Iniciar servidor:
```bash
cd mcp-code-review
mvn spring-boot:run
```

2. Conectar cliente MCP (Claude Desktop, GitHub Copilot):
```json
{
  "mcpServers": {
    "code-review": {
      "command": "curl",
      "args": ["http://localhost:8081/mcp"]
    }
  }
}
```

3. Usar em Claude/Copilot:
"Review the OrderSagaOrchestrator.java file focusing on SOLID principles"
```

---

## 🔗 Integração com o Projeto Atual

### Opção 1: Módulo Separado (Recomendado)

**Vantagens:**
- ✅ Separação de concerns
- ✅ Pode ser deployado separadamente
- ✅ Não afeta o backend principal
- ✅ Fácil de testar isoladamente

**Estrutura:**
```
smart-order-orchestrator/
├── backend/          # Backend principal
├── frontend/         # Frontend
└── mcp-code-review/  # Servidor MCP (novo módulo)
```

### Opção 2: Integração no Backend Existente

**Vantagens:**
- ✅ Tudo em um único projeto
- ✅ Compartilha dependências

**Desvantagens:**
- ⚠️ Mistura responsabilidades
- ⚠️ Porta diferente necessária

**Recomendação:** Opção 1 (módulo separado)

---

## 💎 Benefícios e Diferenciais

### 1. **Demonstra Expertise em IA**
- ✅ Aplicação prática de IA em engenharia
- ✅ Integração de modelos de IA (OpenAI, Claude)
- ✅ Alinhado com pós-graduação em Engenharia de IA

### 2. **Diferencial Competitivo**
- ✅ Poucos projetos demonstram MCP
- ✅ Tecnologia emergente (2024)
- ✅ Mostra capacidade de inovação

### 3. **Valor Prático**
- ✅ Code review automatizado
- ✅ Feedback consistente
- ✅ Aprendizado contínuo

### 4. **Alinhamento com Mercado**
- ✅ GitHub Copilot usa MCP
- ✅ Anthropic Claude usa MCP
- ✅ Prática moderna

---

## 🎯 Como Funciona na Prática

### Exemplo de Uso

**1. Desenvolvedor solicita review:**
```
Claude: "Review OrderSagaOrchestrator.java focusing on SOLID principles"
```

**2. Claude chama MCP:**
```json
POST http://localhost:8081/mcp/tools/call
{
  "name": "review_code",
  "arguments": {
    "file_path": "OrderSagaOrchestrator.java",
    "focus": "SOLID"
  }
}
```

**3. MCP Server processa:**
- Lê arquivo
- Analisa com JavaParser (AST)
- Chama OpenAI/Claude para feedback contextualizado
- Combina resultados

**4. Resposta estruturada:**
```json
{
  "score": 8.5,
  "issues": [
    {
      "type": "SOLID",
      "principle": "SRP",
      "severity": "info",
      "message": "Classe tem responsabilidade única bem definida",
      "suggestion": "Excelente aplicação de SRP"
    }
  ],
  "strengths": [
    "Saga Pattern bem implementado",
    "Idempotência corretamente aplicada",
    "Compensação automática funcionando"
  ],
  "improvements": [
    "Considerar adicionar checkpoint intermediário",
    "Documentar método compensate() com mais detalhes"
  ],
  "patterns_found": ["Saga", "Factory", "Adapter"],
  "solid_compliance": {
    "SRP": true,
    "OCP": true,
    "LSP": true,
    "ISP": true,
    "DIP": true
  }
}
```

**5. Claude apresenta ao desenvolvedor:**
> "Analisei o código e encontrei:
> - Score: 8.5/10
> - ✅ SRP bem aplicado
> - ✅ Saga Pattern implementado corretamente
> - 💡 Sugestão: Adicionar checkpoint intermediário para resiliência"

---

## 📊 Resumo do Plano

| Fase | Tarefa | Tempo Estimado | Prioridade |
|------|--------|----------------|------------|
| **Fase 1** | Setup e Configuração Base | 2-3h | Alta |
| **Fase 2** | Servidor MCP Base | 3-4h | Alta |
| **Fase 3** | Ferramentas de Code Review | 4-5h | Alta |
| **Fase 4** | Integração com IA | 3-4h | Alta |
| **Fase 5** | Modelos de Dados | 1-2h | Média |
| **Fase 6** | Testes e Documentação | 2-3h | Média |
| **Total** | | **15-21 horas** | |

---

## 🚀 Próximos Passos

1. **Decidir abordagem:** Módulo separado ou integrado?
2. **Configurar dependências:** Spring AI, JavaParser
3. **Implementar Fase 1:** Setup básico
4. **Testar integração:** Conectar com Claude Desktop
5. **Iterar e melhorar:** Adicionar mais ferramentas

---

## 📚 Referências

- [Model Context Protocol - Anthropic](https://modelcontextprotocol.io)
- [Spring AI MCP Server](https://docs.spring.io/spring-ai/reference/mcp-server.html)
- [JavaParser Documentation](https://javaparser.org)
- [MCP Specification](https://spec.modelcontextprotocol.io)

---

**Autor:** Marcelo  
**Data:** 2024  
**Versão:** 1.0  
**Status:** Plano de Implementação

