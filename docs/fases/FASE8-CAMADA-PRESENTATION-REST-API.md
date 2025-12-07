# Fase 8: Camada Presentation (REST API)

## 🎯 Objetivo

Expor endpoints REST para interação com o sistema, implementando validação, tratamento de erros e documentação automática da API.

## ✅ Entregas

### 1. Controllers REST

#### OrderController

**Endpoints Implementados:**
- `POST /api/v1/orders` - Criar pedido (executa saga completa)
- `GET /api/v1/orders/{id}` - Buscar pedido por ID
- `GET /api/v1/orders/number/{orderNumber}` - Buscar pedido por número
- `GET /api/v1/orders` - Listar todos os pedidos

**Características:**
- **Versionamento**: API versionada com prefixo `/api/v1`
- **Validação**: Bean Validation (@Valid) para dados de entrada
- **Documentação**: Swagger/OpenAPI com anotações
- **Tratamento de Erros**: Exception handlers globais

**Por que Controller separado?**
- **Separação de Concerns**: Lógica HTTP separada da lógica de negócio
- **Testabilidade**: Fácil testar com MockMvc
- **Flexibilidade**: Pode adicionar outros formatos (GraphQL, gRPC) sem afetar Application

### 2. DTOs de Request/Response

#### CreateOrderRequest

**Validações:**
- `@NotNull` - Customer ID obrigatório
- `@Email` - Email válido
- `@NotEmpty` - Lista de itens não vazia
- `@NotBlank` - Método de pagamento obrigatório

**Por que DTO separado?**
- **Segurança**: Não expõe estrutura interna do domínio
- **Flexibilidade**: Pode ter campos diferentes da entidade
- **Versionamento**: Pode evoluir independentemente
- **Validação**: Validações específicas para entrada HTTP

#### OrderResponse

**Características:**
- Factory method `from(Order)` para conversão
- Inclui apenas campos necessários para exposição
- Calcula subtotais dos itens

#### CreateOrderResponse

**Características:**
- Retorna resultado da saga (sucesso/falha)
- Inclui ID da execução da saga para rastreamento
- Mensagem de erro se falhou

### 3. Exception Handlers Globais

#### GlobalExceptionHandler

**Exceções Tratadas:**
- `MethodArgumentNotValidException` - Validação falhou (400)
- `OrderNotFoundException` - Pedido não encontrado (404)
- `InvalidOrderStatusException` - Status inválido (400)
- `DomainException` - Erro de domínio (400)
- `IllegalArgumentException` - Argumento inválido (400)
- `IllegalStateException` - Estado inválido (409)
- `Exception` - Erro genérico (500)

**Formato de Erro:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid request data",
  "details": {
    "customerEmail": "Email must be valid"
  }
}
```

**Por que Exception Handler Global?**
- **Centralização**: Tratamento de erros em um único lugar
- **Consistência**: Formato de erro padronizado
- **Segurança**: Não expõe detalhes internos do sistema
- **UX**: Mensagens de erro claras para o cliente

### 4. Mappers

#### OrderMapper

**Responsabilidades:**
- Converte `OrderItemRequest` para `OrderItem` (domínio)
- Converte lista de requests para lista de domínio

**Por que Mapper separado?**
- **Separação de Camadas**: Presentation não conhece domínio diretamente
- **Reutilização**: Pode ser usado em múltiplos controllers
- **Testabilidade**: Fácil testar conversões isoladamente

### 5. Documentação Swagger/OpenAPI

#### OpenApiConfig

**Configuração:**
- Título: "Smart Order Orchestrator API"
- Descrição completa da API
- Informações de contato
- Servidores (dev e prod)

**Acesso:**
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

**Por que Swagger/OpenAPI?**
- **Documentação Automática**: Gera documentação a partir de anotações
- **Teste Interativo**: Permite testar API diretamente do navegador
- **Contrato de API**: Define contrato claro entre cliente e servidor
- **Versionamento**: Suporta múltiplas versões da API

## 🏗️ Arquitetura

### Fluxo de Requisição

```
Cliente HTTP
  ↓
OrderController (Presentation)
  ↓
OrderSagaOrchestrator (Application)
  ↓
Use Cases (Application)
  ↓
Domain (Ports)
  ↓
Infrastructure (Adapters)
```

### Separação de Camadas

1. **Presentation**: Recebe HTTP, valida, converte DTOs
2. **Application**: Orquestra casos de uso
3. **Domain**: Contém regras de negócio
4. **Infrastructure**: Implementa portas

## 📦 Estrutura de Pacotes

```
presentation/
├── controller/
│   └── OrderController.java
├── dto/
│   ├── CreateOrderRequest.java
│   ├── CreateOrderResponse.java
│   ├── OrderItemRequest.java
│   ├── OrderItemResponse.java
│   └── OrderResponse.java
├── exception/
│   ├── ErrorResponse.java
│   └── GlobalExceptionHandler.java
├── mapper/
│   └── OrderMapper.java
└── config/
    └── OpenApiConfig.java
```

## 🔧 Configurações

### application.yml

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
```

### Dependências

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

## 📋 Endpoints da API

### POST /api/v1/orders

**Request:**
```json
{
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "customerName": "João Silva",
  "customerEmail": "joao@example.com",
  "items": [
    {
      "productId": "660e8400-e29b-41d4-a716-446655440000",
      "productName": "Produto A",
      "quantity": 2,
      "unitPrice": 50.00
    }
  ],
  "paymentMethod": "PIX",
  "currency": "BRL"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "order": {
    "id": "770e8400-e29b-41d4-a716-446655440000",
    "orderNumber": "ORD-1234567890",
    "status": "PAID",
    "riskLevel": "LOW",
    "totalAmount": 100.00,
    ...
  },
  "sagaExecutionId": "880e8400-e29b-41d4-a716-446655440000",
  "errorMessage": null
}
```

### GET /api/v1/orders/{id}

**Response (200 OK):**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440000",
  "orderNumber": "ORD-1234567890",
  "status": "PAID",
  "riskLevel": "LOW",
  "totalAmount": 100.00,
  ...
}
```

### GET /api/v1/orders

**Response (200 OK):**
```json
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440000",
    "orderNumber": "ORD-1234567890",
    "status": "PAID",
    ...
  }
]
```

## ✅ Critérios de Conclusão

- [x] Endpoints funcionando
- [x] Documentação completa (Swagger)
- [x] Validações implementadas
- [x] Exception handlers globais
- [x] DTOs separados do domínio
- [x] Versionamento de API (v1)
- [x] Mapeamento entre camadas

## 📚 Próximos Passos

- **Fase 9**: Virtual Threads e Performance (otimização para alta concorrência)
- **Fase 10**: Notificações e Eventos

