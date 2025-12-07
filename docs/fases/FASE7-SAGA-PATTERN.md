# Fase 7: Saga Pattern (Orquestração de Transações)

## 🎯 O Problema Atual

### Situação Atual (Fases 1-6)

Atualmente, temos **3 Use Cases separados** que são chamados manualmente:

1. **CreateOrderUseCase** → Cria pedido (status: PENDING)
2. **ProcessPaymentUseCase** → Processa pagamento (status: PAID ou PAYMENT_FAILED)
3. **AnalyzeRiskUseCase** → Analisa risco (riskLevel: LOW, HIGH, ou PENDING)

### ❌ Problemas desta Abordagem

1. **Chamadas Manuais**: Cliente precisa chamar cada Use Case separadamente
2. **Sem Orquestração**: Não há garantia de que os passos sejam executados na ordem correta
3. **Sem Compensação**: Se pagamento falhar, não há rollback automático
4. **Sem Rastreamento**: Não há histórico de execução da saga
5. **Inconsistência**: Pedido pode ficar em estado inconsistente se algum passo falhar

### Exemplo do Problema:

```java
// Cliente precisa fazer isso manualmente:
Order order = createOrderUseCase.execute(command);
Order paidOrder = processPaymentUseCase.execute(paymentCommand);
Order analyzedOrder = analyzeRiskUseCase.execute(riskCommand);

// E se o pagamento falhar? E se a análise falhar?
// Não há compensação automática!
```

---

## ✅ Solução: Saga Pattern

### O que é Saga Pattern?

**Saga Pattern** é um padrão para gerenciar transações distribuídas em microserviços ou sistemas com múltiplas operações que precisam ser executadas em sequência.

### Dois Tipos de Saga:

1. **Choreography (Orquestração Descentralizada)**: Cada serviço sabe o próximo passo
2. **Orchestration (Orquestração Centralizada)**: Um orquestrador coordena todos os passos ← **Vamos usar este!**

---

## 🏗️ O que Será Implementado na Fase 7

### 1. **OrderSagaOrchestrator**

Um orquestrador que coordena os 3 passos:

```java
public class OrderSagaOrchestrator {
    
    public OrderSagaResult execute(OrderSagaCommand command) {
        // Step 1: Criar pedido
        Order order = createOrderUseCase.execute(...);
        
        // Step 2: Processar pagamento
        Order paidOrder = processPaymentUseCase.execute(...);
        
        // Step 3: Analisar risco (se pagamento foi bem-sucedido)
        if (paidOrder.isPaid()) {
            Order analyzedOrder = analyzeRiskUseCase.execute(...);
            return OrderSagaResult.success(analyzedOrder);
        } else {
            // Compensação: cancelar pedido se pagamento falhou
            return OrderSagaResult.failed(paidOrder, "Payment failed");
        }
    }
}
```

### 2. **Saga Steps (Passos da Saga)**

Cada passo da saga será encapsulado:

- **Step 1: CreateOrderStep** → Cria pedido
- **Step 2: ProcessPaymentStep** → Processa pagamento
- **Step 3: AnalyzeRiskStep** → Analisa risco (apenas se Step 2 suceder)

### 3. **Compensação (Rollback)**

Se algum passo falhar, executar compensação:

- **Se pagamento falhar**: Cancelar pedido (status → CANCELED)
- **Se análise falhar**: Manter pedido como PAID (não é crítico)

### 4. **Estado da Saga**

Persistir estado da saga para rastreamento:

```java
public enum SagaStatus {
    STARTED,        // Saga iniciada
    ORDER_CREATED,  // Step 1 concluído
    PAYMENT_PROCESSED, // Step 2 concluído
    RISK_ANALYZED,  // Step 3 concluído
    COMPLETED,      // Todos os passos concluídos
    FAILED,         // Algum passo falhou
    COMPENSATED     // Compensação executada
}
```

### 5. **Saga Repository**

Persistir histórico da saga no banco:

```sql
CREATE TABLE saga_execution (
    id UUID PRIMARY KEY,
    order_id UUID,
    status VARCHAR(50),
    current_step VARCHAR(50),
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);
```

---

## 🎯 Por que é Importante?

### 1. **Consistência Eventual**

- Garante que todas as operações sejam executadas na ordem correta
- Se uma falhar, executa compensação
- Sistema nunca fica em estado inconsistente

### 2. **Orquestração Centralizada**

- Um único ponto de entrada para o fluxo completo
- Fácil adicionar novos passos
- Rastreamento centralizado

### 3. **Compensação Automática**

- Se pagamento falhar, pedido é cancelado automaticamente
- Não deixa dados órfãos no banco
- Garante integridade dos dados

### 4. **Observabilidade**

- Histórico completo de execução
- Rastreamento de falhas
- Métricas de sucesso/falha

---

## 🎤 Relevância para Apresentação

### Competências Demonstradas:

#### 1. **Padrões de Integração**
- Saga Pattern (padrão clássico de microserviços)
- Orquestração vs Choreography
- Compensação de transações

