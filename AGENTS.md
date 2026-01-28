# 🤖 Agents & AI Tools

This project includes a specialized AI agent implementation using the **Model Context Protocol (MCP)**. The agent is designed to assist with automated code reviews, pattern detection, and architectural analysis of the codebase.

## 🌟 Available Agents

The primary agent is hosted in the `mcp-code-review` module and provides the following capabilities:

### 1. Code Review Agent (`review_code`)
An intelligent agent that performs comprehensive code analysis combining static analysis with AI-powered feedback.
- **Capabilities:**
  - Static Code Analysis (using JavaParser/AST)
  - Code Quality Verification
  - Contextual AI Feedback (via OpenAI GPT models)
  - Improvement Suggestions based on SOLID principles

### 2. Pattern Analysis Agent (`analyze_patterns`)
An architectural agent focused on identifying and validating design patterns.
- **Capabilities:**
  - Detection of Design Patterns (Factory, Adapter, Strategy, etc.)
  - Architectural Compliance Checks
  - SOLID Principles Verification

## 🚀 How to Run the Agent Server

The agent operates as a standalone Spring Boot application that implements the MCP JSON-RPC 2.0 protocol.

### Prerequisites
- Java 21+
- Maven
- OpenAI API Key

### Quick Start
1. **Navigate to the module:**
   ```bash
   cd mcp-code-review
   ```

2. **Configure Environment:**
   ```bash
   export OPENAI_API_KEY=your-openai-key
   ```

3. **Start the Server:**
   ```bash
   mvn spring-boot:run
   ```

The server will start on `http://localhost:8081`.

## 🔌 Integration

This agent server is designed to integrate with:
- **Claude Desktop App:** Can directly invoke tools for interactive code review.
- **GitHub Copilot:** Can be configured as an extension to provide `@mcp` commands.
- **HTTP/REST Clients:** Can be invoked via standard HTTP requests for CI/CD integration.

## 🛠 Technologies

- **Core:** Java 21, Spring Boot 3.2+
- **Protocol:** Model Context Protocol (MCP), JSON-RPC 2.0
- **AI/ML:** OpenAI GPT-4, JavaParser (AST)

For more detailed technical documentation, please refer to the [MCP Code Review README](mcp-code-review/README.md).
