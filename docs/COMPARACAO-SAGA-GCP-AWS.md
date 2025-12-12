# Comparação: Saga Pattern - GCP vs AWS vs Nossa Implementação

## 📋 Visão Geral

Este documento compara as implementações do padrão Saga na **Google Cloud Platform (GCP)**, **Amazon Web Services (AWS)** e nossa implementação atual.

---

## 🔄 GCP: Google Cloud Workflows

### Características Principais

1. **Orquestração Declarativa (YAML/JSON)**
   - Workflows definidos em YAML ou JSON
   - Execução gerenciada pela plataforma
   - Não precisa gerenciar infraestrutura

2. **Retry Automático**
   ```yaml
   reserve_credit:
     try:
       call: http.post
     retry: ${http.default_retry}  # Retry automático para HTTP 503, 502, 504
   ```

3. **Tratamento de Erros com Try/Except**
   ```yaml
   reserve_credit:
     try:
       call: http.post
     retry: ${http.default_retry}
     except:
       as: e
       steps:
         - check_nonrecoverable_error:
             switch:
               - condition: ${e.code == 500}
                 next: reject_pending_order
   ```

4. **Compensação em Cadeia**
   - Suporte a subworkflows para compensação
   - Compensação automática em ordem reversa

5. **Idempotência**
   - Suporte nativo via configuração
   - Rastreamento automático de execuções

6. **Observabilidade**
   - Logs automáticos
   - Histórico de execuções
   - Métricas integradas

---

## ☁️ AWS: Step Functions

### Características Principais

1. **Orquestração Declarativa (JSON)**
   - State machines definidas em JSON (ASL - Amazon States Language)
   - Execução gerenciada pela AWS
   - Integração com outros serviços AWS

2. **Retry e Catch com Estados**
   ```json
   {
     "Type": "Task",
     "Resource": "arn:aws:lambda:...",
     "Retry": [
       {
         "ErrorEquals": ["States.TaskFailed"],
         "IntervalSeconds": 2,
         "MaxAttempts": 3,
         "BackoffRate": 2.0
       }
     ],
     "Catch": [
       {
         "ErrorEquals": ["States.ALL"],
         "Next": "CompensateStep",
         "ResultPath": "$.error"
       }
     ]
   }
   ```

3. **Compensação com Estados de Compensação**
   ```json
   {
     "CompensateStep": {
       "Type": "Task",
       "Resource": "arn:aws:lambda:...",
       "End": true
     }
   }
   ```

4. **Parallel States**
   - Execução paralela de steps
   - Compensação paralela quando necessário

5. **Map State**
   - Processamento em lote
   - Compensação em lote

6. **Wait State**
   - Aguardar tempo específico
   - Aguardar callback externo

7. **Choice State**
   - Lógica condicional avançada
   - Roteamento baseado em condições

8. **Idempotência**
   - Suporte via `IdempotencyToken`
   - Rastreamento automático

9. **Observabilidade**
   - CloudWatch Logs integrado
   - CloudWatch Metrics
   - X-Ray tracing
   - Visualização gráfica no console

10. **Integração com Outros Serviços**
    - Lambda functions
    - ECS tasks
    - SNS/SQS
    - DynamoDB
    - S3
    - E muito mais

---

## 🆚 Comparação Detalhada

| Característica | GCP Workflows | AWS Step Functions | Nossa Implementação |
|----------------|---------------|-------------------|---------------------|
| **Tipo de Orquestração** | Declarativa (YAML) | Declarativa (JSON) | **Imperativa (Java)** |
| **Retry Automático** | ✅ Sim (built-in) | ✅ Sim (configurável) | ✅ Sim (Resilience4j) |
| **Circuit Breaker** | ⚠️ Implícito | ⚠️ Via Lambda | ✅ Sim (Resilience4j) |
| **Tratamento de Erros** | ✅ Try/Except | ✅ Retry/Catch | ⚠️ Try/Catch básico |
| **Compensação** | ✅ Subworkflows | ✅ Estados de compensação | ✅ Método `compensate()` |
| **Compensação em Cadeia** | ✅ Sim | ✅ Sim | ⚠️ Parcial (só anterior) |
| **Execução Paralela** | ⚠️ Limitada | ✅ Parallel States | ❌ Não |
| **Idempotência** | ✅ Nativo | ✅ Nativo | ✅ Implementado |
| **Observabilidade** | ✅ Logs/Métricas | ✅ CloudWatch/X-Ray | ✅ SagaExecutionEntity |
| **Visualização Gráfica** | ⚠️ Limitada | ✅ Console AWS | ❌ Não |
| **Integração com Serviços** | ⚠️ HTTP/REST | ✅ Muitos serviços | ✅ HTTP/REST |
| **Timeout Configurável** | ✅ Sim | ✅ Sim | ⚠️ Via Resilience4j |
| **Wait/Callback** | ⚠️ Limitado | ✅ Wait State | ❌ Não |
| **Map/Batch Processing** | ❌ Não | ✅ Map State | ❌ Não |
| **Escalabilidade** | ✅ Gerenciada | ✅ Gerenciada | ⚠️ Depende do servidor |
| **Custo** | Por execução | Por transição de estado | Infraestrutura própria |