#### 2. **Transações Distribuídas**
- Consistência eventual
- ACID vs BASE
- Compensação vs Rollback

#### 3. **Arquitetura de Sistemas**
- Orquestração centralizada
- Separação de responsabilidades
- Rastreamento de estado

#### 4. **Resiliência**
- Tratamento de falhas
- Compensação automática
- Degradação graciosa

---

## 📊 Fluxo Completo da Saga

```
┌─────────────────────────────────────────────────────────┐
│              OrderSagaOrchestrator                       │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
        ┌───────────────────────────┐
        │  Step 1: Create Order     │
        │  Status: PENDING          │
        └───────────┬───────────────┘
                    │ ✅ Sucesso
                    ▼
        ┌───────────────────────────┐
        │  Step 2: Process Payment  │
        │  Status: PAID ou FAILED    │
        └───────────┬───────────────┘
                    │ ✅ Sucesso (PAID)
                    ▼
        ┌───────────────────────────┐
        │  Step 3: Analyze Risk     │
        │  RiskLevel: LOW/HIGH      │
        └───────────┬───────────────┘
                    │ ✅ Sucesso
                    ▼
            ┌───────────────┐
            │   COMPLETED   │
            └───────────────┘

        Se Step 2 falhar:
                    │ ❌ Falha
                    ▼
        ┌───────────────────────────┐
        │  Compensate: Cancel Order │
        │  Status: CANCELED         │
        └───────────────────────────┘
```

---

## 🔍 Exemplo de Código (Preview)

```java
@Service
public class OrderSagaOrchestrator {
    
    private final CreateOrderUseCase createOrderUseCase;
    private final ProcessPaymentUseCase processPaymentUseCase;
    private final AnalyzeRiskUseCase analyzeRiskUseCase;
    private final SagaExecutionRepository sagaRepository;
    
    @Transactional
    public OrderSagaResult execute(OrderSagaCommand command) {
        SagaExecution saga = SagaExecution.start(command);
        sagaRepository.save(saga);
        
        try {
            // Step 1: Criar pedido
            Order order = createOrderUseCase.execute(command.toCreateOrderCommand());
            saga.markStepCompleted("ORDER_CREATED", order.getId());
            
            // Step 2: Processar pagamento
            Order paidOrder = processPaymentUseCase.execute(
                command.toProcessPaymentCommand(order.getId())
            );
            saga.markStepCompleted("PAYMENT_PROCESSED", paidOrder.getId());
            
            // Step 3: Analisar risco (apenas se pagamento sucedeu)
            if (paidOrder.isPaid()) {
                Order analyzedOrder = analyzeRiskUseCase.execute(
                    command.toAnalyzeRiskCommand(paidOrder.getId())
                );
                saga.markStepCompleted("RISK_ANALYZED", analyzedOrder.getId());
                saga.complete();
                return OrderSagaResult.success(analyzedOrder);
            } else {
                // Compensação: cancelar pedido
                compensate(saga, paidOrder, "Payment failed");
                return OrderSagaResult.failed(paidOrder, "Payment failed");
            }
            
        } catch (Exception e) {
            // Compensação em caso de erro
            compensate(saga, null, e.getMessage());
            throw new SagaExecutionException("Saga failed", e);
        } finally {
            sagaRepository.save(saga);
        }
    }
    
    private void compensate(SagaExecution saga, Order order, String reason) {
        if (order != null && saga.isStepCompleted("ORDER_CREATED")) {
            // Cancelar pedido
            order.updateStatus(OrderStatus.CANCELED);
            orderRepository.save(order);
        }
        saga.fail(reason);
    }
}
```

---

## 🧪 O que Testar na Fase 7

### Testes Críticos:

1. **Saga Completa com Sucesso**
   - Todos os 3 passos executam com sucesso
   - Estado final: COMPLETED

2. **Saga com Falha no Pagamento**
   - Step 1: ✅ Sucesso
   - Step 2: ❌ Falha
   - Compensação: ✅ Pedido cancelado

3. **Saga com Falha na Análise**
   - Step 1: ✅ Sucesso
   - Step 2: ✅ Sucesso
   - Step 3: ❌ Falha
   - Resultado: Pedido fica PAID (análise não é crítica)

4. **Rastreamento de Estado**
   - Verificar que estado da saga é persistido
   - Histórico completo disponível

---

## 📈 Benefícios para Apresentação

### 1. **Conhecimento de Padrões**
- Saga Pattern é padrão clássico de microserviços
- Demonstra conhecimento de arquitetura distribuída

### 2. **Resolução de Problemas Complexos**
- Transações distribuídas são um problema real
- Solução elegante e testável

### 3. **Arquitetura Escalável**
- Fácil adicionar novos passos
- Orquestração centralizada facilita manutenção

### 4. **Observabilidade**
- Rastreamento completo
- Facilita debugging e monitoramento

---

## 🚀 Resumo

**Fase 7 implementa:**

