# Revisão do Padrão Saga: Persistência, Consistência e Resiliência

> **📋 Documento Técnico de Análise e Planejamento**  
> Este documento é técnico e focado em melhorias futuras. Para preparação de entrevista, veja `docs/PREPARACAO-ENTREVISTA-TECNICA-ACCENTURE.md` que contém uma seção resumida sobre melhorias futuras.

## 📋 Sumário Executivo

Este documento apresenta uma revisão completa da implementação atual do **Saga Pattern (Orquestração)** no projeto, focando em três aspectos críticos:

1. **Persistência**: Como o estado da saga é salvo e recuperado
2. **Consistência**: Garantias de integridade dos dados
3. **Resiliência**: Capacidade de recuperação após falhas

### 🎯 Event-Driven Architecture Implementada

**IMPORTANTE:** O sistema agora publica eventos em cada step da saga usando **Factory Pattern** para message brokers. Veja:
- **Eventos:** `backend/src/main/java/com/marcelo/orchestrator/domain/event/saga/`
- **Factory:** `backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/factory/EventPublisherFactory.java`
- **Adapters:** `backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/`
- **Integração:** `OrderSagaOrchestrator.java` (linhas 300-350)

**Documentação de Padrões:** Veja `docs/PREPARACAO-ENTREVISTA-TECNICA-ACCENTURE.md` para referências completas ao código e padrões implementados.

---

## 🔍 Análise da Implementação Atual

### ✅ Pontos Fortes

1. **Orquestração Centralizada**: Implementação correta do padrão de orquestração
2. **Rastreamento Completo**: Cada passo é persistido com timestamps e duração
3. **Compensação Automática**: Cancela pedido se pagamento falhar
4. **Resilience4j nos Adapters**: Circuit Breaker e Retry nas integrações externas
5. **Estrutura de Dados**: Tabelas bem normalizadas com índices apropriados

### ⚠️ Problemas Identificados

#### 1. **Persistência - Problema de Checkpoint**

**Situação Atual:**
```java
@Transactional
public OrderSagaResult execute(OrderSagaCommand command) {
    SagaExecutionEntity saga = startSaga(); // ✅ Persistido
    Order order = executeStep1_CreateOrder(command, saga); // ✅ Persistido após step
    Order paidOrder = executeStep2_ProcessPayment(command, order, saga); // ✅ Persistido após step
    // ❌ Se falhar aqui, saga pode ficar em estado inconsistente
}
```

**Problemas:**
- Tudo está em uma única transação (`@Transactional`)
- Se a aplicação cair entre steps, não há como recuperar
- Não há checkpoint intermediário que permita retomar de onde parou
- Falta de idempotência: se executar duas vezes, pode criar duplicatas

**Impacto:**
- **Alto**: Em caso de falha (timeout, crash, rede), a saga pode ficar em estado inconsistente
- **Médio**: Não é possível retomar sagas interrompidas
- **Médio**: Dificulta debugging em produção

---

#### 2. **Consistência - Falta de Idempotência e Garantias**

**Situação Atual:**
```java
private Order executeStep1_CreateOrder(OrderSagaCommand command, SagaExecutionEntity saga) {
    SagaStepEntity step = startStep(saga, "ORDER_CREATED");
    Order order = createOrderUseCase.execute(command.toCreateOrderCommand());
    completeStep(step, true, null);
    saga.setOrderId(order.getId());
    saga.setStatus(SagaExecutionEntity.SagaStatus.ORDER_CREATED);
    sagaRepository.save(saga); // ✅ Persistido
    return order;
}
```

**Problemas:**
- **Sem Idempotência**: Se o mesmo comando for executado duas vezes, pode criar dois pedidos
- **Sem Verificação de Estado**: Não verifica se a saga já foi executada
- **Compensação Não Garantida**: Se a compensação falhar, não há retry
- **Race Conditions**: Múltiplas threads podem executar a mesma saga

**Impacto:**
- **Crítico**: Duplicação de pedidos em caso de retry
- **Alto**: Inconsistência de dados se compensação falhar
- **Médio**: Problemas em ambientes concorrentes