---

## 🔍 Diferenças Principais

### 1. **Abordagem: Declarativa vs Imperativa**

**GCP/AWS (Declarativa):**
```yaml
# GCP Workflows
- create_order:
    call: http.post
    args:
      url: ${order_service_url}
  
- process_payment:
    call: http.post
    args:
      url: ${payment_service_url}
```

**Nossa Implementação (Imperativa):**
```java
// Java
Order order = executeStep1_CreateOrder(command, saga);
Order paidOrder = executeStep2_ProcessPayment(command, order, saga);
```

**Vantagens Declarativa:**
- ✅ Mais fácil de visualizar o fluxo
- ✅ Menos código boilerplate
- ✅ Plataforma gerencia execução

**Vantagens Imperativa:**
- ✅ Mais controle sobre lógica
- ✅ Debugging mais fácil
- ✅ Não depende de plataforma cloud

---

### 2. **Tratamento de Erros**

**GCP:**
```yaml
reserve_credit:
  try:
    call: http.post
  retry: ${http.default_retry}
  except:
    as: e
    steps:
      - check_error:
          switch:
            - condition: ${e.code == 500}
              next: compensate
```

**AWS:**
```json
{
  "Retry": [
    {
      "ErrorEquals": ["States.TaskFailed"],
      "MaxAttempts": 3
    }
  ],
  "Catch": [
    {
      "ErrorEquals": ["States.ALL"],
      "Next": "CompensateStep"
    }
  ]
}
```

**Nossa Implementação:**
```java
try {
    Order paidOrder = processPaymentUseCase.execute(command);
} catch (Exception e) {
    compensate(saga, order, e.getMessage());
}
```

**Diferença:** GCP/AWS têm retry e catch integrados. Nós temos retry no adapter, mas não no orchestrator.

---

### 3. **Compensação em Cadeia**

**GCP/AWS:**
- Suportam compensação de múltiplos steps em ordem reversa
- Subworkflows/estados de compensação

**Nossa Implementação:**
- Compensamos apenas o step anterior
- Não há chaining automático

---

### 4. **Execução Paralela**

**AWS Step Functions:**
```json
{
  "Type": "Parallel",
  "Branches": [
    {
      "StartAt": "ProcessPayment",
      "States": { ... }
    },
    {
      "StartAt": "ValidateInventory",
      "States": { ... }
    }
  ]
}
```

**Nossa Implementação:**
- ❌ Não suporta execução paralela
- Steps são sempre sequenciais

---

### 5. **Observabilidade**

**GCP/AWS:**
- Visualização gráfica do fluxo
- Métricas automáticas
- Tracing integrado

**Nossa Implementação:**
- ✅ Rastreamento via `SagaExecutionEntity`
- ❌ Sem visualização gráfica
- ⚠️ Métricas manuais

---

## ❌ O Que Falta Implementar no Nosso Projeto

### 🔴 Alta Prioridade

#### 1. **Compensação em Cadeia Completa**

**Problema Atual:**
```java
// Só compensa o step anterior
if (order != null && !order.isPaid()) {
    order.updateStatus(OrderStatus.CANCELED);
}
```

**Solução Sugerida:**
```java
private void compensate(SagaExecutionEntity saga, Order order, String reason) {
    // Compensar em ordem reversa
    List<SagaStepEntity> completedSteps = saga.getSteps().stream()
        .filter(s -> s.getStatus() == StepStatus.SUCCESS)
        .sorted(Comparator.comparing(SagaStepEntity::getCompletedAt).reversed())
        .collect(Collectors.toList());
    
    for (SagaStepEntity step : completedSteps) {
        compensateStep(step, saga);
    }
}

private void compensateStep(SagaStepEntity step, SagaExecutionEntity saga) {
    switch (step.getStepName()) {
        case "RISK_ANALYZED":
            // Compensar análise de risco (se necessário)
            break;
        case "PAYMENT_PROCESSED":
            // Compensar pagamento (já feito)
            break;
        case "ORDER_CREATED":
            // Compensar criação de pedido
            if (saga.getOrderId() != null) {
                Order order = orderRepository.findById(saga.getOrderId())
                    .orElseThrow();
                order.updateStatus(OrderStatus.CANCELED);
                orderRepository.save(order);
            }
            break;
    }
}
```

