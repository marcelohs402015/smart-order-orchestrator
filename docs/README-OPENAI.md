# Integração com OpenAI

Documentação da integração com OpenAI para análise de risco de pedidos.

## 📚 Documentação Oficial

- [OpenAI API Reference](https://platform.openai.com/docs/api-reference/chat)
- [Chat Completions Guide](https://platform.openai.com/docs/guides/text-generation)

## 🔑 Configuração

### Variáveis de Ambiente

Configure a chave de API do OpenAI:

```bash
export OPENAI_API_KEY=sua_chave_api_aqui
```

Ou no `application.properties`:

```properties
openai.api.key=sua_chave_api_aqui
```

### Configurações Disponíveis

```properties
# Base URL (padrão: https://api.openai.com/v1)
openai.api.base-url=${OPENAI_BASE_URL:https://api.openai.com/v1}

# Chave de API (obrigatória)
openai.api.key=${OPENAI_API_KEY:}

# Modelo a ser utilizado (padrão: gpt-3.5-turbo)
openai.api.model=${OPENAI_MODEL:gpt-3.5-turbo}

# Temperatura (0.0 = determinístico, padrão: 0.0)
openai.api.temperature=${OPENAI_TEMPERATURE:0.0}

# Máximo de tokens na resposta (padrão: 10)
openai.api.max-tokens=${OPENAI_MAX_TOKENS:10}
```

## 🏗️ Arquitetura da Integração

### Componentes

1. **OpenAIRiskAnalysisAdapter**: Implementa `RiskAnalysisPort`
2. **OpenAIConfig**: Configura `WebClient` para chamadas HTTP
3. **DTOs**: `OpenAIRequest`, `OpenAIResponse`

### Fluxo

```
Use Case (AnalyzeRiskUseCase)
    ↓
RiskAnalysisPort (interface)
    ↓
OpenAIRiskAnalysisAdapter (implementação)
    ↓
WebClient → OpenAI API (Chat Completions)
    ↓
RiskAnalysisResult (domínio)
```

## 🔒 Resiliência

A integração utiliza Resilience4j:

- **Circuit Breaker**: Protege contra falhas em cascata
- **Retry**: Tenta novamente em falhas transitórias
- **Fallback**: Retorna `PENDING` quando OpenAI indisponível

## 📝 Endpoint Utilizado

### Chat Completions

- **Endpoint**: `POST /v1/chat/completions`
- **Autenticação**: Bearer token
- **Request**: `OpenAIRequest` (com prompt estruturado)
- **Response**: `OpenAIResponse` (contém "LOW" ou "HIGH")

## 🎯 Estratégia de Prompt

O prompt é estruturado para garantir que a IA retorne apenas "LOW" ou "HIGH":

1. **Instruções claras**: Define o papel da IA como sistema de análise de risco
2. **Formato específico**: Solicita resposta apenas "LOW" ou "HIGH"
3. **Contexto completo**: Inclui todos os dados relevantes do pedido
4. **Critérios explícitos**: Define quando retornar LOW vs HIGH

## 🧪 Testes

Execute os testes unitários:

```bash
mvn test -Dtest=OpenAIRiskAnalysisAdapterTest
```

## ⚠️ Notas Importantes

1. **Custos**: Cada análise consome tokens. Use `gpt-3.5-turbo` para desenvolvimento (mais barato).
2. **Temperatura 0.0**: Garante respostas determinísticas e consistentes.
3. **Max Tokens 10**: Suficiente para retornar apenas "LOW" ou "HIGH".
4. **Fallback**: Sistema continua funcionando mesmo se OpenAI estiver offline.

## 💡 Exemplo de Uso

```java
// No Use Case
RiskAnalysisRequest request = new RiskAnalysisRequest(
    orderId,
    orderAmount,
    customerId,
    customerEmail,
    paymentMethod,
    additionalContext
);

RiskAnalysisResult result = riskAnalysisPort.analyzeRisk(request);
// result.riskLevel() = LOW, HIGH, ou PENDING (se falhou)
```

## 🔄 Fluxo Completo

1. Pedido é pago (status → PAID)
2. `AnalyzeRiskUseCase` é chamado
3. Adapter envia prompt para OpenAI
4. OpenAI retorna "LOW" ou "HIGH"
5. Adapter faz parse e retorna `RiskAnalysisResult`
6. Use Case atualiza `riskLevel` do pedido
7. Pedido é persistido com novo nível de risco

