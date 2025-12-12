# 🧪 Guia Completo de Testes - Saga Pattern, Circuit Breaker e Resiliência

> **🎯 Objetivo:** Testar todos os cenários do Saga Pattern, Circuit Breaker e integração com AbacatePay para validação completa antes da apresentação na Accenture.

---

## 📋 Índice

1. [Configuração Inicial](#configuração-inicial)
2. [Cenários do Saga Pattern](#cenários-do-saga-pattern)
3. [Cenários de Circuit Breaker](#cenários-de-circuit-breaker)
4. [Cenários de Integração AbacatePay](#cenários-de-integração-abacatepay)
5. [Validação de Status e Persistência](#validação-de-status-e-persistência)
6. [Observabilidade e Métricas](#observabilidade-e-métricas)
7. [Checklist para Apresentação](#checklist-para-apresentação)

---

## 🔧 Configuração Inicial

### Pré-requisitos

1. **Backend rodando:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **PostgreSQL rodando:**
   ```bash
   docker-compose up -d postgres
   ```

3. **Variáveis de Ambiente:**
   ```bash
   export ABACATEPAY_API_KEY=sua_chave_api
   export ABACATEPAY_BASE_URL=https://api.abacatepay.com/v1
   ```

### Endpoints Disponíveis

- **API:** `http://localhost:8080/api/v1/orders`
- **Swagger:** `http://localhost:8080/swagger-ui.html`
- **Health Check:** `http://localhost:8080/actuator/health`
- **Metrics:** `http://localhost:8080/actuator/metrics`
- **Circuit Breaker State:** `http://localhost:8080/actuator/circuitbreakers`

---

## 🎭 Cenários do Saga Pattern

### Cenário 1: Fluxo Completo com Sucesso ✅

**Objetivo:** Validar que a saga executa todos os 3 passos com sucesso.

#### 1.1 Criar Pedido

```bash
POST http://localhost:8080/api/v1/orders
Content-Type: application/json

{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "João Silva",
  "customerEmail": "joao@example.com",
  "items": [
    {
      "productId": "prod-123",
      "productName": "Produto Teste",
      "quantity": 2,
      "unitPrice": 50.00
    }
  ]
}
```

**✅ Resposta Esperada (HTTP 201):**
```json
{
  "success": true,
  "order": {
    "id": "...",
    "orderNumber": "ORD-...",
    "status": "PAID",
    "totalAmount": 100.00,
    "customerEmail": "joao@example.com",
    "items": [...]
  },
  "sagaExecutionId": "...",
  "errorMessage": null
}
```

**🔍 O que verificar:**

1. **Status do Pedido:**
   ```sql
   SELECT id, order_number, status, total_amount 
   FROM orders 
   WHERE order_number = 'ORD-...';
   ```
   - ✅ Status deve ser `PAID`

2. **Saga Execution:**
   ```sql
   SELECT id, order_id, status, current_step, started_at, completed_at
   FROM saga_executions
   WHERE order_id = '...';
   ```
   - ✅ Status: `COMPLETED`
   - ✅ Current Step: `null` (todos os passos completos)
   - ✅ Completed At: não nulo

3. **Saga Steps:**
   ```sql
   SELECT step_name, status, started_at, completed_at, error_message
   FROM saga_steps
   WHERE saga_execution_id = '...'
   ORDER BY step_order;
   ```
   - ✅ Step 1: `CREATE_ORDER` - Status: `COMPLETED`
   - ✅ Step 2: `PROCESS_PAYMENT` - Status: `COMPLETED`
   - ✅ Step 3: `ANALYZE_RISK` - Status: `COMPLETED`

4. **Logs do Backend:**
   ```
   ✅ "Starting saga execution..."
   ✅ "Step CREATE_ORDER completed"
   ✅ "Step PROCESS_PAYMENT completed"
   ✅ "Step ANALYZE_RISK completed"
   ✅ "Saga completed successfully"
   ```

---

### Cenário 2: Falha no Pagamento (AbacatePay Retorna Erro) ❌

**Objetivo:** Validar que quando o pagamento falha, o status `PAYMENT_FAILED` é mantido e a compensação é executada.

#### 2.1 Simular Falha no AbacatePay

**Opção A: Usar chave de API inválida**
```bash
export ABACATEPAY_API_KEY=invalid_key
```

**Opção B: Usar URL inválida (timeout)**
```bash
export ABACATEPAY_BASE_URL=http://localhost:9999
```

#### 2.2 Criar Pedido (vai falhar no pagamento)

```bash
POST http://localhost:8080/api/v1/orders
Content-Type: application/json

{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "João Silva",
  "customerEmail": "joao@example.com",
  "items": [
    {
      "productId": "prod-123",
      "productName": "Produto Teste",
      "quantity": 1,
      "unitPrice": 50.00
    }
  ]
}
```

**❌ Resposta Esperada (HTTP 200 com erro):**
```json
{
  "success": false,
  "order": null,
  "sagaExecutionId": "...",
  "errorMessage": "Failed to process payment"
}
```

**🔍 O que verificar:**

1. **Status do Pedido:**
   ```sql
   SELECT id, order_number, status, total_amount 
   FROM orders 
   WHERE order_number = 'ORD-...';
   ```
   - ✅ Status deve ser `PAYMENT_FAILED` (NÃO `CANCELED`)
   - ⚠️ **IMPORTANTE:** Status `PAYMENT_FAILED` é mantido para o frontend identificar a causa

2. **Saga Execution:**
   ```sql
   SELECT id, order_id, status, current_step, error_message
   FROM saga_executions
   WHERE order_id = '...';
   ```
   - ✅ Status: `FAILED`
   - ✅ Current Step: `PROCESS_PAYMENT` (onde falhou)
   - ✅ Error Message: contém "payment" ou "Payment"

3. **Saga Steps:**
   ```sql
   SELECT step_name, status, error_message
   FROM saga_steps
   WHERE saga_execution_id = '...'
   ORDER BY step_order;
   ```
   - ✅ Step 1: `CREATE_ORDER` - Status: `COMPLETED`
   - ❌ Step 2: `PROCESS_PAYMENT` - Status: `FAILED`
   - ⏸️ Step 3: `ANALYZE_RISK` - Status: `NOT_STARTED` (não executado)

4. **Logs do Backend:**
   ```
   ✅ "Starting saga execution..."
   ✅ "Step CREATE_ORDER completed"
   ❌ "Step PROCESS_PAYMENT failed: ..."
   ⚠️ "Compensating saga... - Reason: Payment failed"
   ⚠️ "Order status is PAYMENT_FAILED, keeping status (not changing to CANCELED)"
   ```

5. **Buscar Pedidos com Status PAYMENT_FAILED:**
   ```bash
   GET http://localhost:8080/api/v1/orders?status=PAYMENT_FAILED
   ```
   - ✅ Deve retornar o pedido criado

---

### Cenário 3: Falha na Análise de Risco ⚠️

**Objetivo:** Validar que quando a análise de risco falha, o pedido já está pago e a saga falha, mas o pagamento não é revertido.

#### 3.1 Simular Falha no OpenAI

**Opção A: Usar chave de API inválida**
```bash
export OPENAI_API_KEY=invalid_key
```

**Opção B: Usar URL inválida**
```bash
export OPENAI_BASE_URL=http://localhost:9999
```

#### 3.2 Criar Pedido (pagamento sucesso, análise de risco falha)

```bash
POST http://localhost:8080/api/v1/orders
Content-Type: application/json

{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "João Silva",
  "customerEmail": "joao@example.com",
  "items": [
    {
      "productId": "prod-123",
      "productName": "Produto Teste",
      "quantity": 1,
      "unitPrice": 50.00
    }
  ]
}
```

**⚠️ Resposta Esperada:**
```json
{
  "success": false,
  "order": null,
  "sagaExecutionId": "...",
  "errorMessage": "Failed to analyze risk"
}
```

**🔍 O que verificar:**

1. **Status do Pedido:**
   ```sql
   SELECT id, order_number, status, total_amount 
   FROM orders 
   WHERE order_number = 'ORD-...';
   ```
   - ✅ Status deve ser `PAID` (pagamento foi processado)
   - ⚠️ **IMPORTANTE:** Pagamento não é revertido (AbacatePay já processou)

2. **Saga Execution:**
   ```sql
   SELECT id, order_id, status, current_step, error_message
   FROM saga_executions
   WHERE order_id = '...';
   ```
   - ✅ Status: `FAILED`
   - ✅ Current Step: `ANALYZE_RISK` (onde falhou)

3. **Saga Steps:**
   ```sql
   SELECT step_name, status, error_message
   FROM saga_steps
   WHERE saga_execution_id = '...'
   ORDER BY step_order;
   ```
   - ✅ Step 1: `CREATE_ORDER` - Status: `COMPLETED`
   - ✅ Step 2: `PROCESS_PAYMENT` - Status: `COMPLETED`
   - ❌ Step 3: `ANALYZE_RISK` - Status: `FAILED`

4. **Logs do Backend:**
   ```
   ✅ "Step CREATE_ORDER completed"
   ✅ "Step PROCESS_PAYMENT completed"
   ❌ "Step ANALYZE_RISK failed: ..."
   ⚠️ "Saga failed but payment was already processed"
   ```

---

## 🔌 Cenários de Circuit Breaker

### Configuração Atual (application.yml)

```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentGateway:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50  # Abre após 50% de falhas
        waitDurationInOpenState: 10s
```

**📊 Interpretação:**
- **Sliding Window:** Últimas 10 chamadas
- **Minimum Calls:** Precisa de 5 chamadas antes de avaliar
- **Failure Threshold:** Abre circuito após 50% de falhas (3 de 5)
- **Wait Duration:** Aguarda 10s antes de tentar fechar (half-open)

---

### Cenário 4: Circuit Breaker Fechado (Normal) ✅

**Objetivo:** Validar que com sucesso, o circuito permanece fechado.

#### 4.1 Executar 10 Pedidos com Sucesso

```bash
# Script para criar 10 pedidos
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/v1/orders \
    -H "Content-Type: application/json" \
    -d '{
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "customerName": "Cliente Teste",
      "customerEmail": "teste@example.com",
      "items": [{"productId": "prod-123", "productName": "Produto", "quantity": 1, "unitPrice": 50.00}]
    }'
  echo ""
  sleep 1
done
```

**🔍 O que verificar:**

1. **Estado do Circuit Breaker:**
   ```bash
   GET http://localhost:8080/actuator/circuitbreakers
   ```
   - ✅ Estado: `CLOSED`
   - ✅ Success Count: 10
   - ✅ Failure Count: 0
   - ✅ Failure Rate: 0%

2. **Logs:**
   ```
   ✅ "Processing payment via AbacatePay..."
   ✅ "Payment successful..."
   ```
   - ✅ Nenhuma chamada ao fallback

---

### Cenário 5: Circuit Breaker Abrindo (Falhas Acumulando) ⚠️

**Objetivo:** Validar que após 3 falhas em 5 chamadas, o circuito abre.

#### 5.1 Simular Falhas no AbacatePay

```bash
export ABACATEPAY_BASE_URL=http://localhost:9999  # URL inválida
```

#### 5.2 Executar 5 Pedidos (vão falhar)

```bash
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/v1/orders \
    -H "Content-Type: application/json" \
    -d '{
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "customerName": "Cliente Teste",
      "customerEmail": "teste@example.com",
      "items": [{"productId": "prod-123", "productName": "Produto", "quantity": 1, "unitPrice": 50.00}]
    }'
  echo ""
  sleep 0.5
done
```

**🔍 O que verificar:**

1. **Estado do Circuit Breaker (após 5 chamadas):**
   ```bash
   GET http://localhost:8080/actuator/circuitbreakers
   ```
   - ⚠️ Estado: `OPEN` (após 3+ falhas em 5 chamadas)
   - ❌ Success Count: 0
   - ❌ Failure Count: 5
   - ❌ Failure Rate: 100%

2. **Logs:**
   ```
   ❌ "AbacatePay API error: Connection refused"
   ⚠️ "Circuit breaker open for AbacatePay. Returning failed payment result."
   ```
   - ✅ Após circuito aberto, fallback é chamado imediatamente

3. **Tempo de Resposta:**
   - ✅ Com circuito aberto, resposta é instantânea (fallback)
   - ✅ Sem esperar timeout da conexão

---

### Cenário 6: Circuit Breaker Half-Open (Tentando Recuperar) 🔄

**Objetivo:** Validar que após 10s, o circuito tenta fechar (half-open).

#### 6.1 Aguardar 10 Segundos

```bash
sleep 10
```

#### 6.2 Restaurar AbacatePay

```bash
export ABACATEPAY_BASE_URL=https://api.abacatepay.com/v1
export ABACATEPAY_API_KEY=sua_chave_valida
```

#### 6.3 Tentar 1 Pedido (teste half-open)

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "customerName": "Cliente Teste",
    "customerEmail": "teste@example.com",
    "items": [{"productId": "prod-123", "productName": "Produto", "quantity": 1, "unitPrice": 50.00}]
  }'
```

**🔍 O que verificar:**

1. **Estado do Circuit Breaker:**
   ```bash
   GET http://localhost:8080/actuator/circuitbreakers
   ```
   - 🔄 Estado: `HALF_OPEN` (tentando recuperar)
   - ✅ Se sucesso: volta para `CLOSED`
   - ❌ Se falha: volta para `OPEN` (aguarda mais 10s)

2. **Logs:**
   ```
   🔄 "Circuit breaker half-open, attempting call..."
   ✅ "Payment successful" → Circuito fecha
   ❌ "Payment failed" → Circuito abre novamente
   ```

---

### Cenário 7: Retry em Ação (Falhas Transitórias) 🔄

**Objetivo:** Validar que retry tenta novamente em falhas transitórias.

#### Configuração de Retry

```yaml
resilience4j:
  retry:
    instances:
      paymentGateway:
        maxAttempts: 3
        waitDuration: 1s
        retryExceptions: java.net.ConnectException, java.util.concurrent.TimeoutException
```

#### 7.1 Simular Timeout Intermitente

**Opção A: Usar ferramenta para simular timeout**
```bash
# Usar mock server que responde lentamente
```

**Opção B: Usar URL que demora para responder**
```bash
export ABACATEPAY_BASE_URL=http://httpbin.org/delay/2
```

#### 7.2 Criar Pedido

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "customerName": "Cliente Teste",
    "customerEmail": "teste@example.com",
    "items": [{"productId": "prod-123", "productName": "Produto", "quantity": 1, "unitPrice": 50.00}]
  }'
```

**🔍 O que verificar:**

1. **Logs:**
   ```
   🔄 "Attempt 1: TimeoutException..."
   ⏳ "Waiting 1s before retry..."
   🔄 "Attempt 2: TimeoutException..."
   ⏳ "Waiting 1s before retry..."
   🔄 "Attempt 3: TimeoutException..."
   ❌ "All retry attempts failed"
   ```

2. **Métricas de Retry:**
   ```bash
   GET http://localhost:8080/actuator/metrics/resilience4j.retry.calls
   ```
   - ✅ Total Calls: 1
   - ✅ Successful Calls: 0
   - ✅ Failed Calls: 1
   - ✅ Retry Attempts: 3

---

## 🔗 Cenários de Integração AbacatePay

### Cenário 8: Pagamento Bem-Sucedido ✅

**Objetivo:** Validar integração completa com AbacatePay retornando sucesso.

#### 8.1 Configurar AbacatePay Válido

```bash
export ABACATEPAY_API_KEY=sua_chave_valida
export ABACATEPAY_BASE_URL=https://api.abacatepay.com/v1
```

#### 8.2 Criar Pedido

```bash
POST http://localhost:8080/api/v1/orders
Content-Type: application/json

{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "João Silva",
  "customerEmail": "joao@example.com",
  "items": [
    {
      "productId": "prod-123",
      "productName": "Produto Teste",
      "quantity": 1,
      "unitPrice": 100.00
    }
  ]
}
```

**✅ Resposta Esperada:**
```json
{
  "success": true,
  "order": {
    "id": "...",
    "status": "PAID",
    "totalAmount": 100.00
  },
  "sagaExecutionId": "...",
  "errorMessage": null
}
```

**🔍 O que verificar:**

1. **Request Enviado ao AbacatePay:**
   - ✅ Endpoint: `POST /v1/billing/create`
   - ✅ Headers: `Authorization: Bearer {api_key}`
   - ✅ Body: `{"amount": 10000, "description": "Pedido ...", "methods": ["PIX", "CARD"], "frequency": "ONE_TIME"}`

2. **Response do AbacatePay:**
   - ✅ Status: `200 OK`
   - ✅ Body: `{"success": true, "data": {"id": "...", "status": "PAID", "amount": 10000}}`

3. **Mapeamento:**
   - ✅ `AbacatePayBillingResponse` → `PaymentResult`
   - ✅ Status `PAID` → `PaymentStatus.SUCCESS`
   - ✅ Amount em centavos → BigDecimal em reais

4. **Persistência:**
   ```sql
   SELECT id, status, payment_id 
   FROM orders 
   WHERE order_number = 'ORD-...';
   ```
   - ✅ Status: `PAID`
   - ✅ Payment ID: ID retornado pelo AbacatePay

---

### Cenário 9: Pagamento Falhado (API Retorna Erro) ❌

**Objetivo:** Validar que quando AbacatePay retorna erro, o sistema trata graciosamente.

#### 9.1 Simular Erro no AbacatePay

**Opção A: Usar chave de API inválida**
```bash
export ABACATEPAY_API_KEY=invalid_key
```

**Opção B: Usar mock server que retorna 400/500**
```bash
# Configurar mock server para retornar erro
```

#### 9.2 Criar Pedido

```bash
POST http://localhost:8080/api/v1/orders
Content-Type: application/json

{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "João Silva",
  "customerEmail": "joao@example.com",
  "items": [
    {
      "productId": "prod-123",
      "productName": "Produto Teste",
      "quantity": 1,
      "unitPrice": 100.00
    }
  ]
}
```

**❌ Resposta Esperada:**
```json
{
  "success": false,
  "order": null,
  "sagaExecutionId": "...",
  "errorMessage": "Failed to process payment"
}
```

**🔍 O que verificar:**

1. **Response do AbacatePay:**
   - ❌ Status: `401 Unauthorized` ou `400 Bad Request`
   - ❌ Body: `{"success": false, "error": "Invalid API key"}`

2. **Tratamento no Adapter:**
   ```java
   // AbacatePayAdapter.processPayment()
   catch (WebClientResponseException e) {
       log.error("AbacatePay API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
       return createFailedResult(amount, "AbacatePay API error: " + e.getStatusCode());
   }
   ```
   - ✅ Exceção é capturada e convertida em `PaymentResult` com falha
   - ✅ Não lança exceção (fail-fast controlado)

3. **Status do Pedido:**
   ```sql
   SELECT id, status 
   FROM orders 
   WHERE order_number = 'ORD-...';
   ```
   - ✅ Status: `PAYMENT_FAILED`

4. **Logs:**
   ```
   ❌ "AbacatePay API error for order ...: 401 - Invalid API key"
   ⚠️ "Payment failed for order ... - Reason: AbacatePay API error: 401"
   ```

---

### Cenário 10: Timeout na Integração ⏱️

**Objetivo:** Validar que timeout é tratado e retry é executado.

#### 10.1 Configurar Timeout

**WebClient timeout (configurado em AbacatePayConfig):**
```java
WebClient.builder()
    .clientConnector(new ReactorClientHttpConnector(
        HttpClient.create()
            .responseTimeout(Duration.ofSeconds(5))  // 5 segundos
    ))
```

#### 10.2 Simular Timeout

```bash
# Usar URL que demora mais que 5s
export ABACATEPAY_BASE_URL=http://httpbin.org/delay/10
```

#### 10.3 Criar Pedido

```bash
POST http://localhost:8080/api/v1/orders
Content-Type: application/json

{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "João Silva",
  "customerEmail": "joao@example.com",
  "items": [
    {
      "productId": "prod-123",
      "productName": "Produto Teste",
      "quantity": 1,
      "unitPrice": 100.00
    }
  ]
}
```

**🔍 O que verificar:**

1. **Timeout Ocorre:**
   - ✅ Após 5s, `TimeoutException` é lançado
   - ✅ Retry tenta novamente (3 tentativas)

2. **Logs:**
   ```
   ⏱️ "Processing payment via AbacatePay..."
   ❌ "TimeoutException after 5s"
   🔄 "Retry attempt 1..."
   ❌ "TimeoutException after 5s"
   🔄 "Retry attempt 2..."
   ❌ "TimeoutException after 5s"
   ❌ "All retry attempts failed"
   ```

3. **Status do Pedido:**
   - ✅ Status: `PAYMENT_FAILED`

---

## 📊 Validação de Status e Persistência

### Tabela de Status Esperados

| Cenário | Status do Pedido | Status da Saga | Observações |
|---------|------------------|----------------|-------------|
| Sucesso completo | `PAID` | `COMPLETED` | Todos os 3 passos OK |
| Falha no pagamento | `PAYMENT_FAILED` | `FAILED` | Status mantido (não muda para CANCELED) |
| Falha na análise | `PAID` | `FAILED` | Pagamento já processado |
| Circuit Breaker aberto | `PAYMENT_FAILED` | `FAILED` | Fallback retorna falha |

### Query para Validar Status

```sql
-- Buscar pedidos por status
SELECT 
    o.id,
    o.order_number,
    o.status,
    o.total_amount,
    o.created_at,
    se.status as saga_status,
    se.current_step,
    se.error_message
FROM orders o
LEFT JOIN saga_executions se ON se.order_id = o.id
WHERE o.status = 'PAYMENT_FAILED'  -- ou 'PAID', 'PENDING', 'CANCELED'
ORDER BY o.created_at DESC;
```

### Endpoint para Buscar por Status

```bash
GET http://localhost:8080/api/v1/orders?status=PAYMENT_FAILED
GET http://localhost:8080/api/v1/orders?status=PAID
GET http://localhost:8080/api/v1/orders?status=PENDING
GET http://localhost:8080/api/v1/orders?status=CANCELED
```

---

## 📈 Observabilidade e Métricas

### Métricas Disponíveis

#### 1. Health Check

```bash
GET http://localhost:8080/actuator/health
```

**Resposta:**
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

#### 2. Circuit Breaker Metrics

```bash
GET http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls
GET http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state
```

**Métricas importantes:**
- `resilience4j.circuitbreaker.calls{state="successful"}` - Chamadas bem-sucedidas
- `resilience4j.circuitbreaker.calls{state="failed"}` - Chamadas falhadas
- `resilience4j.circuitbreaker.state{state="closed"}` - Circuito fechado
- `resilience4j.circuitbreaker.state{state="open"}` - Circuito aberto

#### 3. Retry Metrics

```bash
GET http://localhost:8080/actuator/metrics/resilience4j.retry.calls
```

**Métricas importantes:**
- `resilience4j.retry.calls{result="successful"}` - Retries bem-sucedidos
- `resilience4j.retry.calls{result="failed"}` - Retries falhados
- `resilience4j.retry.calls{result="retry"}` - Tentativas de retry

#### 4. Saga Execution Metrics

```sql
-- Total de sagas executadas
SELECT COUNT(*) as total_sagas
FROM saga_executions;

-- Taxa de sucesso
SELECT 
    status,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM saga_executions), 2) as percentage
FROM saga_executions
GROUP BY status;

-- Tempo médio de execução
SELECT 
    AVG(EXTRACT(EPOCH FROM (completed_at - started_at))) as avg_duration_seconds
FROM saga_executions
WHERE completed_at IS NOT NULL;
```

---

## ✅ Checklist para Apresentação

### Pré-Apresentação

- [ ] Backend rodando e saudável
- [ ] PostgreSQL conectado
- [ ] AbacatePay configurado (chave válida)
- [ ] Swagger acessível
- [ ] Actuator endpoints funcionando

### Demonstrações Obrigatórias

#### 1. Saga Pattern - Fluxo Completo ✅
- [ ] Criar pedido com sucesso
- [ ] Verificar status `PAID` no banco
- [ ] Verificar saga `COMPLETED` com 3 steps completos
- [ ] Mostrar logs da execução

#### 2. Saga Pattern - Compensação ❌
- [ ] Simular falha no pagamento
- [ ] Verificar status `PAYMENT_FAILED` (não `CANCELED`)
- [ ] Verificar saga `FAILED` com step `PROCESS_PAYMENT` falhado
- [ ] Mostrar que compensação mantém status específico

#### 3. Circuit Breaker - Proteção ⚠️
- [ ] Simular 5 falhas consecutivas
- [ ] Verificar circuito abrindo (`OPEN`)
- [ ] Mostrar que requisições seguintes são rejeitadas instantaneamente (fallback)
- [ ] Mostrar métricas do Circuit Breaker

#### 4. Circuit Breaker - Recuperação 🔄
- [ ] Aguardar 10s (waitDurationInOpenState)
- [ ] Restaurar serviço
- [ ] Tentar 1 requisição (half-open)
- [ ] Verificar circuito fechando (`CLOSED`) se sucesso

#### 5. Retry - Resiliência 🔄
- [ ] Simular timeout intermitente
- [ ] Mostrar 3 tentativas nos logs
- [ ] Verificar métricas de retry

#### 6. Integração AbacatePay ✅
- [ ] Mostrar request enviado ao AbacatePay
- [ ] Mostrar response recebido
- [ ] Mostrar mapeamento para domínio
- [ ] Verificar persistência do `payment_id`

#### 7. Observabilidade 📊
- [ ] Mostrar health check
- [ ] Mostrar métricas do Circuit Breaker
- [ ] Mostrar métricas de Retry
- [ ] Mostrar queries SQL para saga executions

### Pontos de Destaque para Entrevista

1. **Arquitetura Hexagonal:**
   - ✅ Domínio isolado (não conhece AbacatePay)
   - ✅ Ports e Adapters
   - ✅ Dependency Inversion

2. **Saga Pattern:**
   - ✅ Orquestração centralizada
   - ✅ Compensação automática
   - ✅ Rastreamento completo

3. **Resiliência:**
   - ✅ Circuit Breaker protege contra falhas em cascata
   - ✅ Retry para falhas transitórias
   - ✅ Fallback gracioso

4. **Observabilidade:**
   - ✅ Logs estruturados
   - ✅ Métricas expostas
   - ✅ Rastreamento de saga

5. **Clean Code:**
   - ✅ SOLID principles
   - ✅ Testes unitários
   - ✅ Documentação completa

---

## 🎯 Scripts Úteis

### Script para Teste Completo

```bash
#!/bin/bash

echo "🧪 Teste Completo - Saga Pattern e Circuit Breaker"
echo "=================================================="

# 1. Health Check
echo "1. Verificando health..."
curl -s http://localhost:8080/actuator/health | jq

# 2. Criar pedido com sucesso
echo "2. Criando pedido com sucesso..."
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "customerName": "João Silva",
    "customerEmail": "joao@example.com",
    "items": [{"productId": "prod-123", "productName": "Produto", "quantity": 1, "unitPrice": 50.00}]
  }')

echo "$ORDER_RESPONSE" | jq

# 3. Verificar status
ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r '.order.id')
echo "3. Verificando status do pedido $ORDER_ID..."
curl -s "http://localhost:8080/api/v1/orders/$ORDER_ID" | jq

# 4. Circuit Breaker state
echo "4. Estado do Circuit Breaker..."
curl -s http://localhost:8080/actuator/circuitbreakers | jq

echo "✅ Teste completo!"
```

### Script para Simular Falhas

```bash
#!/bin/bash

echo "⚠️ Simulando falhas para abrir Circuit Breaker..."

# Configurar URL inválida
export ABACATEPAY_BASE_URL=http://localhost:9999

# Criar 5 pedidos (vão falhar)
for i in {1..5}; do
  echo "Tentativa $i..."
  curl -s -X POST http://localhost:8080/api/v1/orders \
    -H "Content-Type: application/json" \
    -d '{
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "customerName": "Cliente Teste",
      "customerEmail": "teste@example.com",
      "items": [{"productId": "prod-123", "productName": "Produto", "quantity": 1, "unitPrice": 50.00}]
    }' | jq -r '.errorMessage'
  sleep 0.5
done

echo "✅ 5 falhas simuladas. Circuit Breaker deve estar OPEN."
```

---

## 📝 Notas Finais

### Para a Apresentação

1. **Prepare-se para perguntas sobre:**
   - Por que Saga Pattern ao invés de transação distribuída?
   - Como funciona a compensação?
   - O que acontece se o sistema cair durante a saga?
   - Como garantir idempotência?

2. **Demonstre conhecimento:**
   - Arquitetura Hexagonal
   - SOLID principles
   - Design Patterns (Saga, Circuit Breaker, Factory)
   - Resiliência em microserviços

3. **Mostre código:**
   - `OrderSagaOrchestrator.java` - Orquestração
   - `AbacatePayAdapter.java` - Circuit Breaker
   - `OrderStatus.java` - State Machine
   - `application.yml` - Configuração

### Próximos Passos (Opcional)

- [ ] Implementar idempotência com chave única
- [ ] Adicionar métricas customizadas
- [ ] Implementar alertas baseados em métricas
- [ ] Adicionar testes de integração end-to-end
- [ ] Documentar estratégias de retry por tipo de erro

---

**🎉 Boa sorte na apresentação!**