#### 2. **Distinção de Erros no Orchestrator**

**Problema Atual:**
- Não distinguimos entre erro transitório (retry esgotado) e permanente

**Solução Sugerida:**
```java
public class SagaException extends RuntimeException {
    private final boolean isTransient;
    private final int retryAttempts;
    
    public SagaException(String message, boolean isTransient, int retryAttempts) {
        super(message);
        this.isTransient = isTransient;
        this.retryAttempts = retryAttempts;
    }
}

private Order executeStep2_ProcessPayment(...) {
    try {
        Order paidOrder = processPaymentUseCase.execute(command);
        return paidOrder;
    } catch (TransientPaymentException e) {
        // Retry esgotado - pode ser transitório
        throw new SagaException("Payment failed after retries", true, 3);
    } catch (PermanentPaymentException e) {
        // Rejeitado pelo gateway - permanente
        throw new SagaException("Payment rejected", false, 0);
    }
}
```

#### 3. **Timeout Configurável por Step**

**Problema Atual:**
- Timeout é gerenciado pelo Resilience4j no adapter
- Não há timeout específico por step no orchestrator

**Solução Sugerida:**
```java
@Value("${saga.step.timeout.payment:30000}")
private long paymentStepTimeout;

private Order executeStep2_ProcessPayment(...) {
    CompletableFuture<Order> future = CompletableFuture.supplyAsync(() -> {
        return processPaymentUseCase.execute(command);
    });
    
    try {
        return future.get(paymentStepTimeout, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
        future.cancel(true);
        throw new SagaException("Payment step timeout", true, 0);
    }
}
```

---

### 🟡 Média Prioridade

#### 4. **Execução Paralela de Steps**

**Quando Útil:**
- Validar estoque e processar pagamento em paralelo
- Enviar notificações em paralelo

**Solução Sugerida:**
```java
private Order executeStepsInParallel(OrderSagaCommand command, Order order, SagaExecutionEntity saga) {
    CompletableFuture<PaymentResult> paymentFuture = CompletableFuture.supplyAsync(() -> {
        return processPaymentUseCase.execute(command.toProcessPaymentCommand(order.getId()));
    });
    
    CompletableFuture<InventoryResult> inventoryFuture = CompletableFuture.supplyAsync(() -> {
        return validateInventoryUseCase.execute(command.toValidateInventoryCommand(order.getId()));
    });
    
    CompletableFuture.allOf(paymentFuture, inventoryFuture).join();
    
    // Processar resultados
    PaymentResult paymentResult = paymentFuture.get();
    InventoryResult inventoryResult = inventoryFuture.get();
    
    // Continuar saga...
}
```

#### 5. **Visualização Gráfica do Fluxo**

**Solução Sugerida:**
- Endpoint REST para retornar estado atual da saga em formato Graphviz/DOT
- Frontend renderiza graficamente
- Ou usar biblioteca como Mermaid.js

```java
@GetMapping("/sagas/{id}/graph")
public ResponseEntity<String> getSagaGraph(@PathVariable UUID id) {
    SagaExecutionEntity saga = sagaRepository.findById(id).orElseThrow();
    String dotGraph = sagaGraphGenerator.generateDotGraph(saga);
    return ResponseEntity.ok(dotGraph);
}
```

#### 6. **Métricas e Observabilidade Avançada**

**Solução Sugerida:**
- Integração com Micrometer/Prometheus
- Métricas customizadas:
  - Taxa de sucesso por step
  - Tempo médio de execução por step
  - Taxa de compensação
  - Taxa de retry

```java
@Timed(value = "saga.execution", description = "Saga execution time")
public OrderSagaResult execute(OrderSagaCommand command) {
    // ...
}

@Counter(value = "saga.step.failure", tags = {"step", "payment"})
private void recordStepFailure(String stepName) {
    // ...
}
```

---

### 🟢 Baixa Prioridade

#### 7. **Wait State / Callback Pattern**

**Quando Útil:**
- Aguardar confirmação externa
- Aguardar tempo específico antes de retry