1. ✅ **OrderSagaOrchestrator** - Coordena os 3 passos
2. ✅ **Saga Steps** - Encapsula cada passo
3. ✅ **Compensação** - Rollback automático em falhas
4. ✅ **Saga Repository** - Persiste estado e histórico
5. ✅ **Testes** - Validação completa do fluxo

**Por que é importante:**

- Demonstra conhecimento de padrões avançados
- Resolve problema real de transações distribuídas
- Mostra capacidade de arquitetar soluções complexas
- Facilita apresentação técnica em entrevistas

---

## 🔮 Melhorias Futuras (Roadmap)

> **📋 Nota:** Esta seção apresenta melhorias técnicas planejadas para evolução do Saga Pattern. A implementação atual já é robusta e funcional para produção.

### 🎯 Event-Driven Architecture Implementada

**IMPORTANTE:** O sistema já publica eventos em cada step da saga usando **Factory Pattern** para message brokers. Veja:
- **Eventos:** `backend/src/main/java/com/marcelo/orchestrator/domain/event/saga/`
- **Factory:** `backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/factory/EventPublisherFactory.java`
- **Adapters:** `backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/`
- **Integração:** `OrderSagaOrchestrator.java` (publicação de eventos após cada step)

### ✅ Pontos Fortes da Implementação Atual

1. **Orquestração Centralizada**: Implementação correta do padrão de orquestração
2. **Rastreamento Completo**: Cada passo é persistido com timestamps e duração
3. **Compensação Automática**: Cancela pedido se pagamento falhar
4. **Resilience4j nos Adapters**: Circuit Breaker e Retry nas integrações externas
5. **Estrutura de Dados**: Tabelas bem normalizadas com índices apropriados
6. **Idempotência**: Implementada com `idempotencyKey` único
7. **Event-Driven**: Eventos publicados em cada step (OrderCreated, PaymentProcessed, etc.)

### 🔄 Melhorias Planejadas

#### 1. **Persistência - Checkpoint Intermediário**

**Situação Atual:**
- Tudo está em uma única transação (`@Transactional`)
- Se a aplicação cair entre steps, não há como recuperar
- Não há checkpoint intermediário que permita retomar de onde parou

**Melhoria Planejada:**
- Salvar checkpoint após cada step bem-sucedido
- Implementar recuperação automática de sagas interrompidas
- Job scheduler para retomar sagas pendentes

**Benefício:**
- Resiliência contra falhas de aplicação
- Possibilidade de retomar sagas após restart

#### 2. **Consistência - Idempotência Avançada**

**Situação Atual:**
- Idempotência implementada com `idempotencyKey`
- Verificação antes de criar nova saga

**Melhoria Planejada:**
- Idempotência por step (não apenas por saga completa)
- Verificação de estado antes de executar cada step
- Prevenção de race conditions com locks otimistas

**Benefício:**
- Maior garantia de idempotência
- Prevenção de execuções duplicadas mesmo em cenários complexos

#### 3. **Resiliência - Recuperação Automática**

**Situação Atual:**
- Compensação automática em caso de falha
- Não há retry automático de sagas falhas

**Melhoria Planejada:**
- Job scheduler para retry automático de sagas falhas
- Estratégias de retry configuráveis (exponencial backoff)
- Dead Letter Queue para sagas que falharam múltiplas vezes

**Benefício:**
- Recuperação automática de falhas transitórias
- Menor intervenção manual necessária

#### 4. **Observabilidade - Métricas Avançadas**

**Situação Atual:**
- Rastreamento completo de cada execução
- Persistência de timestamps e duração

**Melhoria Planejada:**
- Métricas de taxa de sucesso por step
- Alertas para sagas que demoram muito
- Dashboard de monitoramento em tempo real
- Distributed Tracing (Jaeger/Zipkin)

**Benefício:**
- Melhor visibilidade de problemas
- Identificação proativa de gargalos

#### 5. **Escalabilidade - Processamento Assíncrono**

**Situação Atual:**
- Saga executada de forma síncrona na requisição HTTP

**Melhoria Planejada:**
- Processamento assíncrono de sagas
- Fila de processamento (Kafka, RabbitMQ)
- Workers dedicados para processar sagas

**Benefício:**
- Melhor escalabilidade
- Requisições HTTP mais rápidas
- Processamento em background

### 📊 Priorização das Melhorias

| Melhoria | Prioridade | Complexidade | Impacto |
|----------|-----------|--------------|---------|
| Checkpoint Intermediário | Alta | Média | Alto |
| Idempotência Avançada | Média | Baixa | Médio |
| Recuperação Automática | Alta | Média | Alto |
| Métricas Avançadas | Média | Baixa | Médio |
| Processamento Assíncrono | Baixa | Alta | Alto |

### 🎯 Conclusão

A implementação atual do Saga Pattern é **robusta e funcional para produção**, com:
- ✅ Orquestração completa
- ✅ Compensação automática
- ✅ Idempotência
- ✅ Rastreamento completo
- ✅ Event-Driven Architecture

As melhorias planejadas são **evoluções** que aumentam ainda mais a resiliência e escalabilidade, mas não são críticas para o funcionamento atual do sistema.

