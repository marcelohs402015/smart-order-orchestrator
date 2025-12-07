# MCP Code Review Server

> **🤖 Servidor MCP para Code Review Automatizado com IA**  
> Demonstra expertise em Engenharia de IA através da implementação do Model Context Protocol (MCP) para análise automatizada de código.

---

## 📋 O que é MCP?

**MCP (Model Context Protocol)** é um protocolo padrão desenvolvido pela Anthropic que permite que modelos de IA (Claude, GPT-4, GitHub Copilot) interajam com ferramentas externas via JSON-RPC 2.0.

Este servidor expõe ferramentas de code review que podem ser usadas por assistentes de IA para analisar código Java automaticamente.

---

## 🚀 Funcionalidades

### Ferramentas Disponíveis

1. **`review_code`** - Code review completo
   - Análise estática com JavaParser (AST)
   - Verificação de qualidade de código
   - Feedback contextualizado com IA
   - Sugestões de melhoria

2. **`analyze_patterns`** - Análise de design patterns
   - Detecção de design patterns (Factory, Adapter, Strategy, etc.)
   - Verificação de princípios SOLID
   - Análise arquitetural

---

## 🏗️ Arquitetura

```
mcp-code-review/
├── server/          # Servidor MCP (JSON-RPC 2.0)
├── tools/          # Ferramentas (review_code, analyze_patterns)
├── analyzer/        # Analisadores (CodeAnalyzer, PatternDetector)
├── ai/             # Integração IA (AiFeedbackService)
└── controller/     # REST API para expor MCP via HTTP
```

---

## 🛠️ Tecnologias

- **Spring Boot 3.2+** - Framework base
- **Spring AI** - Suporte nativo para MCP Server
- **JavaParser** - Análise AST de código Java
- **OpenAI GPT-4** - Feedback contextualizado com IA
- **JSON-RPC 2.0** - Protocolo de comunicação MCP

---

## 📦 Como Usar

### 1. Configurar Variáveis de Ambiente

```bash
export OPENAI_API_KEY=sua-chave-openai
```

### 2. Executar Servidor

```bash
cd mcp-code-review
mvn spring-boot:run
```

O servidor estará disponível em: `http://localhost:8081`

### 3. Usar via HTTP (JSON-RPC 2.0)

#### Listar Ferramentas

```bash
curl http://localhost:8081/mcp/tools
```

#### Executar Code Review

```bash
curl -X POST http://localhost:8081/mcp/tools/execute \
  -H "Content-Type: application/json" \
  -d '{
    "tool": "review_code",
    "arguments": {
      "file_path": "backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaOrchestrator.java",
      "focus": "SOLID"
    }
  }'
```

#### Analisar Patterns

```bash
curl -X POST http://localhost:8081/mcp/tools/execute \
  -H "Content-Type: application/json" \
  -d '{
    "tool": "analyze_patterns",
    "arguments": {
      "file_path": "backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/factory/EventPublisherFactory.java",
      "check_solid": true
    }
  }'
```

---

## 🔌 Integração com Claude/GPT-4

### Via Claude Desktop

1. Configure o servidor MCP no arquivo de configuração do Claude
2. O Claude poderá chamar as ferramentas diretamente

**Exemplo de uso:**
```
Usuário: "Review OrderSagaOrchestrator.java focusing on SOLID principles"
Claude: [chama review_code via MCP] → Retorna feedback estruturado
```

### Via GitHub Copilot

1. Configure o servidor como extensão do Copilot
2. Use comandos como: `@mcp review OrderSagaOrchestrator.java`

---

## 📊 Exemplo de Resposta

```json
{
  "success": true,
  "content": "# Code Review Report\n\n## Issues Found\n- Method execute() is too long (120 lines)\n- Class has 15 methods, consider splitting\n\n## AI Feedback\nOverall, the code follows good practices...",
  "issues": [
    "Method execute() is too long (120 lines)",
    "Class has 15 methods, consider splitting"
  ],
  "metadata": {
    "file_path": "OrderSagaOrchestrator.java",
    "focus": "SOLID",
    "violations": [...]
  }
}
```

---

## 🎯 Benefícios

- ✅ **Demonstra Expertise em IA** - Alinhado com pós-graduação em Engenharia de IA
- ✅ **Diferencial Competitivo** - Poucos projetos demonstram MCP implementado
- ✅ **Valor Prático** - Code review automatizado e feedback consistente
- ✅ **Tecnologia Emergente** - MCP é padrão usado por GitHub Copilot e Claude

---

## 📚 Documentação

- **Plano de Implementação:** [docs/PLANO-IMPLEMENTACAO-MCP-CODE-REVIEW.md](../docs/PLANO-IMPLEMENTACAO-MCP-CODE-REVIEW.md)
- **MCP Specification:** https://modelcontextprotocol.io

---

## 👨‍💻 Autor

**Marcelo Hernandes da Silva**  
🌐 [Site Pessoal](https://marcelohsilva.com.br) | 📄 [Currículo](../docs/RESUME_JAVA_SENIOR_MARCELO_HERNANDES_DA_SILVA.pdf)