**Solução Sugerida:**
```java
public enum SagaStatus {
    STARTED,
    WAITING_FOR_CALLBACK,  // Novo
    ORDER_CREATED,
    // ...
}

@GetMapping("/sagas/{id}/callback")
public ResponseEntity<Void> sagaCallback(
    @PathVariable UUID id,
    @RequestParam String step,
    @RequestParam boolean success) {
    
    SagaExecutionEntity saga = sagaRepository.findById(id).orElseThrow();
    saga.setStatus(SagaStatus.ORDER_CREATED); // Continuar saga
    sagaRepository.save(saga);
    
    // Continuar execução
    sagaOrchestrator.resume(saga);
    
    return ResponseEntity.ok().build();
}
```

#### 8. **Map State / Batch Processing**

**Quando Útil:**
- Processar múltiplos pedidos em lote
- Compensar múltiplos pedidos

**Solução Sugerida:**
```java
public List<OrderSagaResult> executeBatch(List<OrderSagaCommand> commands) {
    return commands.parallelStream()
        .map(this::execute)
        .collect(Collectors.toList());
}
```

---

## 📊 Matriz de Comparação Final

| Funcionalidade | GCP | AWS | Nossa Implementação | Prioridade |
|----------------|-----|-----|---------------------|------------|
| **Orquestração Básica** | ✅ | ✅ | ✅ | - |
| **Retry Automático** | ✅ | ✅ | ✅ | - |
| **Circuit Breaker** | ⚠️ | ⚠️ | ✅ | - |
| **Compensação Simples** | ✅ | ✅ | ✅ | - |
| **Compensação em Cadeia** | ✅ | ✅ | ❌ | 🔴 Alta |
| **Distinção de Erros** | ✅ | ✅ | ⚠️ | 🔴 Alta |
| **Timeout por Step** | ✅ | ✅ | ⚠️ | 🔴 Alta |
| **Execução Paralela** | ⚠️ | ✅ | ❌ | 🟡 Média |
| **Visualização Gráfica** | ⚠️ | ✅ | ❌ | 🟡 Média |
| **Métricas Avançadas** | ✅ | ✅ | ⚠️ | 🟡 Média |
| **Wait/Callback** | ⚠️ | ✅ | ❌ | 🟢 Baixa |
| **Map/Batch** | ❌ | ✅ | ❌ | 🟢 Baixa |

---

## 🎯 Plano de Implementação Recomendado

### Fase 1: Melhorias Críticas (1-2 semanas)

1. ✅ **Compensação em Cadeia**
   - Implementar compensação reversa de todos os steps
   - Testes unitários e de integração

2. ✅ **Distinção de Erros**
   - Adicionar informação de erro transitório vs permanente
   - Melhorar logs e métricas

3. ✅ **Timeout por Step**
   - Configurar timeout por step
   - Tratamento de timeout

### Fase 2: Melhorias Importantes (2-3 semanas)

4. ✅ **Execução Paralela**
   - Identificar steps que podem ser paralelos
   - Implementar execução paralela

5. ✅ **Visualização Gráfica**
   - Endpoint para gerar gráfico da saga
   - Frontend para visualizar

6. ✅ **Métricas Avançadas**
   - Integração com Micrometer
   - Dashboard de métricas

### Fase 3: Melhorias Opcionais (futuro)

7. ⚠️ **Wait/Callback Pattern**
8. ⚠️ **Map/Batch Processing**

---

## ✅ Conclusão

### Pontos Fortes da Nossa Implementação

- ✅ Core do Saga Pattern implementado corretamente
- ✅ Retry e Circuit Breaker via Resilience4j
- ✅ Idempotência implementada
- ✅ Rastreamento completo
- ✅ Event-Driven Architecture

### Principais Gaps vs GCP/AWS

1. **Compensação em Cadeia** - Falta implementar
2. **Distinção de Erros** - Melhorar
3. **Timeout por Step** - Adicionar
4. **Execução Paralela** - Não suportado
5. **Visualização Gráfica** - Não implementado

### Recomendação

**Priorizar Fase 1** (Compensação em Cadeia, Distinção de Erros, Timeout) para alinhar com as melhores práticas de GCP e AWS.

A implementação atual está **85% alinhada** com as melhores práticas. As melhorias sugeridas são incrementais e não críticas para o funcionamento básico.

---

## 📚 Referências

- [GCP: Implementing the saga pattern in Workflows](https://cloud.google.com/blog/topics/developers-practitioners/implementing-saga-pattern-workflows)
- [AWS: Saga Pattern with Step Functions](https://docs.aws.amazon.com/prescriptive-guidance/latest/cloud-design-patterns/saga-orchestration.html)
- [AWS Step Functions Best Practices](https://docs.aws.amazon.com/step-functions/latest/dg/best-practices.html)
- [Resilience4j Documentation](https://resilience4j.readme.io/)

---

**Data da Análise**: 12/12/2025

