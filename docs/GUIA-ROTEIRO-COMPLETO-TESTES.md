# 🧪 Guia Roteiro Completo de Testes - Smart Order Orchestrator

> **🎯 Objetivo:** Roteiro completo e unificado para testar todo o sistema, incluindo backend, frontend, Saga Pattern, Circuit Breaker e integração com AbacatePay em modo teste.

---

## 📋 Índice

1. [Pré-requisitos e Configuração Inicial](#pré-requisitos-e-configuração-inicial)
2. [Roteiro de Testes - Backend (Bruno API Client)](#roteiro-de-testes---backend-bruno-api-client)
3. [Roteiro de Testes - AbacatePay (Modo Teste)](#roteiro-de-testes---abacatepay-modo-teste)
4. [Roteiro de Testes - Saga Pattern](#roteiro-de-testes---saga-pattern)
5. [Roteiro de Testes - Circuit Breaker e Resiliência](#roteiro-de-testes---circuit-breaker-e-resiliência)
6. [Roteiro de Testes - Testes Automatizados](#roteiro-de-testes---testes-automatizados)
   - [Visão Geral dos Testes Automatizados](#visão-geral-dos-testes-automatizados)
   - [Tipos de Testes](#tipos-de-testes)
   - [Estrutura de Testes](#estrutura-de-testes)
   - [Configuração de Ambiente de Teste](#configuração-de-ambiente-de-teste)
   - [CI/CD e Testes Automatizados](#cicd-e-testes-automatizados)
   - [Boas Práticas de Testes](#boas-práticas-de-testes)
7. [Checklist Completo de Validação](#checklist-completo-de-validação)
8. [Checklist de Cobertura de Testes](#checklist-de-cobertura-de-testes)
9. [Próximos Passos](#próximos-passos)
10. [Referências](#referências)

---

## ✅ Pré-requisitos e Configuração Inicial

### 1.1. Ferramentas Necessárias

- ✅ **Docker** rodando (banco PostgreSQL)
- ✅ **Java 21** instalado
- ✅ **Maven** instalado
- ✅ **Bruno API Client** instalado ([Download Bruno](https://www.usebruno.com/))
- ✅ **PostgreSQL** rodando via Docker
- ✅ **Backend** compilado e rodando
- ✅ **Frontend** (opcional, para testes E2E)

### 1.2. Verificar Banco de Dados

```bash
# Verificar containers Docker
docker ps

# Você deve ver algo como:
# CONTAINER ID   IMAGE              PORTS                    NAMES
# abc123def456   postgres:15        0.0.0.0:5432->5432/tcp   smartorder-postgres

# Conectar no PostgreSQL
docker exec -it smartorder-postgres psql -U postgres -d smartorder

# Verificar tabelas (após migrations)
\dt
```

**Tabelas esperadas:**
- `orders`
- `order_items`
- `saga_executions`
- `saga_steps`

### 1.3. Configurar Variáveis de Ambiente

#### Variáveis Obrigatórias (Mínimas)

```bash
# Windows PowerShell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/smartorder"
$env:DATABASE_USERNAME="postgres"
$env:DATABASE_PASSWORD="postgres"

# Windows CMD
set DATABASE_URL=jdbc:postgresql://localhost:5432/smartorder
set DATABASE_USERNAME=postgres
set DATABASE_PASSWORD=postgres

# Linux/Mac
export DATABASE_URL=jdbc:postgresql://localhost:5432/smartorder
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres
```

#### Variáveis para AbacatePay (Modo Teste)

```bash
# Windows PowerShell
$env:ABACATEPAY_API_KEY="sua_chave_teste_aqui"
$env:ABACATEPAY_BASE_URL="https://api.abacatepay.com/v1"

# Linux/Mac
export ABACATEPAY_API_KEY=sua_chave_teste_aqui
export ABACATEPAY_BASE_URL=https://api.abacatepay.com/v1
```

**📝 Como obter chave de teste do AbacatePay:**
1. Acesse: https://docs.abacatepay.com/pages/introduction
2. Crie uma conta de desenvolvedor
3. Acesse o painel e gere uma chave de API de teste
4. Use essa chave na variável `ABACATEPAY_API_KEY`

**⚠️ Nota:** Se não configurar AbacatePay, o sistema usará mock (simulação).

### 1.4. Subir a Aplicação Backend

```bash
# Navegar para o diretório do backend
cd backend

# Executar a aplicação
mvn spring-boot:run

# Aguardar mensagem no console:
# Started OrchestratorApplication in X.XXX seconds
```

**Verificar Health Check:**
```bash
# No navegador ou Bruno
GET http://localhost:8080/actuator/health
```

**Resposta esperada:**
```json
{
  "status": "UP"
}
```

**Verificar Swagger:**
```
http://localhost:8080/swagger-ui/index.html
```

### 1.5. Configurar Bruno API Client

1. Abrir Bruno
2. Criar nova collection: `Smart Order Orchestrator`
3. Criar ambiente: `Local Development`
4. Configurar variável base URL:
   ```
   baseUrl = http://localhost:8080
   ```

**Estrutura de Pastas no Bruno:**
```
Smart Order Orchestrator/
├── Health Check/
│   └── GET Health
├── Orders/
│   ├── POST Create Order
│   ├── GET Get Order by ID
│   ├── GET Get Order by Number
│   └── GET List All Orders
└── Observability/
    └── GET Saga Execution (via SQL)
```

---

## 🧪 Roteiro de Testes - Backend (Bruno API Client)

### Teste 1: Health Check ✅

**Objetivo:** Verificar se o backend está rodando e saudável.

**Método:** `GET`  
**URL:** `{{baseUrl}}/actuator/health`

**✅ Resposta Esperada:**
```json
{
  "status": "UP"
}
```

---

### Teste 2: Criar Pedido com Sucesso Completo ✅

**Objetivo:** Criar um pedido completo e verificar toda a saga sendo executada.

**Método:** `POST`  
**URL:** `{{baseUrl}}/api/v1/orders`  
**Headers:**
```json
{
  "Content-Type": "application/json"
}
```

**Body (JSON):**
```json
{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "João Silva",
  "customerEmail": "joao.silva@email.com",
  "items": [
    {
      "productId": "660e8400-e29b-41d4-a716-446655440001",
      "productName": "Notebook Dell",
      "quantity": 1,
      "unitPrice": 3500.00
    },
    {
      "productId": "770e8400-e29b-41d4-a716-446655440002",
      "productName": "Mouse Logitech",
      "quantity": 2,
      "unitPrice": 89.90
    }
  ],
  "paymentMethod": "CREDIT_CARD",
  "currency": "BRL",
  "idempotencyKey": "test-idempotency-key-001"
}
```

**✅ Resposta Esperada (HTTP 201):**
```json
{
  "success": true,
  "order": {
    "id": "uuid-do-pedido",
    "orderNumber": "ORD-1234567890",
    "status": "PAID",
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "customerName": "João Silva",
    "customerEmail": "joao.silva@email.com",
    "items": [
      {
        "productId": "660e8400-e29b-41d4-a716-446655440001",
        "productName": "Notebook Dell",
        "quantity": 1,
        "unitPrice": 3500.00,
        "subtotal": 3500.00
      },
      {
        "productId": "770e8400-e29b-41d4-a716-446655440002",
        "productName": "Mouse Logitech",
        "quantity": 2,
        "unitPrice": 89.90,
        "subtotal": 179.80
      }
    ],
    "totalAmount": 3679.80,
    "paymentId": "payment-id-123",
    "riskLevel": "LOW",
    "createdAt": "2024-12-XX...",
    "updatedAt": "2024-12-XX..."
  },
  "sagaExecutionId": "uuid-da-execucao-saga"
}
```

**🔍 O que verificar:**

1. **Status do Pedido no Banco:**
   ```sql
   SELECT id, order_number, status, total_amount 
   FROM orders 
   WHERE order_number = 'ORD-1234567890';
   ```
   - ✅ Status deve ser `PAID`

2. **Saga Execution:**
   ```sql
   SELECT id, order_id, status, current_step, started_at, completed_at
   FROM saga_executions
   WHERE order_id = 'uuid-do-pedido';
   ```
   - ✅ Status: `COMPLETED`
   - ✅ Current Step: `null` (todos os passos completos)

3. **Saga Steps:**
   ```sql
   SELECT step_name, status, started_at, completed_at, error_message
   FROM saga_steps
   WHERE saga_execution_id = 'uuid-da-execucao-saga'
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

**💾 Salvar o `orderId` e `sagaExecutionId` para próximos testes!**

---

### Teste 3: Testar Idempotência 🔁

**Objetivo:** Verificar que requisições duplicadas não criam pedidos duplicados.

**Método:** `POST`  
**URL:** `{{baseUrl}}/api/v1/orders`  
**Body:** Usar EXATAMENTE o mesmo `idempotencyKey` do Teste 2:
```json
{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "João Silva",
  "customerEmail": "joao.silva@email.com",
  "items": [
    {
      "productId": "660e8400-e29b-41d4-a716-446655440001",
      "productName": "Notebook Dell",
      "quantity": 1,
      "unitPrice": 3500.00
    }
  ],
  "paymentMethod": "CREDIT_CARD",
  "currency": "BRL",
  "idempotencyKey": "test-idempotency-key-001"  // ← MESMA CHAVE!
}
```

**✅ Resposta Esperada (HTTP 202 Accepted):**
```json
{
  "success": false,
  "order": null,
  "sagaExecutionId": "uuid-da-execucao-saga-anterior",
  "errorMessage": "Order creation is already in progress"
}
```

**🔍 O que verificar:**
- ✅ Sistema detectou que já existe saga com essa `idempotencyKey`
- ✅ Retornou HTTP 202 (Accepted) em vez de criar novo pedido
- ✅ **Zero duplicação!** - Idempotência funcionando!

---

### Teste 4: Consultar Pedido Criado 🔍

**Objetivo:** Buscar um pedido específico pelo ID.

**Método:** `GET`  
**URL:** `{{baseUrl}}/api/v1/orders/{{orderId}}`

**💡 Substituir `{{orderId}}` pelo ID do pedido criado no Teste 2**

**✅ Resposta Esperada (HTTP 200):**
```json
{
  "id": "uuid-do-pedido",
  "orderNumber": "ORD-1234567890",
  "status": "PAID",
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "João Silva",
  "customerEmail": "joao.silva@email.com",
  "items": [...],
  "totalAmount": 3679.80,
  "paymentId": "payment-id-123",
  "riskLevel": "LOW",
  "createdAt": "2024-12-XX...",
  "updatedAt": "2024-12-XX..."
}
```

---

### Teste 5: Listar Todos os Pedidos 📋

**Objetivo:** Ver todos os pedidos criados.

**Método:** `GET`  
**URL:** `{{baseUrl}}/api/v1/orders`

**✅ Resposta Esperada (HTTP 200):**
```json
[
  {
    "id": "uuid-pedido-1",
    "orderNumber": "ORD-1234567890",
    "status": "PAID",
    ...
  },
  {
    "id": "uuid-pedido-2",
    "orderNumber": "ORD-0987654321",
    "status": "PENDING",
    ...
  }
]
```

---

### Teste 6: Buscar por Número do Pedido 🔢

**Objetivo:** Buscar pedido pelo número (ex: ORD-1234567890).

**Método:** `GET`  
**URL:** `{{baseUrl}}/api/v1/orders/number/ORD-1234567890`

**💡 Substituir `ORD-1234567890` pelo número do pedido criado no Teste 2**

**✅ Resposta Esperada (HTTP 200):**
```json
{
  "id": "uuid-do-pedido",
  "orderNumber": "ORD-1234567890",
  "status": "PAID",
  ...
}
```

---

### Teste 7: Buscar Pedidos por Status 🔍

**Objetivo:** Filtrar pedidos por status específico.

**Método:** `GET`  
**URL:** `{{baseUrl}}/api/v1/orders?status=PAYMENT_FAILED`

**Status disponíveis:**
- `PENDING` - Pedido pendente
- `PAID` - Pedido pago
- `PAYMENT_FAILED` - Falha no pagamento
- `CANCELED` - Pedido cancelado

**✅ Resposta Esperada:**
```json
[
  {
    "id": "uuid-pedido-falha",
    "orderNumber": "ORD-XXXX",
    "status": "PAYMENT_FAILED",
    ...
  }
]
```

---

## 💳 Roteiro de Testes - AbacatePay (Modo Teste)

### Teste 8: Pagamento Bem-Sucedido no AbacatePay (Modo Teste) ✅

**Objetivo:** Validar integração completa com AbacatePay retornando sucesso em modo teste.

#### 8.1. Configurar AbacatePay em Modo Teste

**Passo 1: Obter Chave de API de Teste**

1. Acesse: https://docs.abacatepay.com/pages/introduction
2. Crie uma conta de desenvolvedor (se ainda não tiver)
3. Acesse o painel do desenvolvedor
4. Gere uma chave de API de **teste** (não use chave de produção!)
5. Copie a chave gerada

**Passo 2: Configurar Variáveis de Ambiente**

```bash
# Windows PowerShell
$env:ABACATEPAY_API_KEY="sua_chave_teste_aqui"
$env:ABACATEPAY_BASE_URL="https://api.abacatepay.com/v1"

# Linux/Mac
export ABACATEPAY_API_KEY=sua_chave_teste_aqui
export ABACATEPAY_BASE_URL=https://api.abacatepay.com/v1
```

**⚠️ IMPORTANTE:**
- Use **sempre** chave de teste em desenvolvimento
- Chaves de teste não processam pagamentos reais
- Chaves de teste têm limites diferentes de produção

**Passo 3: Reiniciar Backend (se já estava rodando)**

```bash
# Parar o backend (Ctrl+C)
# Subir novamente
cd backend
mvn spring-boot:run
```

#### 8.2. Criar Pedido com Pagamento Real no AbacatePay

**Método:** `POST`  
**URL:** `{{baseUrl}}/api/v1/orders`  
**Headers:**
```json
{
  "Content-Type": "application/json"
}
```

**Body (JSON):**
```json
{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "João Silva",
  "customerEmail": "joao.silva@email.com",
  "items": [
    {
      "productId": "660e8400-e29b-41d4-a716-446655440001",
      "productName": "Produto Teste AbacatePay",
      "quantity": 1,
      "unitPrice": 100.00
    }
  ],
  "paymentMethod": "CREDIT_CARD",
  "currency": "BRL",
  "idempotencyKey": "test-abacatepay-001"
}
```

**✅ Resposta Esperada (HTTP 201):**
```json
{
  "success": true,
  "order": {
    "id": "uuid-do-pedido",
    "orderNumber": "ORD-1234567890",
    "status": "PAID",
    "totalAmount": 100.00,
    "paymentId": "payment-id-do-abacatepay",
    "riskLevel": "LOW",
    ...
  },
  "sagaExecutionId": "uuid-da-execucao-saga"
}
```

#### 8.3. O que Verificar

**1. Request Enviado ao AbacatePay:**

Verificar nos logs do backend:
```
✅ "Processing payment via AbacatePay..."
✅ "POST https://api.abacatepay.com/v1/billing/create"
✅ "Authorization: Bearer {api_key}"
✅ "Body: {"amount": 10000, "description": "Pedido ...", "methods": ["PIX", "CARD"], "frequency": "ONE_TIME"}"
```

**2. Response do AbacatePay:**

Verificar nos logs:
```
✅ "AbacatePay response: 200 OK"
✅ "Response body: {"success": true, "data": {"id": "...", "status": "PAID", "amount": 10000}}"
```

**3. Mapeamento Correto:**

Verificar no banco de dados:
```sql
SELECT id, order_number, status, payment_id, total_amount
FROM orders
WHERE order_number = 'ORD-1234567890';
```

**O que deve estar correto:**
- ✅ Status: `PAID`
- ✅ Payment ID: ID retornado pelo AbacatePay (não null)
- ✅ Total Amount: 100.00 (convertido de centavos para reais)

**4. Persistência do Payment ID:**

```sql
SELECT payment_id 
FROM orders 
WHERE order_number = 'ORD-1234567890';
```

- ✅ `payment_id` deve conter o ID retornado pelo AbacatePay
- ✅ Não deve ser `null`

**5. Saga Step de Pagamento:**

```sql
SELECT step_name, status, started_at, completed_at, error_message
FROM saga_steps
WHERE saga_execution_id = 'uuid-da-execucao-saga'
  AND step_name = 'PROCESS_PAYMENT';
```

- ✅ Status: `COMPLETED`
- ✅ Error Message: `null`
- ✅ Completed At: não nulo

#### 8.4. Verificar no Painel do AbacatePay (Opcional)

1. Acesse o painel do desenvolvedor do AbacatePay
2. Navegue até "Cobranças" ou "Billing"
3. Procure pela cobrança criada usando o `payment_id` retornado
4. Verifique que a cobrança está com status `PAID`

#### 8.5. Testar Valores Diferentes

**Teste com valor maior:**
```json
{
  "items": [
    {
      "productId": "prod-123",
      "productName": "Produto Caro",
      "quantity": 1,
      "unitPrice": 5000.00
    }
  ]
}
```

**Teste com múltiplos itens:**
```json
{
  "items": [
    {
      "productId": "prod-1",
      "productName": "Item 1",
      "quantity": 2,
      "unitPrice": 50.00
    },
    {
      "productId": "prod-2",
      "productName": "Item 2",
      "quantity": 3,
      "unitPrice": 30.00
    }
  ]
}
```

**Verificar cálculo correto:**
- Item 1: 2 × 50.00 = 100.00
- Item 2: 3 × 30.00 = 90.00
- **Total: 190.00**

---

### Teste 9: Pagamento Falhado no AbacatePay ❌

**Objetivo:** Validar que quando AbacatePay retorna erro, o sistema trata graciosamente.

#### 9.1. Simular Erro no AbacatePay

**Opção A: Usar chave de API inválida**
```bash
export ABACATEPAY_API_KEY=invalid_key
```

**Opção B: Usar URL inválida (timeout)**
```bash
export ABACATEPAY_BASE_URL=http://localhost:9999
```

#### 9.2. Criar Pedido (vai falhar no pagamento)

**Método:** `POST`  
**URL:** `{{baseUrl}}/api/v1/orders`  
**Body:**
```json
{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "João Silva",
  "customerEmail": "joao.silva@email.com",
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

**❌ Resposta Esperada (HTTP 200 com erro):**
```json
{
  "success": false,
  "order": null,
  "sagaExecutionId": "uuid-da-execucao-saga",
  "errorMessage": "Failed to process payment"
}
```

#### 9.3. O que Verificar

**1. Status do Pedido:**
```sql
SELECT id, order_number, status 
FROM orders 
WHERE order_number = 'ORD-...';
```
- ✅ Status deve ser `PAYMENT_FAILED` (NÃO `CANCELED`)

**2. Logs do Backend:**
```
❌ "AbacatePay API error: 401 - Invalid API key"
⚠️ "Payment failed for order ... - Reason: AbacatePay API error: 401"
```

**3. Buscar Pedidos com Falha:**
```bash
GET http://localhost:8080/api/v1/orders?status=PAYMENT_FAILED
```
- ✅ Deve retornar o pedido criado

---

## 🎭 Roteiro de Testes - Saga Pattern

### Teste 10: Fluxo Completo com Sucesso ✅

**Objetivo:** Validar que a saga executa todos os 3 passos com sucesso.

**Siga o Teste 2** (Criar Pedido com Sucesso Completo) e verifique:

1. ✅ Step 1: `CREATE_ORDER` - Status: `COMPLETED`
2. ✅ Step 2: `PROCESS_PAYMENT` - Status: `COMPLETED`
3. ✅ Step 3: `ANALYZE_RISK` - Status: `COMPLETED`
4. ✅ Saga Execution: Status `COMPLETED`

---

### Teste 11: Falha no Pagamento (Compensação) ❌

**Objetivo:** Validar que quando o pagamento falha, o status `PAYMENT_FAILED` é mantido e a compensação é executada.

**Siga o Teste 9** (Pagamento Falhado) e verifique:

1. ✅ Status do Pedido: `PAYMENT_FAILED` (NÃO `CANCELED`)
2. ✅ Saga Execution: Status `FAILED`
3. ✅ Current Step: `PROCESS_PAYMENT` (onde falhou)
4. ✅ Step 1: `CREATE_ORDER` - Status: `COMPLETED`
5. ❌ Step 2: `PROCESS_PAYMENT` - Status: `FAILED`
6. ⏸️ Step 3: `ANALYZE_RISK` - Status: `NOT_STARTED` (não executado)

**Logs esperados:**
```
✅ "Step CREATE_ORDER completed"
❌ "Step PROCESS_PAYMENT failed: ..."
⚠️ "Compensating saga... - Reason: Payment failed"
⚠️ "Order status is PAYMENT_FAILED, keeping status (not changing to CANCELED)"
```

---

### Teste 12: Falha na Análise de Risco ⚠️

**Objetivo:** Validar que quando a análise de risco falha, o pedido já está pago e a saga falha, mas o pagamento não é revertido.

#### 12.1. Simular Falha no OpenAI

```bash
export OPENAI_API_KEY=invalid_key
```

#### 12.2. Criar Pedido (pagamento sucesso, análise de risco falha)

**Método:** `POST`  
**URL:** `{{baseUrl}}/api/v1/orders`  
**Body:**
```json
{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "João Silva",
  "customerEmail": "joao.silva@email.com",
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

#### 12.3. O que Verificar

1. **Status do Pedido:**
   ```sql
   SELECT id, order_number, status 
   FROM orders 
   WHERE order_number = 'ORD-...';
   ```
   - ✅ Status deve ser `PAID` (pagamento foi processado)
   - ⚠️ **IMPORTANTE:** Pagamento não é revertido (AbacatePay já processou)

2. **Saga Steps:**
   ```sql
   SELECT step_name, status, error_message
   FROM saga_steps
   WHERE saga_execution_id = '...'
   ORDER BY step_order;
   ```
   - ✅ Step 1: `CREATE_ORDER` - Status: `COMPLETED`
   - ✅ Step 2: `PROCESS_PAYMENT` - Status: `COMPLETED`
   - ❌ Step 3: `ANALYZE_RISK` - Status: `FAILED`

---

## 🔌 Roteiro de Testes - Circuit Breaker e Resiliência

### Teste 13: Circuit Breaker Fechado (Normal) ✅

**Objetivo:** Validar que com sucesso, o circuito permanece fechado.

#### 13.1. Executar 10 Pedidos com Sucesso

```bash
# Script para criar 10 pedidos (Linux/Mac)
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

#### 13.2. Verificar Estado do Circuit Breaker

**Método:** `GET`  
**URL:** `http://localhost:8080/actuator/circuitbreakers`

**✅ Resposta Esperada:**
```json
{
  "circuitBreakers": [
    {
      "name": "paymentGateway",
      "state": "CLOSED",
      "metrics": {
        "successfulCalls": 10,
        "failedCalls": 0,
        "failureRate": 0.0
      }
    }
  ]
}
```

**O que verificar:**
- ✅ Estado: `CLOSED`
- ✅ Success Count: 10
- ✅ Failure Count: 0
- ✅ Failure Rate: 0%

---

### Teste 14: Circuit Breaker Abrindo (Falhas Acumulando) ⚠️

**Objetivo:** Validar que após 3 falhas em 5 chamadas, o circuito abre.

#### 14.1. Simular Falhas no AbacatePay

```bash
export ABACATEPAY_BASE_URL=http://localhost:9999  # URL inválida
```

#### 14.2. Executar 5 Pedidos (vão falhar)

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

#### 14.3. Verificar Estado do Circuit Breaker

**Método:** `GET`  
**URL:** `http://localhost:8080/actuator/circuitbreakers`

**⚠️ Resposta Esperada:**
```json
{
  "circuitBreakers": [
    {
      "name": "paymentGateway",
      "state": "OPEN",
      "metrics": {
        "successfulCalls": 0,
        "failedCalls": 5,
        "failureRate": 100.0
      }
    }
  ]
}
```

**O que verificar:**
- ⚠️ Estado: `OPEN` (após 3+ falhas em 5 chamadas)
- ❌ Success Count: 0
- ❌ Failure Count: 5
- ❌ Failure Rate: 100%

**Logs esperados:**
```
❌ "AbacatePay API error: Connection refused"
⚠️ "Circuit breaker open for AbacatePay. Returning failed payment result."
```

---

### Teste 15: Circuit Breaker Half-Open (Tentando Recuperar) 🔄

**Objetivo:** Validar que após 10s, o circuito tenta fechar (half-open).

#### 15.1. Aguardar 10 Segundos

```bash
sleep 10
```

#### 15.2. Restaurar AbacatePay

```bash
export ABACATEPAY_BASE_URL=https://api.abacatepay.com/v1
export ABACATEPAY_API_KEY=sua_chave_valida
```

#### 15.3. Tentar 1 Pedido (teste half-open)

**Método:** `POST`  
**URL:** `{{baseUrl}}/api/v1/orders`  
**Body:**
```json
{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "Cliente Teste",
  "customerEmail": "teste@example.com",
  "items": [{"productId": "prod-123", "productName": "Produto", "quantity": 1, "unitPrice": 50.00}]
}
```

#### 15.4. Verificar Estado do Circuit Breaker

**Método:** `GET`  
**URL:** `http://localhost:8080/actuator/circuitbreakers`

**O que verificar:**
- 🔄 Estado: `HALF_OPEN` (tentando recuperar)
- ✅ Se sucesso: volta para `CLOSED`
- ❌ Se falha: volta para `OPEN` (aguarda mais 10s)

**Logs esperados:**
```
🔄 "Circuit breaker half-open, attempting call..."
✅ "Payment successful" → Circuito fecha
❌ "Payment failed" → Circuito abre novamente
```

---

## 🧪 Roteiro de Testes - Testes Automatizados

### Visão Geral dos Testes Automatizados

Este projeto utiliza uma estratégia de testes em **pirâmide de testes**, priorizando testes unitários (base) e complementando com testes de integração e end-to-end.

#### Stack de Testes

- **JUnit 5**: Framework de testes (incluído no `spring-boot-starter-test`)
- **Mockito**: Framework de mocking para testes unitários
- **Spring Boot Test**: Suporte para testes de integração
- **H2 Database**: Banco in-memory para testes (sem necessidade de Docker)

#### Cobertura Atual

- ✅ **Testes Unitários**: Domain, Application, Infrastructure
- ✅ **Testes de Integração**: Adapters, Repositories
- ✅ **Testes de Saga**: Orquestração completa
- ⚠️ **Testes End-to-End**: A ser implementado (REST API)

---

### Teste 16: Executar Todos os Testes

**Objetivo:** Validar que todos os testes automatizados passam.

```bash
cd backend
mvn test
```

**✅ Resultado Esperado:**
```
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Outras formas de executar:**

**Executar testes de uma classe específica:**
```bash
mvn test -Dtest=OrderTest
```

**Executar testes de um pacote:**
```bash
mvn test -Dtest=com.marcelo.orchestrator.domain.model.*
```

**Executar testes com cobertura (JaCoCo - se configurado):**
```bash
mvn clean test jacoco:report
```

**Executar testes em modo verbose:**
```bash
mvn test -X
```

**Executar testes via IDE:**

**IntelliJ IDEA:**
1. Clique com botão direito na classe de teste
2. Selecione "Run 'ClassNameTest'"
3. Ou use atalho: `Ctrl+Shift+F10` (Windows/Linux) ou `Cmd+Shift+R` (Mac)

**VS Code:**
1. Instale extensão "Java Test Runner"
2. Clique no ícone de "Run Test" acima do método de teste

---

### Teste 17: Executar Testes de Integração

**Objetivo:** Validar que todos os testes de integração passam.

```bash
cd backend
mvn test -Dtest=*IntegrationTest
```

**✅ Resultado Esperado:**
```
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

### Teste 18: Executar Testes Específicos por Camada

#### Testes de Domínio

**Objetivo:** Testar regras de negócio encapsuladas nas entidades.

```bash
mvn test -Dtest=com.marcelo.orchestrator.domain.model.*
```

**Testes Existentes:**
- ✅ `OrderTest.java` - Cálculo de total, transições de status
- ✅ `OrderStatusTest.java` - Validação de transições de estado
- ✅ `MoneyTest.java` - Operações matemáticas com valores monetários

**Exemplo de Teste:**
```java
@Test
@DisplayName("Deve calcular total corretamente baseado nos itens")
void shouldCalculateTotalCorrectly() {
    // Arrange
    Order order = Order.builder()
        .items(List.of(item1, item2))
        .build();
    
    // Act
    order.calculateTotal();
    
    // Assert
    assertEquals(BigDecimal.valueOf(46.00), order.getTotalAmount());
}
```

**O que testar:**
- ✅ Regras de negócio (cálculos, validações)
- ✅ Transições de estado válidas e inválidas
- ✅ Imutabilidade de Value Objects
- ✅ Métodos de negócio encapsulados

#### Testes de Use Cases

```bash
mvn test -Dtest=com.marcelo.orchestrator.application.usecase.*
```

**Testes Existentes:**
- ✅ `AnalyzeRiskUseCaseTest.java` - Análise de risco
- ⚠️ `CreateOrderUseCaseTest.java` - A ser implementado
- ⚠️ `ProcessPaymentUseCaseTest.java` - A ser implementado

**Exemplo de Teste:**
```java
@ExtendWith(MockitoExtension.class)
class AnalyzeRiskUseCaseTest {
    @Mock
    private OrderRepositoryPort orderRepository;
    
    @Mock
    private RiskAnalysisPort riskAnalysisPort;
    
    @InjectMocks
    private AnalyzeRiskUseCase useCase;
    
    @Test
    void shouldAnalyzeRiskSuccessfully() {
        // Arrange
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(riskAnalysisPort.analyzeRisk(any())).thenReturn(analysis);
        
        // Act
        AnalyzeRiskResult result = useCase.execute(command);
        
        // Assert
        assertTrue(result.isSuccess());
    }
}
```

#### Testes de Saga

```bash
mvn test -Dtest=OrderSagaOrchestratorTest
```

**Testes Existentes:**
- ✅ `OrderSagaOrchestratorTest.java` - Orquestração completa da saga

**Exemplo de Teste:**
```java
@Test
void shouldExecuteCompleteSaga() {
    // Arrange
    OrderSagaCommand command = createSagaCommand();
    
    // Act
    OrderSagaResult result = orchestrator.execute(command);
    
    // Assert
    assertTrue(result.isSuccess());
    assertEquals(3, result.getSteps().size());
    verify(createOrderUseCase).execute(any());
    verify(processPaymentUseCase).execute(any());
    verify(analyzeRiskUseCase).execute(any());
}
```

**O que testar:**
- ✅ Execução sequencial dos steps
- ✅ Compensação em caso de falha
- ✅ Rastreamento de estado
- ✅ Persistência de histórico

#### Testes de Payment Gateway

```bash
mvn test -Dtest=AbacatePayAdapterTest
```

**Testes Existentes:**
- ✅ `AbacatePayAdapterTest.java` - Integração com AbacatePay

**Exemplo de Teste:**
```java
@Test
void shouldProcessPayment() {
    // Arrange
    PaymentRequest request = createPaymentRequest();
    AbacatePayBillingResponse response = createSuccessResponse();
    
    // Act
    when(webClient.post()).thenReturn(responseSpec);
    PaymentResult result = adapter.processPayment(request);
    
    // Assert
    assertTrue(result.isSuccess());
    assertEquals("PAY-123", result.getPaymentId());
}
```

**O que testar:**
- ✅ Conversão DTO → Domain
- ✅ Chamada HTTP correta
- ✅ Tratamento de erros (401, 500, timeout)
- ✅ Circuit Breaker e Fallback
- ✅ Retry em falhas transitórias

#### Testes de AI Integration

```bash
mvn test -Dtest=OpenAIRiskAnalysisAdapterTest
```

**Testes Existentes:**
- ✅ `OpenAIRiskAnalysisAdapterTest.java` - Integração com OpenAI

**O que testar:**
- ✅ Construção de prompt estruturado
- ✅ Parsing de resposta (LOW/HIGH)
- ✅ Tratamento de erros (401, 500, timeout)
- ✅ Fallback gracioso (retorna PENDING)
- ✅ Circuit Breaker

---

### Estrutura de Testes

```
backend/src/test/java/com/marcelo/orchestrator/
├── domain/
│   └── model/
│       ├── OrderTest.java              # Testes de entidade de domínio
│       ├── OrderStatusTest.java         # Testes de State Machine
│       └── MoneyTest.java               # Testes de Value Object
│
├── application/
│   ├── usecase/
│   │   └── AnalyzeRiskUseCaseTest.java # Testes de use case
│   └── saga/
│       └── OrderSagaOrchestratorTest.java # Testes de saga
│
└── infrastructure/
    ├── persistence/
    │   └── adapter/
    │       └── OrderRepositoryAdapterTest.java # Testes de persistência
    ├── payment/
    │   └── adapter/
    │       └── AbacatePayAdapterTest.java # Testes de gateway de pagamento
    └── ai/
        └── adapter/
            └── OpenAIRiskAnalysisAdapterTest.java # Testes de integração com IA
```

**Convenções de Nomenclatura:**
- **Testes Unitários**: `*Test.java` (ex: `OrderTest.java`)
- **Testes de Integração**: `*IntegrationTest.java` ou `*AdapterTest.java`
- **Testes E2E**: `*E2ETest.java` (futuro)

---

### Tipos de Testes

#### 1. Testes Unitários

**O que são:** Testam uma unidade isolada (classe, método) sem dependências externas.

**Características:**
- Rápidos (milissegundos)
- Isolados (mocks de dependências)
- Sem banco de dados
- Sem chamadas HTTP reais

**Onde estão:**
- `backend/src/test/java/com/marcelo/orchestrator/domain/model/` - Testes de domínio
- `backend/src/test/java/com/marcelo/orchestrator/application/usecase/` - Testes de use cases
- `backend/src/test/java/com/marcelo/orchestrator/infrastructure/*/adapter/` - Testes de adapters

**Exemplo:**
```java
@ExtendWith(MockitoExtension.class)
class OrderTest {
    @Test
    void shouldCalculateTotalCorrectly() {
        // Arrange, Act, Assert
    }
}
```

#### 2. Testes de Integração

**O que são:** Testam integração entre componentes (ex: Repository + Database, Adapter + HTTP).

**Características:**
- Mais lentos (segundos)
- Usam dependências reais (H2, WebClient mockado)
- Testam fluxo completo entre camadas

**Onde estão:**
- `backend/src/test/java/com/marcelo/orchestrator/infrastructure/persistence/adapter/` - Testes de persistência
- `backend/src/test/java/com/marcelo/orchestrator/infrastructure/payment/adapter/` - Testes de integração com gateway
- `backend/src/test/java/com/marcelo/orchestrator/infrastructure/ai/adapter/` - Testes de integração com IA

**Exemplo:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderRepositoryAdapterIntegrationTest {
    @Autowired
    private OrderRepositoryAdapter adapter;
    
    @Test
    void shouldSaveAndRetrieveOrder() {
        // Testa persistência real com H2
    }
}
```

#### 3. Testes End-to-End (E2E)

**O que são:** Testam o fluxo completo da aplicação, do endpoint REST até o banco de dados.

**Características:**
- Mais lentos (segundos a minutos)
- Testam API REST completa
- Usam banco de dados real (H2 ou PostgreSQL via TestContainers)
- Testam validações, exceções, etc.

**Status:** ⚠️ A ser implementado

**Exemplo Futuro:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class OrderControllerE2ETest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldCreateOrderEndToEnd() {
        // Testa POST /api/v1/orders completo
    }
}
```

---

### Configuração de Ambiente de Teste

#### Perfil de Teste (application-test.yml)

O Spring Boot usa automaticamente o perfil `test` durante testes, que pode ser configurado em `src/test/resources/application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
```

#### H2 Database (In-Memory)

**Vantagens:**
- Não precisa de Docker
- Rápido para testes
- Isolado (cada teste tem seu próprio banco)

**Configuração Automática:**
- Spring Boot detecta H2 no classpath
- Cria banco in-memory automaticamente
- Limpa dados entre testes

#### Variáveis de Ambiente para Testes

**Não são necessárias** para testes unitários (usam mocks).

**Para testes de integração:**
```bash
export ABACATEPAY_API_KEY=test_key
export OPENAI_API_KEY=test_key
```

**Ou via application-test.yml:**
```yaml
abacatepay:
  api:
    key: test_key

openai:
  api:
    key: test_key
```

---

### CI/CD e Testes Automatizados

#### GitHub Actions

O projeto possui workflow CI/CD que executa testes automaticamente:

**Arquivo:** `.github/workflows/ci.yml`

**O que faz:**
1. Checkout do código
2. Setup Java 21
3. Compilação (`mvn clean compile`)
4. Execução de testes (`mvn test`)

**Como funciona:**
- Executa em cada `push` e `pull request`
- Falha se algum teste falhar
- Cache de dependências Maven para performance

**Verificar Status:**
- Acesse: https://github.com/seu-usuario/smart-order-orchestrator/actions

#### Executar Testes Localmente (Como no CI)

```bash
# Compilar
mvn clean compile -DskipTests

# Executar testes
mvn test
```

---

### Boas Práticas de Testes

#### 1. Nomenclatura de Testes

**Use:** `should[ExpectedBehavior]When[StateUnderTest]`

**Exemplo:**
```java
@Test
@DisplayName("Deve calcular total corretamente quando pedido tem múltiplos itens")
void shouldCalculateTotalCorrectlyWhenOrderHasMultipleItems() {
    // ...
}
```

#### 2. Estrutura AAA (Arrange-Act-Assert)

```java
@Test
void shouldCalculateTotalCorrectly() {
    // Arrange: Preparar dados de teste
    Order order = Order.builder()
        .items(List.of(item1, item2))
        .build();
    
    // Act: Executar ação a ser testada
    order.calculateTotal();
    
    // Assert: Verificar resultado
    assertEquals(BigDecimal.valueOf(46.00), order.getTotalAmount());
}
```

#### 3. Testes Isolados

- Cada teste deve ser independente
- Não compartilhar estado entre testes
- Usar `@BeforeEach` para setup comum

#### 4. Mocks Apropriados

- **Mock:** Dependências externas (HTTP, Database)
- **Não Mock:** Objetos de domínio (Value Objects, Entities)

#### 5. Cobertura de Testes

**Foco em:**
- Regras de negócio (Domain)
- Orquestração (Use Cases)
- Integrações críticas (Adapters)

**Não precisa testar:**
- Getters/Setters (Lombok)
- Mappers simples (métodos de conversão direta sem lógica complexa)
- DTOs simples

#### 6. Testes Rápidos

- Testes unitários: < 100ms cada
- Testes de integração: < 1s cada
- Suite completa: < 30s

---

## ✅ Checklist Completo de Validação

### Pré-Teste

- [ ] Docker rodando
- [ ] PostgreSQL conectado
- [ ] Backend rodando (porta 8080)
- [ ] Health check respondendo `UP`
- [ ] Swagger acessível
- [ ] Variáveis de ambiente configuradas
- [ ] Bruno configurado com `baseUrl`

### Testes Básicos

- [ ] Teste 1: Health Check ✅
- [ ] Teste 2: Criar Pedido com Sucesso ✅
- [ ] Teste 3: Testar Idempotência ✅
- [ ] Teste 4: Consultar Pedido ✅
- [ ] Teste 5: Listar Todos os Pedidos ✅
- [ ] Teste 6: Buscar por Número ✅
- [ ] Teste 7: Buscar por Status ✅

### Testes AbacatePay

- [ ] Teste 8: Pagamento Bem-Sucedido (Modo Teste) ✅
- [ ] Teste 9: Pagamento Falhado ❌
- [ ] Verificar `payment_id` persistido
- [ ] Verificar conversão centavos → reais
- [ ] Verificar logs de request/response

### Testes Saga Pattern

- [ ] Teste 10: Fluxo Completo com Sucesso ✅
- [ ] Teste 11: Falha no Pagamento (Compensação) ❌
- [ ] Teste 12: Falha na Análise de Risco ⚠️
- [ ] Verificar status `PAYMENT_FAILED` mantido
- [ ] Verificar saga steps no banco

### Testes Circuit Breaker

- [ ] Teste 13: Circuit Breaker Fechado ✅
- [ ] Teste 14: Circuit Breaker Abrindo ⚠️
- [ ] Teste 15: Circuit Breaker Half-Open 🔄
- [ ] Verificar métricas do Circuit Breaker
- [ ] Verificar fallback funcionando

### Testes Automatizados

- [ ] Teste 16: Executar Todos os Testes ✅
- [ ] Teste 17: Executar Testes de Integração ✅
- [ ] Teste 18: Executar Testes por Camada ✅
- [ ] Testes de Domínio (OrderTest, OrderStatusTest, MoneyTest)
- [ ] Testes de Use Cases (AnalyzeRiskUseCaseTest)
- [ ] Testes de Saga (OrderSagaOrchestratorTest)
- [ ] Testes de Payment Gateway (AbacatePayAdapterTest)
- [ ] Testes de AI Integration (OpenAIRiskAnalysisAdapterTest)

### Validações no Banco de Dados

- [ ] Tabela `orders` com dados corretos
- [ ] Tabela `order_items` com dados corretos
- [ ] Tabela `saga_executions` com execuções rastreadas
- [ ] Tabela `saga_steps` com steps detalhados
- [ ] Idempotência funcionando (sem duplicações)

### Observabilidade

- [ ] Health check funcionando
- [ ] Métricas do Circuit Breaker expostas
- [ ] Métricas de Retry expostas
- [ ] Logs estruturados no console
- [ ] Queries SQL retornando dados corretos

---

## 🐛 Troubleshooting

### Problema 1: Aplicação não sobe

**Erro:** `Port 8080 already in use`

**Solução:**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### Problema 2: Erro de conexão com banco

**Erro:** `Connection refused` ou `Connection timeout`

**Solução:**
1. Verificar se Docker está rodando: `docker ps`
2. Verificar se PostgreSQL está acessível: `docker exec -it <container> psql -U postgres`
3. Verificar variáveis de ambiente: `echo $DATABASE_URL`

### Problema 3: AbacatePay retorna 401 Unauthorized

**Erro:** `401 Unauthorized` ao criar pedido

**Solução:**
1. Verificar se `ABACATEPAY_API_KEY` está configurada
2. Verificar se a chave é válida (teste no painel do AbacatePay)
3. Verificar se está usando chave de **teste** (não produção)
4. Reiniciar backend após configurar variável

### Problema 4: Erro 400 Bad Request

**Erro:** `Validation failed`

**Solução:**
- Verificar se todos os campos obrigatórios estão preenchidos
- Verificar formato do email
- Verificar se `customerId` é UUID válido
- Verificar se `items` não está vazio
- Verificar se `unitPrice` é número positivo

### Problema 5: Erro 500 Internal Server Error

**Erro:** `Internal server error`

**Solução:**
1. Verificar logs da aplicação no console
2. Verificar se variáveis de ambiente estão configuradas
3. Verificar se banco de dados está acessível
4. Verificar se AbacatePay/OpenAI estão configurados (ou usar mock)

### Problema 6: Circuit Breaker não abre

**Erro:** Circuit Breaker permanece `CLOSED` mesmo com falhas

**Solução:**
1. Verificar configuração em `application.yml`:
   ```yaml
   resilience4j:
     circuitbreaker:
       instances:
         paymentGateway:
           slidingWindowSize: 10
           minimumNumberOfCalls: 5
           failureRateThreshold: 50
   ```
2. Garantir que há pelo menos 5 chamadas antes de avaliar
3. Garantir que 50%+ das chamadas falharam

---

## 📝 Scripts Úteis

### Script para Teste Completo (Linux/Mac)

```bash
#!/bin/bash

echo "🧪 Teste Completo - Smart Order Orchestrator"
echo "============================================"

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

### Script para Simular Falhas (Linux/Mac)

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

## 📚 Referências

### Documentação do Projeto

- [Frontend - Testes de Jornada](FRONTEND-TESTES-JORNADA-INTEGRACAO.md) - Testes específicos do frontend (componentes, E2E)
- [Integração AbacatePay](README-ABACATEPAY.md) - Documentação da integração
- [Fase 1: Fundação e Estrutura](fases/FASE1-FUNDACAO-ESTRUTURA.md) - Configuração inicial
- [Fase 2: Camada Domain](fases/FASE2-CAMADA-DOMAIN.md) - Modelos de domínio
- [Fase 3: Camada Application](fases/FASE3-CAMADA-APPLICATION.md) - Use cases
- [Fase 4: Infrastructure - Persistência](fases/FASE4-CAMADA-INFRASTRUCTURE-PERSISTENCIA.md) - JPA e Repositories
- [Fase 7: Saga Pattern](fases/FASE7-SAGA-PATTERN.md) - Testes de orquestração

### Documentação Externa

- [AbacatePay Docs](https://docs.abacatepay.com/pages/introduction)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Resilience4j Documentation](https://resilience4j.readme.io/)

---

---

## 🎯 Checklist de Cobertura de Testes

### Testes Unitários
- [x] Domain Models (`OrderTest`, `OrderStatusTest`, `MoneyTest`)
- [x] Use Cases (`AnalyzeRiskUseCaseTest`)
- [x] Saga Orchestrator (`OrderSagaOrchestratorTest`)
- [x] Adapters (`AbacatePayAdapterTest`, `OpenAIRiskAnalysisAdapterTest`, `OrderRepositoryAdapterTest`)

### Testes de Integração
- [x] Persistence Adapter (`OrderRepositoryAdapterTest`)
- [x] Payment Gateway Adapter (`AbacatePayAdapterTest`)
- [x] AI Adapter (`OpenAIRiskAnalysisAdapterTest`)

### Testes End-to-End
- [ ] REST API Controllers (a ser implementado)
- [ ] Fluxo completo: Criar → Pagar → Analisar Risco

### Testes de Performance
- [ ] Carga com Virtual Threads
- [ ] Circuit Breaker sob carga
- [ ] Saga Pattern com múltiplas execuções simultâneas

---

## 🚀 Próximos Passos

1. **Implementar Testes E2E:**
   - `OrderControllerE2ETest` - Testes completos da API REST
   - Usar `@SpringBootTest` com `MockMvc`

2. **Adicionar Cobertura de Código:**
   - Configurar JaCoCo
   - Meta: > 80% de cobertura

3. **Testes de Performance:**
   - JMeter ou Gatling
   - Validar Virtual Threads sob carga

4. **Testes de Contrato:**
   - Pact ou Spring Cloud Contract
   - Validar contratos com APIs externas

---

**📅 Documento criado em:** Dezembro 2024  
**🔄 Última atualização:** Dezembro 2024  
**👨‍💻 Mantido por:** Marcelo Hernandes da Silva