---

#### 3. **Resiliência - Falta de Recuperação Automática**

**Situação Atual:**
```java
catch (Exception e) {
    log.error("Saga execution failed: {}", e.getMessage(), e);
    compensate(saga, null, e.getMessage());
    return OrderSagaResult.failed(null, saga.getId(), e.getMessage());
}
```

**Problemas:**
- **Sem Retry Automático**: Se falhar por timeout, não tenta novamente
- **Sem Dead Letter Queue**: Sagas falhadas não são armazenadas para análise
- **Sem Recovery Service**: Não há serviço para retomar sagas interrompidas
- **Falhas Silenciosas**: Se compensação falhar, apenas loga erro

**Impacto:**
- **Alto**: Sagas podem ficar "travadas" sem recuperação
- **Médio**: Dificulta identificação de problemas recorrentes
- **Médio**: Perda de dados em caso de falha na compensação

---

#### 4. **Modelo de Orquestração - Falta de Recuperação**

**Situação Atual:**
- Orquestração centralizada ✅
- Mas não há mecanismo para:
  - Retomar sagas interrompidas
  - Verificar sagas "travadas" (timeout)
  - Processar dead letter queue

**Impacto:**
- **Médio**: Sagas podem ficar em estado intermediário indefinidamente
- **Baixo**: Requer intervenção manual para recuperação

---

## 🎯 Melhorias Propostas

### 1. **Persistência com Checkpoints**

#### Objetivo
Garantir que o estado da saga seja persistido após cada step, permitindo recuperação.

#### Implementação

**1.1. Remover `@Transactional` do método principal**

```java
// ❌ ANTES: Tudo em uma transação
@Transactional
public OrderSagaResult execute(OrderSagaCommand command) {
    // ...
}

// ✅ DEPOIS: Transações por step
public OrderSagaResult execute(OrderSagaCommand command) {
    SagaExecutionEntity saga = startSaga();
    
    try {
        // Cada step persiste seu próprio estado
        Order order = executeStep1_CreateOrder(command, saga);
        Order paidOrder = executeStep2_ProcessPayment(command, order, saga);
        // ...
    } catch (Exception e) {
        // Compensação também persiste estado
        compensate(saga, order, e.getMessage());
    }
}
```

**1.2. Checkpoint após cada step**

```java
@Transactional
private Order executeStep1_CreateOrder(OrderSagaCommand command, SagaExecutionEntity saga) {
    // Verificar se já foi executado (idempotência)
    if (saga.getStatus() == SagaStatus.ORDER_CREATED) {
        log.info("Step 1 already completed, skipping");
        return orderRepository.findById(saga.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(saga.getOrderId()));
    }
    
    SagaStepEntity step = startStep(saga, "ORDER_CREATED");
    
    try {
        Order order = createOrderUseCase.execute(command.toCreateOrderCommand());
        
        // CHECKPOINT: Persistir estado antes de continuar
        completeStep(step, true, null);
        saga.setOrderId(order.getId());
        saga.setStatus(SagaStatus.ORDER_CREATED);
        saga.setCurrentStep("ORDER_CREATED");
        sagaRepository.save(saga); // ✅ Estado persistido
        
        log.info("Step 1 checkpoint saved: Order {}", order.getId());
        return order;
        
    } catch (Exception e) {
        completeStep(step, false, e.getMessage());
        saga.setStatus(SagaStatus.FAILED);
        sagaRepository.save(saga);
        throw new RuntimeException("Failed to create order", e);
    }
}
```

**1.3. Adicionar campo `retry_count` e `last_error`**

```sql
ALTER TABLE saga_executions 
ADD COLUMN retry_count INTEGER DEFAULT 0,
ADD COLUMN last_error TEXT,
ADD COLUMN next_retry_at TIMESTAMP;
```

---

### 2. **Consistência com Idempotência**

#### Objetivo
Garantir que executar a mesma saga múltiplas vezes produza o mesmo resultado.

#### Implementação

