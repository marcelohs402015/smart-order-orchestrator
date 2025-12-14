# Roteiro Completo de Testes - Smart Order Orchestrator

> **📅 Última Atualização:** 12/12/2025  
> **🎯 Objetivo:** Roteiro completo para testar todos os cenários do sistema

---

## 📋 Índice

1. [Pré-requisitos](#pré-requisitos)
2. [Preparação do Ambiente](#preparação-do-ambiente)
3. [Cenários de Teste](#cenários-de-teste)
4. [Validações no Banco de Dados](#validações-no-banco-de-dados)
5. [Troubleshooting](#troubleshooting)

---

## ✅ Pré-requisitos

- ✅ Docker rodando (PostgreSQL)
- ✅ Java 21 instalado
- ✅ Maven instalado
- ✅ Bruno API Client ou Postman instalado
- ✅ Variáveis de ambiente configuradas (opcional para testes básicos)

---

## 🔧 Preparação do Ambiente

### 1. Verificar Banco de Dados

```bash
# Verificar containers Docker
docker ps

# Conectar no PostgreSQL
docker exec -it smartorder-postgres psql -U postgres -d smartorder
```

### 2. Configurar Variáveis de Ambiente

**Mínimas (obrigatórias):**
```bash
# Windows PowerShell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/smartorder"
$env:DATABASE_USERNAME="postgres"
$env:DATABASE_PASSWORD="postgres"
```

**Opcionais (para integrações externas):**
```bash
$env:ABACATEPAY_API_KEY="sua-chave"
$env:OPENAI_API_KEY="sua-chave"
```

### 3. Subir Aplicação

```bash
cd backend
mvn spring-boot:run
```

**Verificar logs:**
- ✅ `Flyway migration successful`
- ✅ `Started OrchestratorApplication`
- ✅ Porta 8080 disponível

### 4. Testar Health Check

```bash
GET http://localhost:8080/actuator/health
```

**Resposta esperada:**
```json
{
  "status": "UP"
}
```

---

## 🧪 Cenários de Teste

### 📝 Cenário 1: Criar Pedido com Sucesso Completo

**Objetivo:** Criar um pedido completo e verificar toda a saga sendo executada.

**Request:**
```http
POST http://localhost:8080/api/orders
Content-Type: application/json
```

**Body:**
```json
{
  "customerId": "6078e5ac-ee78-4a59-ba28-b43f44f4b5fc",
  "customerName": "João Silva",
  "customerEmail": "joao.silva@example.com",
  "items": [
    {
      "productId": "1ac5f43b-7242-4fa2-8334-19ba2f506c49",
      "productName": "Notebook Dell Inspiron 15",
      "quantity": 1,
      "unitPrice": 3299.99
    },
    {
      "productId": "95de81c9-fdc4-4a76-9c71-051fa71ddd15",
      "productName": "Mouse Logitech MX Master 3",
      "quantity": 2,
      "unitPrice": 249.90
    }
  ],
  "paymentMethod": "PIX",
  "currency": "BRL",
  "idempotencyKey": "test-order-001"
}
```

**Resposta Esperada (HTTP 201):**
```json
{
  "success": true,
  "order": {
    "id": "uuid-do-pedido",
    "orderNumber": "ORD-1234567890",
    "status": "PAID",
    "customerId": "6078e5ac-ee78-4a59-ba28-b43f44f4b5fc",
    "customerName": "João Silva",
    "customerEmail": "joao.silva@example.com",
    "items": [
      {
        "productId": "1ac5f43b-7242-4fa2-8334-19ba2f506c49",
        "productName": "Notebook Dell Inspiron 15",
        "quantity": 1,
        "unitPrice": 3299.99
      },
      {
        "productId": "95de81c9-fdc4-4a76-9c71-051fa71ddd15",
        "productName": "Mouse Logitech MX Master 3",
        "quantity": 2,
        "unitPrice": 249.90
      }
    ],
    "totalAmount": 3799.79,
    "paymentId": "payment-id-123",
    "riskLevel": "LOW",
    "createdAt": "2024-12-12T...",
    "updatedAt": "2024-12-12T..."
  },
  "sagaExecutionId": "uuid-da-execucao-saga"
}
```

**O que verificar:**
- ✅ Status HTTP 201 Created
- ✅ `order.status` = `PAID`
- ✅ `order.riskLevel` = `LOW` ou `HIGH` ou `PENDING`
- ✅ `order.paymentId` preenchido
- ✅ `sagaExecutionId` retornado
- ✅ Logs mostram execução completa da saga

**💾 Salvar:**
- `order.id` para próximos testes
- `order.orderNumber` para busca por número
- `sagaExecutionId` para verificação de observabilidade

---

### 🔁 Cenário 2: Testar Idempotência

**Objetivo:** Verificar que requisições duplicadas não criam pedidos duplicados.

**Request:**
```http
POST http://localhost:8080/api/orders
Content-Type: application/json
```

**Body (MESMO `idempotencyKey` do Cenário 1):**
```json
{
  "customerId": "6078e5ac-ee78-4a59-ba28-b43f44f4b5fc",
  "customerName": "João Silva",
  "customerEmail": "joao.silva@example.com",
  "items": [
    {
      "productId": "1ac5f43b-7242-4fa2-8334-19ba2f506c49",
      "productName": "Notebook Dell Inspiron 15",
      "quantity": 1,
      "unitPrice": 3299.99
    }
  ],
  "paymentMethod": "PIX",
  "currency": "BRL",
  "idempotencyKey": "test-order-001"
}
```

**Resposta Esperada (HTTP 202 Accepted):**
```json
{
  "success": false,
  "order": null,
  "sagaExecutionId": "uuid-da-execucao-saga-anterior",
  "errorMessage": "Order creation is already in progress"
}
```

**O que verificar:**
- ✅ Status HTTP 202 Accepted
- ✅ `success` = `false`
- ✅ `order` = `null`
- ✅ `sagaExecutionId` = ID da saga anterior
- ✅ **Zero duplicação** - Idempotência funcionando!

---

### 📝 Cenário 3: Criar Pedido sem IdempotencyKey

**Objetivo:** Verificar que sistema gera hash SHA-256 automaticamente.

**Request:**
```http
POST http://localhost:8080/api/orders
Content-Type: application/json
```

**Body (SEM `idempotencyKey`):**
```json
{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "Maria Santos",
  "customerEmail": "maria.santos@example.com",
  "items": [
    {
      "productId": "660e8400-e29b-41d4-a716-446655440001",
      "productName": "Teclado Mecânico",
      "quantity": 1,
      "unitPrice": 450.00
    }
  ],
  "paymentMethod": "DEBIT_CARD",
  "currency": "BRL"
}
```

**Resposta Esperada (HTTP 201):**
- ✅ Status HTTP 201 Created
- ✅ Pedido criado normalmente
- ✅ Sistema gera `idempotencyKey` automaticamente (hash SHA-256)

**Verificar no banco:**
```sql
SELECT idempotency_key FROM saga_executions ORDER BY started_at DESC LIMIT 1;
-- Deve ter um hash SHA-256 gerado automaticamente
```

---

### 🔍 Cenário 4: Buscar Pedido por ID

**Objetivo:** Buscar um pedido específico pelo ID.

**Request:**
```http
GET http://localhost:8080/api/orders/{orderId}
```

**Substituir `{orderId}` pelo ID do pedido criado no Cenário 1.**

**Resposta Esperada (HTTP 200):**
```json
{
  "id": "uuid-do-pedido",
  "orderNumber": "ORD-1234567890",
  "status": "PAID",
  "customerId": "6078e5ac-ee78-4a59-ba28-b43f44f4b5fc",
  "customerName": "João Silva",
  "customerEmail": "joao.silva@example.com",
  "items": [...],
  "totalAmount": 3799.79,
  "paymentId": "payment-id-123",
  "riskLevel": "LOW",
  "createdAt": "2024-12-12T...",
  "updatedAt": "2024-12-12T..."
}
```

**O que verificar:**
- ✅ Status HTTP 200 OK
- ✅ Dados completos do pedido
- ✅ Itens presentes
- ✅ Status correto

**Teste de erro (pedido não existe):**
```http
GET http://localhost:8080/api/orders/00000000-0000-0000-0000-000000000000
```

**Resposta Esperada (HTTP 404):**
```json
{
  "timestamp": "2024-12-12T...",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found",
  "path": "/api/orders/00000000-0000-0000-0000-000000000000"
}
```

---

### 🔢 Cenário 5: Buscar Pedido por Número

**Objetivo:** Buscar pedido pelo número (ex: ORD-1234567890).

**Request:**
```http
GET http://localhost:8080/api/orders/number/ORD-1234567890
```

**Substituir `ORD-1234567890` pelo número do pedido criado no Cenário 1.**

**Resposta Esperada (HTTP 200):**
- ✅ Status HTTP 200 OK
- ✅ Dados completos do pedido
- ✅ Mesma estrutura do Cenário 4

---

### 📋 Cenário 6: Listar Todos os Pedidos

**Objetivo:** Ver todos os pedidos criados.

**Request:**
```http
GET http://localhost:8080/api/orders
```

**Resposta Esperada (HTTP 200):**
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

**O que verificar:**
- ✅ Status HTTP 200 OK
- ✅ Lista com todos os pedidos criados
- ✅ Dados completos de cada pedido

---

### ⚠️ Cenário 7: Validação de Campos Obrigatórios

**Objetivo:** Verificar validações de entrada.

**Request (sem `customerEmail`):**
```http
POST http://localhost:8080/api/orders
Content-Type: application/json
```

**Body:**
```json
{
  "customerId": "6078e5ac-ee78-4a59-ba28-b43f44f4b5fc",
  "customerName": "João Silva",
  "items": [
    {
      "productId": "1ac5f43b-7242-4fa2-8334-19ba2f506c49",
      "productName": "Notebook",
      "quantity": 1,
      "unitPrice": 3299.99
    }
  ]
}
```

**Resposta Esperada (HTTP 400 Bad Request):**
```json
{
  "timestamp": "2024-12-12T...",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "customerEmail",
      "message": "must not be null"
    }
  ]
}
```

**Testar outros campos:**
- ✅ Sem `customerId` → HTTP 400
- ✅ Sem `customerName` → HTTP 400
- ✅ Sem `items` ou `items` vazio → HTTP 400
- ✅ `items` sem `productId` → HTTP 400
- ✅ `items` com `quantity` <= 0 → HTTP 400
- ✅ `items` com `unitPrice` <= 0 → HTTP 400

---

### 🔄 Cenário 8: Testar Compensação (Falha no Pagamento)

**Objetivo:** Verificar que sistema compensa automaticamente quando pagamento falha.

**Como testar:**
1. Simular falha no AbacatePay (configurar variável de ambiente inválida ou mock)
2. Criar pedido normalmente
3. Verificar que pedido é cancelado automaticamente

**Request:**
```http
POST http://localhost:8080/api/orders
Content-Type: application/json
```

**Body:**
```json
{
  "customerId": "6078e5ac-ee78-4a59-ba28-b43f44f4b5fc",
  "customerName": "João Silva",
  "customerEmail": "joao.silva@example.com",
  "items": [
    {
      "productId": "1ac5f43b-7242-4fa2-8334-19ba2f506c49",
      "productName": "Notebook",
      "quantity": 1,
      "unitPrice": 3299.99
    }
  ],
  "paymentMethod": "PIX",
  "currency": "BRL",
  "idempotencyKey": "test-compensation-001"
}
```

**Resposta Esperada (HTTP 201 ou 500):**
- ✅ Se pagamento falhar, pedido deve ser cancelado
- ✅ Status do pedido = `CANCELED`
- ✅ Logs mostram compensação executada

**Verificar no banco:**
```sql
SELECT status, error_message FROM saga_executions 
WHERE idempotency_key = 'test-compensation-001';
-- status deve ser 'COMPENSATED' ou 'FAILED'
```

---

## 🔍 Validações no Banco de Dados

### 1. Verificar Tabela `orders`

```sql
SELECT 
    id,
    order_number,
    status,
    customer_name,
    total_amount,
    payment_id,
    risk_level,
    created_at
FROM orders
ORDER BY created_at DESC;
```

**O que verificar:**
- ✅ Pedidos criados estão presentes
- ✅ Status correto (`PAID`, `PENDING`, `CANCELED`)
- ✅ `total_amount` calculado corretamente
- ✅ `payment_id` preenchido (se pagamento foi processado)

---

### 2. Verificar Tabela `order_items`

```sql
SELECT 
    oi.id,
    oi.order_id,
    oi.product_name,
    oi.quantity,
    oi.unit_price,
    (oi.quantity * oi.unit_price) as subtotal
FROM order_items oi
JOIN orders o ON oi.order_id = o.id
ORDER BY o.created_at DESC, oi.product_name;
```

**O que verificar:**
- ✅ Itens estão associados aos pedidos corretos
- ✅ Quantidades e preços corretos
- ✅ Subtotal calculado corretamente

---

### 3. Verificar Tabela `saga_executions`

```sql
SELECT 
    id,
    order_id,
    status,
    current_step,
    idempotency_key,
    started_at,
    completed_at,
    duration_ms,
    error_message
FROM saga_executions
ORDER BY started_at DESC;
```

**O que verificar:**
- ✅ Cada pedido tem uma execução de saga
- ✅ `idempotency_key` está preenchido e único
- ✅ `status` correto (`COMPLETED`, `COMPENSATED`, `FAILED`)
- ✅ `duration_ms` mostra tempo de execução
- ✅ `error_message` preenchido apenas se houver erro

---

### 4. Verificar Tabela `saga_steps`

```sql
SELECT 
    ss.step_name,
    ss.status,
    ss.started_at,
    ss.completed_at,
    ss.duration_ms,
    ss.error_message,
    se.idempotency_key
FROM saga_steps ss
JOIN saga_executions se ON ss.saga_execution_id = se.id
ORDER BY se.started_at DESC, ss.started_at;
```

**O que verificar:**
- ✅ Cada saga tem 3 steps: `CREATE_ORDER`, `PROCESS_PAYMENT`, `ANALYZE_RISK`
- ✅ Cada step tem `started_at`, `completed_at`, `duration_ms`
- ✅ `status` de cada step: `SUCCESS` ou `FAILED`
- ✅ `error_message` preenchido apenas se step falhou

---

### 5. Verificar Idempotência

```sql
-- Verificar se idempotency_key está único
SELECT idempotency_key, COUNT(*) as count
FROM saga_executions
WHERE idempotency_key IS NOT NULL
GROUP BY idempotency_key
HAVING COUNT(*) > 1;
```

**Resultado esperado:**
- ✅ **Vazio** - Nenhuma duplicação (idempotência funcionando!)

---

### 6. Verificar Integridade Referencial

```sql
-- Verificar se todos os order_items têm order_id válido
SELECT COUNT(*) as orphan_items
FROM order_items oi
LEFT JOIN orders o ON oi.order_id = o.id
WHERE o.id IS NULL;
```

**Resultado esperado:**
- ✅ **0** - Nenhum item órfão (integridade mantida!)

```sql
-- Verificar se todos os saga_steps têm saga_execution_id válido
SELECT COUNT(*) as orphan_steps
FROM saga_steps ss
LEFT JOIN saga_executions se ON ss.saga_execution_id = se.id
WHERE se.id IS NULL;
```

**Resultado esperado:**
- ✅ **0** - Nenhum step órfão (integridade mantida!)

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

---

### Problema 2: Erro de conexão com banco

**Erro:** `Connection refused` ou `Connection timeout`

**Solução:**
1. Verificar se Docker está rodando: `docker ps`
2. Verificar se PostgreSQL está acessível: `docker exec -it <container> psql -U postgres`
3. Verificar variáveis de ambiente: `echo $DATABASE_URL`

---

### Problema 3: Migrations não executam

**Erro:** `Flyway migration failed`

**Solução:**
1. Verificar se banco existe: `docker exec -it <container> psql -U postgres -l`
2. Verificar se usuário tem permissões
3. Limpar schema e tentar novamente (CUIDADO - apaga dados!):
   ```sql
   DROP SCHEMA public CASCADE;
   CREATE SCHEMA public;
   ```

---

### Problema 4: Erro 400 Bad Request

**Erro:** `Validation failed`

**Solução:**
- Verificar se todos os campos obrigatórios estão preenchidos
- Verificar formato do email
- Verificar se `customerId` é UUID válido
- Verificar se `items` não está vazio
- Verificar se `quantity` e `unitPrice` são > 0

---

### Problema 5: Erro 500 Internal Server Error

**Erro:** `Internal server error`

**Solução:**
1. Verificar logs da aplicação no console
2. Verificar se variáveis de ambiente estão configuradas
3. Verificar se banco de dados está acessível
4. Verificar se AbacatePay/OpenAI estão configurados (ou usar mock)

---

### Problema 6: Pedido não persiste

**Sintomas:** Resposta 201, mas pedido não aparece no banco

**Solução:**
1. Verificar logs da aplicação (erro de persistência)
2. Verificar se transação foi commitada
3. Verificar se há erro de constraint (ex: `idempotency_key` duplicado)
4. Verificar se `OrderEntity.items` está inicializado corretamente

---

## 📊 Checklist de Testes

### Testes Funcionais
- [ ] Criar pedido com sucesso completo
- [ ] Testar idempotência (requisição duplicada)
- [ ] Criar pedido sem `idempotencyKey` (geração automática)
- [ ] Buscar pedido por ID
- [ ] Buscar pedido por número
- [ ] Listar todos os pedidos
- [ ] Validação de campos obrigatórios
- [ ] Testar compensação (falha no pagamento)

### Testes de Integridade
- [ ] Verificar tabela `orders` no banco
- [ ] Verificar tabela `order_items` no banco
- [ ] Verificar tabela `saga_executions` no banco
- [ ] Verificar tabela `saga_steps` no banco
- [ ] Verificar idempotência (chaves únicas)
- [ ] Verificar integridade referencial

### Testes de Observabilidade
- [ ] Verificar logs da aplicação
- [ ] Verificar rastreamento de saga
- [ ] Verificar timestamps e durações
- [ ] Verificar mensagens de erro (quando aplicável)

---

## 🎓 O que Você Aprendeu

Após completar este roteiro, você entendeu:

- ✅ Como criar pedidos via API REST
- ✅ Como funciona a Idempotência na prática
- ✅ Como consultar pedidos criados
- ✅ Como verificar observabilidade no banco de dados
- ✅ Como rastrear execuções de saga
- ✅ Como validar integridade de dados
- ✅ Como debugar problemas comuns

---

**📅 Documento criado em:** 12/12/2025  
**🔄 Última atualização:** 12/12/2025  
**👨‍💻 Mantido por:** Marcelo Hernandes da Silva

