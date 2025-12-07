# Guia Completo de Teste do Backend - Bruno API Client

> **🎯 Objetivo:** Testar o backend completo passo a passo usando Bruno para entender cada etapa do processo de orquestração de pedidos.

---

## 📋 Índice

1. [Pré-requisitos](#pré-requisitos)
2. [Passo 1: Verificar Banco de Dados](#passo-1-verificar-banco-de-dados)
3. [Passo 2: Executar Migrations](#passo-2-executar-migrations)
4. [Passo 3: Configurar Variáveis de Ambiente](#passo-3-configurar-variáveis-de-ambiente)
5. [Passo 4: Subir a Aplicação](#passo-4-subir-a-aplicação)
6. [Passo 5: Configurar Bruno](#passo-5-configurar-bruno)
7. [Cenários de Teste](#cenários-de-teste)
   - [Cenário 1: Pedido com Sucesso Completo](#cenário-1-pedido-com-sucesso-completo)
   - [Cenário 2: Testar Idempotência](#cenário-2-testar-idempotência)
   - [Cenário 3: Consultar Pedido Criado](#cenário-3-consultar-pedido-criado)
   - [Cenário 4: Listar Todos os Pedidos](#cenário-4-listar-todos-os-pedidos)
   - [Cenário 5: Buscar por Número do Pedido](#cenário-5-buscar-por-número-do-pedido)
   - [Cenário 6: Verificar Observabilidade (Saga)](#cenário-6-verificar-observabilidade-saga)
8. [Verificações no Banco de Dados](#verificações-no-banco-de-dados)
9. [Troubleshooting](#troubleshooting)

---

## ✅ Pré-requisitos

- ✅ Docker rodando (banco PostgreSQL já subido)
- ✅ Java 21 instalado
- ✅ Maven instalado
- ✅ Bruno API Client instalado ([Download Bruno](https://www.usebruno.com/))
- ✅ Variáveis de ambiente configuradas (AbacatePay e OpenAI - opcionais para testes básicos)

---

## 📍 Passo 1: Verificar Banco de Dados

### 1.1. Verificar se o PostgreSQL está rodando

```bash
# Verificar containers Docker
docker ps

# Você deve ver algo como:
# CONTAINER ID   IMAGE              PORTS                    NAMES
# abc123def456   postgres:15        0.0.0.0:5432->5432/tcp   smartorder-postgres
```

### 1.2. Conectar no banco para verificar

```bash
# Conectar no PostgreSQL
docker exec -it smartorder-postgres psql -U postgres -d smartorder

# Ou se o container tiver outro nome:
docker exec -it <nome-do-container> psql -U postgres -d smartorder
```

### 1.3. Verificar se as tabelas já existem

```sql
-- Listar todas as tabelas
\dt

-- Se não houver tabelas, está tudo certo - as migrations vão criar
-- Se já houver tabelas, verificar se estão corretas:
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public';
```

**Tabelas esperadas (após migrations):**
- `orders`
- `order_items`
- `saga_executions`
- `saga_steps`

---

## 🔄 Passo 2: Executar Migrations

### 2.1. As migrations são executadas automaticamente

**Boa notícia:** O Flyway executa as migrations automaticamente quando a aplicação sobe! 

**Mas se quiser executar manualmente ou verificar:**

```bash
# Navegar para o diretório do backend
cd backend

# Verificar status das migrations (após subir a aplicação)
# As migrations estão em: src/main/resources/db/migration/
```

### 2.2. Migrations que serão executadas

1. **V1__create_orders_table.sql** - Cria tabelas `orders` e `order_items`
2. **V2__create_saga_tables.sql** - Cria tabelas `saga_executions` e `saga_steps`
3. **V3__add_idempotency_key_to_saga.sql** - Adiciona campo `idempotency_key` para idempotência

**As migrations serão executadas automaticamente na primeira execução da aplicação!**

---

## ⚙️ Passo 3: Configurar Variáveis de Ambiente

### 3.1. Variáveis Obrigatórias (Mínimas)

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

### 3.2. Variáveis Opcionais (Para Integrações Externas)

**Para testar com AbacatePay e OpenAI (opcional):**

```bash
# AbacatePay (Gateway de Pagamento)
$env:ABACATEPAY_API_KEY="sua-chave-aqui"
$env:ABACATEPAY_BASE_URL="https://api.abacatepay.com/v1"

# OpenAI (Análise de Risco)
$env:OPENAI_API_KEY="sua-chave-aqui"
$env:OPENAI_MODEL="gpt-3.5-turbo"
```

**⚠️ Nota:** Se não configurar essas variáveis, o sistema ainda funcionará, mas:
- Pagamento será simulado (mock)
- Análise de risco será simulada (mock)

**Para testes básicos, você pode deixar sem essas variáveis!**

---

## 🚀 Passo 4: Subir a Aplicação

### 4.1. Navegar para o diretório do backend

```bash
cd backend
```

### 4.2. Executar a aplicação

```bash
# Opção 1: Maven Spring Boot
mvn spring-boot:run

# Opção 2: Compilar e executar JAR
mvn clean package
java -jar target/orchestrator-0.0.1-SNAPSHOT.jar
```

### 4.3. Verificar se a aplicação subiu corretamente

**Aguardar mensagem no console:**
```
Started OrchestratorApplication in X.XXX seconds
```

**Verificar logs:**
- ✅ Deve aparecer: "Flyway migration successful"
- ✅ Deve aparecer: "Started OrchestratorApplication"
- ✅ Porta 8080 deve estar disponível

### 4.4. Testar Health Check

**No navegador ou Bruno:**
```
GET http://localhost:8080/actuator/health
```

**Resposta esperada:**
```json
{
  "status": "UP"
}
```

### 4.5. Verificar Swagger (Documentação da API)

**Acessar no navegador:**
```
http://localhost:8080/swagger-ui.html
```

**Você deve ver a documentação completa da API!**

---

## 🎨 Passo 5: Configurar Bruno

### 5.1. Criar Nova Collection no Bruno

1. Abrir Bruno
2. Criar nova collection: `Smart Order Orchestrator`
3. Criar ambiente: `Local Development`
4. Configurar variável base URL:
   ```
   baseUrl = http://localhost:8080
   ```

### 5.2. Estrutura de Pastas no Bruno

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

## 🧪 Cenários de Teste

### 📝 Cenário 1: Pedido com Sucesso Completo

**Objetivo:** Criar um pedido completo e verificar toda a saga sendo executada.

#### 1.1. Criar Requisição no Bruno

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

#### 1.2. Executar Requisição

**Clique em "Send" no Bruno**

#### 1.3. O que Observar

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

**📊 O que Aconteceu Internamente:**

1. ✅ **Idempotency Check** - Verificou se já existe saga com essa chave
2. ✅ **Step 1: Create Order** - Criou pedido com status `PENDING`
3. ✅ **Step 2: Process Payment** - Processou pagamento (AbacatePay ou mock)
4. ✅ **Step 3: Risk Analysis** - Analisou risco (OpenAI ou mock)
5. ✅ **Event Publishing** - Publicou eventos (OrderCreated, PaymentProcessed, SagaCompleted)

**📝 Verificar Logs no Console:**

Você deve ver logs como:
```
Creating order for customer: 550e8400-e29b-41d4-a716-446655440000
Executing saga with idempotency key: test-idempotency-key-001
Step 1: Creating order...
Step 2: Processing payment...
Step 3: Analyzing risk...
Saga completed successfully
```

**💾 Salvar o `orderId` e `sagaExecutionId` para próximos testes!**

---

### 🔁 Cenário 2: Testar Idempotência

**Objetivo:** Verificar que requisições duplicadas não criam pedidos duplicados.

#### 2.1. Reenviar a Mesma Requisição

**Usar EXATAMENTE o mesmo `idempotencyKey` do Cenário 1:**
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

#### 2.2. Executar Requisição

**Clique em "Send" novamente**

#### 2.3. O que Observar

**✅ Resposta Esperada (HTTP 202 Accepted):**
```json
{
  "success": false,
  "order": null,
  "sagaExecutionId": "uuid-da-execucao-saga-anterior",
  "errorMessage": "Order creation is already in progress"
}
```

**📊 O que Aconteceu:**

- ✅ Sistema detectou que já existe saga com essa `idempotencyKey`
- ✅ Retornou HTTP 202 (Accepted) em vez de criar novo pedido
- ✅ **Zero duplicação!** - Idempotência funcionando!

**💡 Teste Adicional:**

Tente criar pedido **SEM** `idempotencyKey`:
```json
{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "Maria Santos",
  "customerEmail": "maria.santos@email.com",
  "items": [
    {
      "productId": "880e8400-e29b-41d4-a716-446655440003",
      "productName": "Teclado Mecânico",
      "quantity": 1,
      "unitPrice": 450.00
    }
  ],
  "paymentMethod": "DEBIT_CARD",
  "currency": "BRL"
  // ← Sem idempotencyKey - será gerado automaticamente
}
```

**Resultado:** Deve criar novo pedido normalmente (cada execução gera UUID único).

---

### 🔍 Cenário 3: Consultar Pedido Criado

**Objetivo:** Buscar um pedido específico pelo ID.

#### 3.1. Criar Requisição no Bruno

**Método:** `GET`  
**URL:** `{{baseUrl}}/api/v1/orders/{{orderId}}`  
**Headers:** (nenhum necessário)

**💡 Substituir `{{orderId}}` pelo ID do pedido criado no Cenário 1**

#### 3.2. Executar Requisição

**Clique em "Send"**

#### 3.3. O que Observar

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

**❌ Se pedido não existir (HTTP 404):**
```
404 Not Found
```

---

### 📋 Cenário 4: Listar Todos os Pedidos

**Objetivo:** Ver todos os pedidos criados.

#### 4.1. Criar Requisição no Bruno

**Método:** `GET`  
**URL:** `{{baseUrl}}/api/v1/orders`  
**Headers:** (nenhum necessário)

#### 4.2. Executar Requisição

**Clique em "Send"**

#### 4.3. O que Observar

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

**📊 Você deve ver todos os pedidos criados nos testes anteriores!**

---

### 🔢 Cenário 5: Buscar por Número do Pedido

**Objetivo:** Buscar pedido pelo número (ex: ORD-1234567890).

#### 5.1. Criar Requisição no Bruno

**Método:** `GET`  
**URL:** `{{baseUrl}}/api/v1/orders/number/ORD-1234567890`  
**Headers:** (nenhum necessário)

**💡 Substituir `ORD-1234567890` pelo número do pedido criado no Cenário 1**

#### 5.2. Executar Requisição

**Clique em "Send"**

#### 5.3. O que Observar

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

### 📊 Cenário 6: Verificar Observabilidade (Saga)

**Objetivo:** Verificar rastreamento completo da saga no banco de dados.

#### 6.1. Conectar no Banco de Dados

```bash
docker exec -it smartorder-postgres psql -U postgres -d smartorder
```

#### 6.2. Consultar Execuções de Saga

```sql
-- Ver todas as execuções de saga
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

**📊 O que Observar:**

- ✅ Cada execução tem `id`, `order_id`, `status`
- ✅ `idempotency_key` está preenchido
- ✅ `duration_ms` mostra tempo de execução
- ✅ `status` pode ser: `IN_PROGRESS`, `COMPLETED`, `COMPENSATED`, `FAILED`

#### 6.3. Consultar Passos da Saga

```sql
-- Ver passos detalhados de uma saga específica
SELECT 
    step_name,
    status,
    started_at,
    completed_at,
    duration_ms,
    error_message
FROM saga_steps
WHERE saga_execution_id = 'uuid-da-saga'  -- Substituir pelo ID da saga
ORDER BY started_at;
```

**📊 O que Observar:**

- ✅ Deve ter 3 steps: `CREATE_ORDER`, `PROCESS_PAYMENT`, `ANALYZE_RISK`
- ✅ Cada step tem `started_at`, `completed_at`, `duration_ms`
- ✅ `status` de cada step: `SUCCESS` ou `FAILED`

#### 6.4. Consultar Pedidos Criados

```sql
-- Ver todos os pedidos
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

#### 6.5. Consultar Itens dos Pedidos

```sql
-- Ver itens de um pedido específico
SELECT 
    oi.product_name,
    oi.quantity,
    oi.unit_price,
    (oi.quantity * oi.unit_price) as subtotal
FROM order_items oi
WHERE oi.order_id = 'uuid-do-pedido'  -- Substituir pelo ID do pedido
ORDER BY oi.product_name;
```

---

## 🔍 Verificações no Banco de Dados

### Checklist Completo de Verificação

Após executar os cenários, verificar:

#### ✅ Tabela `orders`

```sql
SELECT COUNT(*) as total_pedidos FROM orders;
-- Deve ter pelo menos 1 pedido (do Cenário 1)
```

#### ✅ Tabela `order_items`

```sql
SELECT COUNT(*) as total_itens FROM order_items;
-- Deve ter pelo menos 2 itens (do Cenário 1)
```

#### ✅ Tabela `saga_executions`

```sql
SELECT COUNT(*) as total_sagas FROM saga_executions;
-- Deve ter pelo menos 1 saga (do Cenário 1)
```

#### ✅ Tabela `saga_steps`

```sql
SELECT COUNT(*) as total_steps FROM saga_steps;
-- Deve ter pelo menos 3 steps por saga (CREATE_ORDER, PROCESS_PAYMENT, ANALYZE_RISK)
```

#### ✅ Verificar Idempotência

```sql
-- Verificar se idempotency_key está único
SELECT idempotency_key, COUNT(*) as count
FROM saga_executions
WHERE idempotency_key IS NOT NULL
GROUP BY idempotency_key
HAVING COUNT(*) > 1;
-- Resultado deve ser vazio (nenhuma duplicação)
```

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

### Problema 4: Erro 400 Bad Request

**Erro:** `Validation failed`

**Solução:**
- Verificar se todos os campos obrigatórios estão preenchidos
- Verificar formato do email
- Verificar se `customerId` é UUID válido
- Verificar se `items` não está vazio

### Problema 5: Erro 500 Internal Server Error

**Erro:** `Internal server error`

**Solução:**
1. Verificar logs da aplicação no console
2. Verificar se variáveis de ambiente estão configuradas
3. Verificar se banco de dados está acessível
4. Verificar se AbacatePay/OpenAI estão configurados (ou usar mock)

---

## 📝 Próximos Passos

Após testar todos os cenários:

1. ✅ **Testar Cenários de Falha:**
   - Pagamento falhando (simular erro no AbacatePay)
   - Análise de risco falhando (simular erro no OpenAI)
   - Verificar compensação automática

2. ✅ **Testar Circuit Breaker:**
   - Fazer múltiplas requisições com serviço externo offline
   - Verificar se sistema continua funcionando

3. ✅ **Testar Event-Driven:**
   - Verificar se eventos estão sendo publicados
   - Verificar logs de eventos

4. ✅ **Testar Performance:**
   - Fazer múltiplas requisições simultâneas
   - Verificar uso de Virtual Threads

---

## 🎓 O que Você Aprendeu

Após completar este guia, você entendeu:

- ✅ Como executar migrations do Flyway
- ✅ Como configurar e subir a aplicação Spring Boot
- ✅ Como criar pedidos via API REST
- ✅ Como funciona a Idempotência na prática
- ✅ Como consultar pedidos criados
- ✅ Como verificar observabilidade no banco de dados
- ✅ Como rastrear execuções de saga
- ✅ Como debugar problemas comuns

---

**📅 Documento criado em:** Dezembro 2024  
**🔄 Última atualização:** Dezembro 2024  
**👨‍💻 Mantido por:** Marcelo Hernandes da Silva