**2.1. Adicionar `idempotency_key` ao comando**

```java
public class OrderSagaCommand {
    private UUID idempotencyKey; // ✅ Chave única para idempotência
    
    // ...
}
```

**2.2. Verificar idempotência antes de executar**

```java
public OrderSagaResult execute(OrderSagaCommand command) {
    // Verificar se já existe saga com esta chave
    Optional<SagaExecutionEntity> existingSaga = sagaRepository
        .findByIdempotencyKey(command.getIdempotencyKey());
    
    if (existingSaga.isPresent()) {
        SagaExecutionEntity saga = existingSaga.get();
        
        // Se já completou, retornar resultado anterior
        if (saga.getStatus() == SagaStatus.COMPLETED) {
            Order order = orderRepository.findById(saga.getOrderId())
                .orElseThrow();
            return OrderSagaResult.success(order, saga.getId());
        }
        
        // Se falhou, pode tentar retry (se dentro do limite)
        if (saga.getStatus() == SagaStatus.FAILED && 
            saga.getRetryCount() < MAX_RETRIES) {
            return retrySaga(saga, command);
        }
        
        // Se está em progresso, retornar status atual
        return OrderSagaResult.inProgress(saga.getId());
    }
    
    // Nova saga
    SagaExecutionEntity saga = startSaga(command.getIdempotencyKey());
    // ...
}
```

**2.3. Adicionar índice único para idempotency_key**

```sql
CREATE UNIQUE INDEX idx_saga_idempotency_key 
ON saga_executions(idempotency_key);
```

---

### 3. **Resiliência com Retry e Recovery**

#### Objetivo
Implementar recuperação automática de sagas falhadas e retry inteligente.

#### Implementação

