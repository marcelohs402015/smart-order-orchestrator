## Roteiro de Teste – Pagamento com AbacatePay e Verificação de Status

> Objetivo: documentar o fluxo completo de criação de order, geração de cobrança no AbacatePay, simulação de pagamento no portal e sincronização de status na nossa aplicação.

---

### 1. Criar Order na Nossa API (Backend)

- **Método:** `POST`  
- **URL:** `http://localhost:8081/api/v1/orders`  
- **Headers:**

```json
{
  "Content-Type": "application/json"
}
```

- **Body (exemplo – cliente João, produto Caixa, valor 13):**

```json
{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "Joao",
  "customerEmail": "joao.da.esquina@example.com",
  "items": [
    {
      "productId": "660e8400-e29b-41d4-a716-446655440002",
      "productName": "Caixa",
      "quantity": 1,
      "unitPrice": 13.00
    }
  ],
  "paymentMethod": "CREDIT_CARD",
  "currency": "BRL",
  "idempotencyKey": "order-joao-caixa-13-v1"
}
```

- **Resposta esperada (pagamento pendente):**

```json
{
  "success": false,
  "order": {
    "id": "0452ecbf-016d-4bc4-9185-30733595e2f8",
    "orderNumber": "ORD-...",
    "status": "PAYMENT_PENDING",
    "paymentId": "bill_FkP5pcNnHfQKCZkJnxWTRXWd"
    // ...
  },
  "sagaExecutionId": "2226404a-505f-4804-b8cc-2615556b6169",
  "errorMessage": "Payment failed"
}
```

- **Anotar:**
  - **orderId:** `order.id` (UUID) → ex: `0452ecbf-016d-4bc4-9185-30733595e2f8`  
  - **paymentId:** `order.paymentId` (ID da cobrança no AbacatePay) → ex: `bill_FkP5pcNnHfQKCZkJnxWTRXWd`

---

### 2. Confirmar que o Pagamento Está Pendente

#### 2.1. Pelo Gateway (status real no AbacatePay) - **ATUALIZA AUTOMATICAMENTE O BANCO** ⚡

- **Método:** `GET`  
- **URL:**  
  `http://localhost:8081/api/v1/payments/{paymentId}/status`

Exemplo:

```text
GET http://localhost:8081/api/v1/payments/bill_FkP5pcNnHfQKCZkJnxWTRXWd/status
```

- **Resposta esperada:**

```json
{
  "paymentId": "bill_FkP5pcNnHfQKCZkJnxWTRXWd",
  "status": "PENDING"
}
```

- **⚠️ Comportamento Importante:**
  - Este endpoint **atualiza automaticamente** o pedido no banco de dados se o status for diferente
  - Se encontrar um pedido com este `paymentId` e o status do gateway for diferente do status atual do pedido, a atualização é feita automaticamente
  - **Não é necessário** chamar o endpoint de refresh separadamente se usar este endpoint
  - Logs informativos são gerados quando há atualização: `"Payment status changed for order {}. Current: {}, New: {}. Updating order."`

#### 2.2. Pelo Pedido na Nossa API

- **Método:** `GET`  
- **URL:**  
  `http://localhost:8081/api/v1/orders/{orderId}`

Exemplo:

```text
GET http://localhost:8081/api/v1/orders/0452ecbf-016d-4bc4-9185-30733595e2f8
```

- **Resposta esperada (trecho):**

```json
{
  "id": "0452ecbf-016d-4bc4-9185-30733595e2f8",
  "orderNumber": "ORD-...",
  "status": "PAYMENT_PENDING",
  "paymentId": "bill_FkP5pcNnHfQKCZkJnxWTRXWd"
}
```

---

### 3. Simular Pagamento no Portal AbacatePay

1. Acessar o portal do AbacatePay.
2. Localizar a cobrança com **ID** igual ao `paymentId` (ex: `bill_FkP5pcNnHfQKCZkJnxWTRXWd`).
3. Abrir detalhes da cobrança.
4. Usar a funcionalidade do portal para **simular pagamento como “Sucesso”** (ou “Cancelado”, se quiser testar falha).

> Resultado esperado no portal: status visual da cobrança muda de **Pendente** para **Sucesso**.

---

### 4. Atualizar Status do Pagamento na Nossa Aplicação

Após marcar o pagamento como pago no portal, sincronizar o estado na nossa API.

- **Método:** `POST`  
- **URL:**  
  `http://localhost:8081/api/v1/payments/orders/{orderId}/refresh-status`

Exemplo:

```text
POST http://localhost:8081/api/v1/payments/orders/0452ecbf-016d-4bc4-9185-30733595e2f8/refresh-status
```

- **Comportamento interno:**
  - A API carrega o `Order` pelo `orderId`.
  - Usa `order.paymentId` para chamar `GET /v1/billing/list` no AbacatePay e filtra pelo `paymentId`.
  - Mapeia o status retornado:
    - `PAID` → `order.status` passa para **`PAID`**.
    - `FAILED` / `CANCELLED` → `order.status` passa para **`PAYMENT_FAILED`**.
    - `PENDING` → mantém **`PAYMENT_PENDING`**.

- **Resposta esperada (sucesso):**

```json
{
  "id": "0452ecbf-016d-4bc4-9185-30733595e2f8",
  "orderNumber": "ORD-...",
  "status": "PAID",
  "paymentId": "bill_FkP5pcNnHfQKCZkJnxWTRXWd",
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "Joao",
  "totalAmount": 13.00,
  "createdAt": "2025-12-15T...",
  "updatedAt": "2025-12-15T..."
}
```

Se o pagamento tiver sido marcado como cancelado/falhou no portal, o campo `status` deve vir como `PAYMENT_FAILED`.

---

### 5. Resumo dos IDs Importantes

- **orderId** (`order.id`):
  - UUID do pedido na nossa base.
  - Usado para:
    - `GET /api/v1/orders/{orderId}`
    - `POST /api/v1/payments/orders/{orderId}/refresh-status`

- **paymentId** (`order.paymentId` / `data.id` no AbacatePay):
  - ID da cobrança no AbacatePay (`bill_xxx`).
  - Usado para:
    - `GET /api/v1/payments/{paymentId}/status` ⚡ **Atualiza banco automaticamente**
    - Visualizar/editar cobrança no portal AbacatePay.

---

### 6. Uso Futuro no Frontend

No frontend, o fluxo esperado para o usuário será:

1. Criar pedido → mostrar `orderNumber`, `status` inicial (`PAYMENT_PENDING`) e talvez um link "Ver cobrança".
2. Exibir botão **"Verificar Status de Pagamento"** que chama:
   - `GET /api/v1/payments/{paymentId}/status` ⚡ **Recomendado** - Atualiza banco automaticamente
   - OU `POST /api/v1/payments/orders/{orderId}/refresh-status` - Alternativa usando orderId
3. Atualizar UI com o novo `status` (`PAID`, `PAYMENT_FAILED`, etc.).

**💡 Dica:** O endpoint `GET /api/v1/payments/{paymentId}/status` é mais eficiente pois:
- Atualiza o banco automaticamente se o status mudou
- Não requer o `orderId`, apenas o `paymentId`
- Retorna o status atualizado imediatamente