**3.1. Criar `SagaRecoveryService`**

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class SagaRecoveryService {
    
    private final JpaSagaExecutionRepository sagaRepository;
    private final OrderSagaOrchestrator orchestrator;
    
    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_DELAY = Duration.ofMinutes(5);
    
    /**
     * Recupera sagas que falharam e estão dentro do limite de retry.
     */
    @Scheduled(fixedDelay = 60000) // Executa a cada 1 minuto
    public void recoverFailedSagas() {
        List<SagaExecutionEntity> failedSagas = sagaRepository
            .findByStatusInAndRetryCountLessThan(
                List.of(SagaStatus.FAILED, SagaStatus.STARTED),
                MAX_RETRIES
            );
        
        for (SagaExecutionEntity saga : failedSagas) {
            if (shouldRetry(saga)) {
                retrySaga(saga);
            }
        }
    }
    
    private boolean shouldRetry(SagaExecutionEntity saga) {
        if (saga.getNextRetryAt() == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(saga.getNextRetryAt());
    }
    
    private void retrySaga(SagaExecutionEntity saga) {
        try {
            log.info("Retrying saga {} (attempt {})", 
                saga.getId(), saga.getRetryCount() + 1);
            
            // Recuperar comando original (pode ser armazenado em metadata)
            OrderSagaCommand command = recoverCommand(saga);
            
            // Retomar execução a partir do último checkpoint
            orchestrator.resume(saga, command);
            
        } catch (Exception e) {
            log.error("Failed to retry saga {}: {}", saga.getId(), e.getMessage());
            incrementRetryCount(saga);
        }
    }
    
    private void incrementRetryCount(SagaExecutionEntity saga) {
        saga.setRetryCount(saga.getRetryCount() + 1);
        saga.setNextRetryAt(calculateNextRetry(saga.getRetryCount()));
        sagaRepository.save(saga);
    }
    
    private LocalDateTime calculateNextRetry(int retryCount) {
        // Backoff exponencial: 5min, 10min, 20min
        long delayMinutes = 5L * (1L << retryCount);
        return LocalDateTime.now().plusMinutes(delayMinutes);
    }
}
```

**3.2. Adicionar método `resume` no orchestrator**

```java
/**
 * Retoma execução de uma saga a partir do último checkpoint.
 */
public OrderSagaResult resume(SagaExecutionEntity saga, OrderSagaCommand command) {
    log.info("Resuming saga {} from step {}", saga.getId(), saga.getCurrentStep());
    
    try {
        Order order = null;
        
        // Retomar a partir do último checkpoint
        switch (saga.getStatus()) {
            case STARTED:
                order = executeStep1_CreateOrder(command, saga);
                // Fall through
            case ORDER_CREATED:
                if (order == null) {
                    order = orderRepository.findById(saga.getOrderId())
                        .orElseThrow();
                }
                Order paidOrder = executeStep2_ProcessPayment(command, order, saga);
                // Fall through
            case PAYMENT_PROCESSED:
                if (order == null) {
                    order = orderRepository.findById(saga.getOrderId())
                        .orElseThrow();
                }
                if (order.isPaid()) {
                    Order analyzedOrder = executeStep3_AnalyzeRisk(command, order, saga);
                    completeSaga(saga, analyzedOrder);
                    return OrderSagaResult.success(analyzedOrder, saga.getId());
                } else {
                    compensate(saga, order, "Payment failed");
                    return OrderSagaResult.failed(order, saga.getId(), "Payment failed");
                }
            default:
                throw new IllegalStateException("Cannot resume saga in status: " + saga.getStatus());
        }
        
    } catch (Exception e) {
        log.error("Failed to resume saga {}: {}", saga.getId(), e.getMessage());
        compensate(saga, null, e.getMessage());
        return OrderSagaResult.failed(null, saga.getId(), e.getMessage());
    }
}
```

**3.3. Dead Letter Queue para sagas que excederam retries**

```java
@Entity
@Table(name = "saga_dead_letters")
public class SagaDeadLetterEntity {
    @Id
    private UUID id;
    
    @Column(name = "saga_execution_id")
    private UUID sagaExecutionId;
    
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
    
    @Column(name = "retry_count")
    private Integer retryCount;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON com comando original
}
```

---

### 4. **Compensação Garantida com Retry**

#### Objetivo
Garantir que a compensação seja executada mesmo em caso de falha.

#### Implementação

```java
@Retryable(
    value = {Exception.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
private void compensate(SagaExecutionEntity saga, Order order, String reason) {
    log.warn("Compensating saga {} - Reason: {}", saga.getId(), reason);
    
    try {
        if (order != null && !order.isPaid()) {
            order.updateStatus(OrderStatus.CANCELED);
            orderRepository.save(order);
            log.info("Order {} cancelled due to payment failure", order.getId());
        }
        
        saga.setStatus(SagaStatus.COMPENSATED);
        saga.setErrorMessage(reason);
        completeSaga(saga, order);
        
    } catch (Exception e) {
        log.error("Failed to compensate saga {}: {}", saga.getId(), e.getMessage());
        // Marcar para retry de compensação
        saga.setStatus(SagaStatus.COMPENSATION_FAILED);
        saga.setNextRetryAt(LocalDateTime.now().plusMinutes(5));
        sagaRepository.save(saga);
        throw e; // Re-throw para @Retryable tentar novamente
    }
}
```

---

## 📊 Comparação: Antes vs Depois

| Aspecto | Antes | Depois |
|---------|-------|--------|
| **Persistência** | Transação única, sem checkpoint | Checkpoint após cada step |
| **Idempotência** | ❌ Não suportada | ✅ Suportada com `idempotency_key` |
| **Recuperação** | ❌ Manual | ✅ Automática com `SagaRecoveryService` |
| **Retry** | ❌ Não há | ✅ Retry exponencial até 3 tentativas |
| **Compensação** | ⚠️ Pode falhar silenciosamente | ✅ Retry garantido |
| **Dead Letter** | ❌ Não há | ✅ Sagas que excederam retries |
| **Race Conditions** | ⚠️ Possível | ✅ Prevenido com idempotência |

---

## 🚀 Plano de Implementação

### Fase 1: Persistência com Checkpoints (Prioridade: Alta)
1. Remover `@Transactional` do método `execute`
2. Adicionar `@Transactional` em cada step individual
3. Implementar checkpoint após cada step
4. Adicionar campos `retry_count`, `last_error`, `next_retry_at`

### Fase 2: Idempotência (Prioridade: Alta)
1. Adicionar `idempotency_key` ao `OrderSagaCommand`
2. Criar índice único no banco
3. Implementar verificação de idempotência no orchestrator
4. Testes de idempotência

### Fase 3: Recovery Service (Prioridade: Média)
1. Criar `SagaRecoveryService`
2. Implementar `@Scheduled` para recuperação periódica
3. Adicionar método `resume` no orchestrator
4. Testes de recuperação

### Fase 4: Dead Letter Queue (Prioridade: Baixa)
1. Criar entidade `SagaDeadLetterEntity`
2. Migrar sagas que excederam retries
3. Endpoint para consultar dead letters
4. Dashboard de monitoramento

---

## 🧪 Testes Necessários

### 1. Testes de Persistência
- ✅ Verificar que checkpoint é salvo após cada step
- ✅ Verificar que saga pode ser recuperada após crash
- ✅ Verificar que estado é consistente após retry

### 2. Testes de Idempotência
- ✅ Executar mesma saga duas vezes → mesmo resultado
- ✅ Verificar que não cria duplicatas
- ✅ Verificar que retorna resultado anterior se já completou

### 3. Testes de Recuperação
- ✅ Saga interrompida no Step 1 → retoma do Step 1
- ✅ Saga interrompida no Step 2 → retoma do Step 2
- ✅ Saga que excedeu retries → vai para dead letter

### 4. Testes de Compensação
- ✅ Compensação falha → retry automático
- ✅ Compensação após 3 tentativas → dead letter
- ✅ Verificar que pedido é cancelado corretamente

---

## 📈 Métricas e Monitoramento

### Métricas Recomendadas

1. **Taxa de Sucesso da Saga**
   - `saga_success_rate = (sagas_completed / sagas_started) * 100`

2. **Tempo Médio de Execução**
   - `avg_saga_duration = SUM(duration_ms) / COUNT(*)`

3. **Taxa de Retry**
   - `retry_rate = (sagas_retried / sagas_failed) * 100`

4. **Taxa de Compensação**
   - `compensation_rate = (sagas_compensated / sagas_failed) * 100`

5. **Dead Letter Queue Size**
   - `dead_letter_count = COUNT(*) FROM saga_dead_letters`

### Dashboards Sugeridos

- **Saga Health Dashboard**: Taxa de sucesso, tempo médio, top erros
- **Recovery Dashboard**: Sagas em retry, próximas tentativas
- **Dead Letter Dashboard**: Sagas que excederam retries, análise de causas

---

## 🔒 Considerações de Segurança

1. **Idempotency Key**: Deve ser gerado pelo cliente e ser único
2. **Rate Limiting**: Limitar número de sagas por cliente/minuto
3. **Audit Log**: Registrar todas as tentativas de retry
4. **Data Retention**: Política de retenção para dead letters (ex: 30 dias)

---

## 📚 Referências

- [Saga Pattern - Microservices.io](https://microservices.io/patterns/data/saga.html)
- [Idempotency Keys - Stripe API](https://stripe.com/docs/api/idempotent_requests)
- [Circuit Breaker Pattern - Resilience4j](https://resilience4j.readme.io/docs/circuitbreaker)
- [Eventual Consistency - Martin Kleppmann](https://martin.kleppmann.com/2015/05/11/please-stop-calling-databases-cp-or-ap.html)

---

## ✅ Checklist de Implementação

- [ ] Fase 1: Persistência com Checkpoints
- [ ] Fase 2: Idempotência
- [ ] Fase 3: Recovery Service
- [ ] Fase 4: Dead Letter Queue
- [ ] Testes unitários
- [ ] Testes de integração
- [ ] Testes de carga (verificar performance)
- [ ] Documentação atualizada
- [ ] Métricas configuradas
- [ ] Dashboards criados

---

**Autor**: Marcelo  
**Data**: 2024  
**Versão**: 1.0

