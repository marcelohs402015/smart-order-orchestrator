# Arquitetura do Smart Order Orchestrator - Descrição para Geração de Diagrama

## 📐 Visão Geral da Arquitetura

**Tipo de Arquitetura:** Hexagonal (Ports and Adapters) + Clean Architecture

**Padrão Principal:** Ports and Adapters (Portas e Adaptadores)

**Estrutura:** 4 camadas principais organizadas em círculos concêntricos, com o domínio no centro e dependências apontando para dentro.

---

## 🎯 Camadas da Arquitetura

### 1. **DOMAIN (Núcleo - Centro do Hexágono)**

**Localização:** `com.marcelo.orchestrator.domain`

**Responsabilidade:** Regras de negócio puras, sem dependências de frameworks ou tecnologias externas.

**Componentes Principais:**

#### Model (Entidades de Domínio)
- **Order**: Entidade principal do pedido
  - Atributos: id, orderNumber, status, customerId, customerName, customerEmail, items, totalAmount, paymentId, riskLevel, createdAt, updatedAt
  - Métodos: updateStatus(), markAsPaid(), markPaymentFailed()
  - Regras de negócio: Validações, transições de estado
  
- **OrderItem**: Value Object para itens do pedido
  - Atributos: productId, productName, quantity, unitPrice
  
- **Money**: Value Object para valores monetários
  - Atributos: amount (BigDecimal), currency (String)
  
- **OrderNumber**: Value Object para número do pedido
  - Formato: "ORD-{UUID}"
  
- **OrderStatus**: Enum com State Machine
  - Estados: PENDING, PAID, PAYMENT_FAILED, CANCELED
  - Transições controladas: PENDING → PAID/PAYMENT_FAILED/CANCELED, PAYMENT_FAILED → CANCELED
  
- **RiskLevel**: Enum
  - Valores: PENDING, LOW, HIGH

#### Ports (Interfaces - Portas de Saída)
- **OrderRepositoryPort**: Interface para persistência
  - Métodos: save(Order), findById(UUID), findAll()
  
- **PaymentGatewayPort**: Interface para gateway de pagamento
  - Métodos: processPayment(PaymentRequest) → PaymentResult
  
- **RiskAnalysisPort**: Interface para análise de risco
  - Métodos: analyzeRisk(RiskAnalysisRequest) → RiskAnalysisResult
  
- **NotificationPort**: Interface para notificações
  - Métodos: sendNotification(Notification)

**Características:**
- ✅ Zero dependências externas
- ✅ Classes puras Java
- ✅ Regras de negócio encapsuladas
- ✅ Testável sem frameworks

---

### 2. **APPLICATION (Casos de Uso - Segundo Círculo)**

**Localização:** `com.marcelo.orchestrator.application`

**Responsabilidade:** Orquestração de casos de uso, coordenação entre domínio e infraestrutura.

**Componentes Principais:**

#### Use Cases (Casos de Uso)
- **CreateOrderUseCase**
  - Input: CreateOrderCommand
  - Output: Order
  - Fluxo: Valida → Cria Order → Salva via OrderRepositoryPort
  
- **ProcessPaymentUseCase**
  - Input: ProcessPaymentCommand
  - Output: Order (atualizado)
  - Fluxo: Busca Order → Chama PaymentGatewayPort → Atualiza status → Salva
  
- **AnalyzeRiskUseCase**
  - Input: AnalyzeRiskCommand
  - Output: Order (com riskLevel atualizado)
  - Fluxo: Busca Order → Chama RiskAnalysisPort → Atualiza riskLevel → Salva
  
- **UpdateOrderStatusUseCase**
  - Input: UpdateOrderStatusCommand
  - Output: Order (atualizado)
  - Fluxo: Busca Order → Atualiza status → Salva

#### Saga Pattern (Orquestração)
- **OrderSagaOrchestrator**
  - Responsabilidade: Orquestrar fluxo completo de criação de pedido
  - Steps:
    1. Criar Pedido (CreateOrderUseCase)
    2. Processar Pagamento (ProcessPaymentUseCase)
    3. Analisar Risco (AnalyzeRiskUseCase) - apenas se pagamento OK
  - Compensação: Se pagamento falhar, cancela pedido
  - Rastreamento: Persiste cada passo em SagaExecutionEntity

**Características:**
- ✅ Depende apenas do Domain
- ✅ Usa Ports (interfaces) do Domain
- ✅ Implementa lógica de orquestração
- ✅ Testável com mocks

---

### 3. **INFRASTRUCTURE (Adaptadores - Terceiro Círculo)**

**Localização:** `com.marcelo.orchestrator.infrastructure`

**Responsabilidade:** Implementações concretas das Portas, integrações com tecnologias externas.

**Componentes Principais:**

#### Persistence Adapter
- **OrderRepositoryAdapter**
  - Implementa: OrderRepositoryPort
  - Usa: JpaOrderRepository (Spring Data JPA)
  - Mapeia: Order (Domain) ↔ OrderEntity (JPA)
  - Tecnologia: PostgreSQL via Spring Data JPA + Hibernate
  
- **OrderEntity**: Entidade JPA
  - Tabela: `orders`
  - Relacionamento: OneToMany com OrderItemEntity
  
- **OrderItemEntity**: Entidade JPA
  - Tabela: `order_items`
  - Relacionamento: ManyToOne com OrderEntity
  
- **SagaExecutionEntity**: Entidade JPA para rastreamento
  - Tabela: `saga_executions`
  - Relacionamento: OneToMany com SagaStepEntity

#### Payment Adapter
- **AbacatePayAdapter**
  - Implementa: PaymentGatewayPort
  - Tecnologia: WebClient (Spring WebFlux) - HTTP reativo
  - Resiliência: Resilience4j (Circuit Breaker + Retry)
  - Endpoint: POST /api/v1/billings (AbacatePay API)
  - Fallback: Retorna PAYMENT_FAILED se API indisponível

#### AI Adapter
- **OpenAIRiskAnalysisAdapter**
  - Implementa: RiskAnalysisPort
  - Tecnologia: WebClient (Spring WebFlux) - HTTP reativo
  - Resiliência: Resilience4j (Circuit Breaker + Retry)
  - Endpoint: POST /v1/chat/completions (OpenAI API)
  - Fallback: Retorna PENDING se API indisponível

#### Configurações
- **PerformanceConfig**: Configuração de Virtual Threads
- **JpaConfig**: Configuração JPA/Hibernate
- **AbacatePayConfig**: Configuração WebClient para AbacatePay
- **OpenAIConfig**: Configuração WebClient para OpenAI

**Características:**
- ✅ Implementa Ports do Domain
- ✅ Isola tecnologias externas
- ✅ Pode ser trocado sem afetar outras camadas
- ✅ Testável com mocks

---

### 4. **PRESENTATION (Interface Externa - Quarto Círculo)**

**Localização:** `com.marcelo.orchestrator.presentation`

**Responsabilidade:** Interface REST, entrada e saída da aplicação.

**Componentes Principais:**

#### Controllers
- **OrderController**
  - Endpoints:
    - POST /api/v1/orders → Criar pedido (usa OrderSagaOrchestrator)
    - GET /api/v1/orders → Listar todos os pedidos
    - GET /api/v1/orders/{id} → Buscar pedido por ID
  - Validação: Bean Validation (@Valid)
  - Documentação: Swagger/OpenAPI

#### DTOs (Data Transfer Objects)
- **CreateOrderRequest**: Input para criar pedido
- **CreateOrderResponse**: Output após criar pedido
- **OrderResponse**: Output para listar/buscar pedido
- **OrderItemRequest/Response**: DTOs para itens

#### Mappers
- **OrderMapper** (MapStruct): Mapeia DTOs ↔ Domain Objects

#### Exception Handling
- **GlobalExceptionHandler**: Tratamento centralizado de exceções
- **ErrorResponse**: DTO padronizado para erros

#### Configurações
- **OpenApiConfig**: Configuração Swagger/OpenAPI

**Características:**
- ✅ Depende apenas do Application
- ✅ Validação de entrada
- ✅ Transformação DTO ↔ Domain
- ✅ Documentação automática

---

## 🔄 Fluxos de Dados Principais

### Fluxo 1: Criar Pedido (Saga Completa)

```
Cliente (Frontend/Postman)
  ↓
POST /api/v1/orders
  ↓
OrderController.createOrder()
  ↓
OrderSagaOrchestrator.execute()
  ↓
Step 1: CreateOrderUseCase
  → OrderRepositoryPort.save() → OrderRepositoryAdapter → PostgreSQL
  ↓
Step 2: ProcessPaymentUseCase
  → PaymentGatewayPort.processPayment() → AbacatePayAdapter → AbacatePay API
  ↓
Step 3: AnalyzeRiskUseCase (se pagamento OK)
  → RiskAnalysisPort.analyzeRisk() → OpenAIRiskAnalysisAdapter → OpenAI API
  ↓
Response: CreateOrderResponse
```

### Fluxo 2: Listar Pedidos

```
Cliente
  ↓
GET /api/v1/orders
  ↓
OrderController.getAllOrders()
  ↓
OrderRepositoryPort.findAll() → OrderRepositoryAdapter → PostgreSQL
  ↓
OrderMapper.toResponse() → OrderResponse[]
  ↓
Response: List<OrderResponse>
```

### Fluxo 3: Compensação (Pagamento Falhou)

```
OrderSagaOrchestrator
  ↓
ProcessPaymentUseCase retorna Order com PAYMENT_FAILED
  ↓
OrderSagaOrchestrator.compensate()
  ↓
Order.updateStatus(CANCELED)
  ↓
OrderRepositoryPort.save() → OrderRepositoryAdapter → PostgreSQL
  ↓
Saga marcada como COMPENSATED
```

---

## 🔌 Integrações Externas

### 1. **AbacatePay (Gateway de Pagamento)**
- **Tipo:** API REST Externa
- **Protocolo:** HTTP/HTTPS
- **Método:** POST /api/v1/billings
- **Resiliência:** Circuit Breaker + Retry (Resilience4j)
- **Fallback:** Retorna PAYMENT_FAILED se API indisponível
- **Adaptador:** AbacatePayAdapter

### 2. **OpenAI (Análise de Risco com IA)**
- **Tipo:** API REST Externa
- **Protocolo:** HTTP/HTTPS
- **Método:** POST /v1/chat/completions
- **Resiliência:** Circuit Breaker + Retry (Resilience4j)
- **Fallback:** Retorna PENDING se API indisponível
- **Adaptador:** OpenAIRiskAnalysisAdapter

### 3. **PostgreSQL (Banco de Dados)**
- **Tipo:** Banco Relacional
- **Protocolo:** JDBC
- **ORM:** Hibernate (JPA)
- **Migrations:** Flyway
- **Adaptador:** OrderRepositoryAdapter

---

## 🎨 Padrões de Design Utilizados

### 1. **Hexagonal Architecture (Ports and Adapters)**
- **Ports:** Interfaces no Domain (OrderRepositoryPort, PaymentGatewayPort, etc.)
- **Adapters:** Implementações na Infrastructure (OrderRepositoryAdapter, AbacatePayAdapter, etc.)

### 2. **Saga Pattern (Orchestration)**
- **Orquestrador:** OrderSagaOrchestrator
- **Steps:** Criar Pedido → Processar Pagamento → Analisar Risco
- **Compensação:** Cancela pedido se pagamento falhar
- **Rastreamento:** Persiste cada passo em SagaExecutionEntity

### 3. **State Machine Pattern**
- **Implementação:** OrderStatus enum
- **Transições:** Controladas via canTransitionTo()
- **Benefício:** Previne estados inválidos

### 4. **Value Objects**
- **Exemplos:** Money, OrderNumber, OrderItem
- **Características:** Imutáveis, validados no construtor

### 5. **Rich Domain Model**
- **Características:** Regras de negócio dentro das entidades
- **Exemplo:** Order.updateStatus() valida transições

### 6. **CQRS (Command Query Responsibility Segregation)**
- **Commands:** CreateOrderCommand, ProcessPaymentCommand, etc.
- **Queries:** Via OrderRepositoryPort.findAll(), findById()

### 7. **Dependency Inversion Principle (SOLID)**
- **Aplicação:** Domain define interfaces (Ports), Infrastructure implementa (Adapters)

---

## 🏗️ Estrutura de Pacotes (Árvore)

```
com.marcelo.orchestrator/
│
├── domain/                          # CAMADA DOMAIN (Núcleo)
│   ├── model/                       # Entidades e Value Objects
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── Money.java
│   │   ├── OrderNumber.java
│   │   ├── OrderStatus.java
│   │   └── RiskLevel.java
│   └── port/                        # Portas (Interfaces)
│       ├── OrderRepositoryPort.java
│       ├── PaymentGatewayPort.java
│       ├── RiskAnalysisPort.java
│       └── NotificationPort.java
│
├── application/                     # CAMADA APPLICATION
│   ├── usecase/                     # Casos de Uso
│   │   ├── CreateOrderUseCase.java
│   │   ├── ProcessPaymentUseCase.java
│   │   ├── AnalyzeRiskUseCase.java
│   │   └── UpdateOrderStatusUseCase.java
│   ├── saga/                        # Saga Pattern
│   │   ├── OrderSagaOrchestrator.java
│   │   ├── OrderSagaCommand.java
│   │   └── OrderSagaResult.java
│   └── exception/                   # Exceções de Domínio
│       ├── DomainException.java
│       └── OrderNotFoundException.java
│
├── infrastructure/                   # CAMADA INFRASTRUCTURE
│   ├── persistence/                 # Adaptador de Persistência
│   │   ├── adapter/
│   │   │   └── OrderRepositoryAdapter.java
│   │   ├── entity/
│   │   │   ├── OrderEntity.java
│   │   │   ├── OrderItemEntity.java
│   │   │   ├── SagaExecutionEntity.java
│   │   │   └── SagaStepEntity.java
│   │   ├── repository/
│   │   │   ├── JpaOrderRepository.java
│   │   │   └── JpaSagaExecutionRepository.java
│   │   └── mapper/
│   │       └── OrderMapper.java
│   ├── payment/                     # Adaptador de Pagamento
│   │   ├── adapter/
│   │   │   └── AbacatePayAdapter.java
│   │   ├── config/
│   │   │   └── AbacatePayConfig.java
│   │   └── dto/
│   │       ├── AbacatePayBillingRequest.java
│   │       └── AbacatePayBillingResponse.java
│   ├── ai/                          # Adaptador de IA
│   │   ├── adapter/
│   │   │   └── OpenAIRiskAnalysisAdapter.java
│   │   ├── config/
│   │   │   └── OpenAIConfig.java
│   │   └── dto/
│   │       ├── OpenAIRequest.java
│   │       └── OpenAIResponse.java
│   └── config/
│       └── PerformanceConfig.java
│
└── presentation/                     # CAMADA PRESENTATION
    ├── controller/
    │   └── OrderController.java
    ├── dto/
    │   ├── CreateOrderRequest.java
    │   ├── CreateOrderResponse.java
    │   ├── OrderResponse.java
    │   └── OrderItemRequest.java
    ├── mapper/
    │   └── OrderMapper.java
    ├── exception/
    │   ├── GlobalExceptionHandler.java
    │   └── ErrorResponse.java
    └── config/
        └── OpenApiConfig.java
```

---

## 🔄 Diagrama de Dependências

**Regra de Dependência:**
- **Domain:** Zero dependências (núcleo)
- **Application:** Depende apenas de Domain
- **Infrastructure:** Depende de Domain (implementa Ports)
- **Presentation:** Depende de Application e Domain

**Direção das Dependências:**
```
Presentation → Application → Domain ← Infrastructure
```

---

## 🎯 Pontos-Chave para o Diagrama

### 1. **Hexágono Central (Domain)**
- Contém: Order, OrderItem, OrderStatus, Ports (interfaces)
- Sem dependências externas
- Círculo mais interno

### 2. **Círculo Application**
- Contém: Use Cases, Saga Orchestrator
- Depende apenas do Domain
- Usa Ports (interfaces) do Domain

### 3. **Círculo Infrastructure**
- Contém: Adapters (implementações das Ports)
- Implementa: OrderRepositoryPort, PaymentGatewayPort, RiskAnalysisPort
- Integrações: PostgreSQL, AbacatePay API, OpenAI API

### 4. **Círculo Presentation**
- Contém: Controllers, DTOs
- Depende de Application
- Interface REST

### 5. **Fluxo Saga**
- OrderSagaOrchestrator orquestra 3 steps
- Cada step usa um Use Case
- Compensação se falhar

### 6. **Resiliência**
- Circuit Breaker em AbacatePayAdapter e OpenAIRiskAnalysisAdapter
- Retry automático
- Fallback strategies

---

## 📊 Tecnologias por Camada

### Domain
- ✅ Java Puro (sem frameworks)

### Application
- ✅ Spring (injeção de dependência)
- ✅ Java Puro (lógica de negócio)

### Infrastructure
- ✅ Spring Data JPA (persistência)
- ✅ Spring WebFlux WebClient (HTTP reativo)
- ✅ Resilience4j (resiliência)
- ✅ PostgreSQL (banco de dados)
- ✅ Flyway (migrations)

### Presentation
- ✅ Spring MVC (REST controllers)
- ✅ Bean Validation (validação)
- ✅ SpringDoc OpenAPI (documentação)
- ✅ MapStruct (mapeamento DTOs)

---

## 🎨 Sugestões para o Diagrama

### Estilo Visual Recomendado:

1. **Hexágono Central (Domain)**
   - Cor: Azul claro
   - Componentes: Order, OrderItem, Ports (interfaces)
   - Sem setas de dependência saindo

2. **Círculo Application**
   - Cor: Verde claro
   - Componentes: Use Cases, Saga Orchestrator
   - Setas apontando para Domain (dependência)

3. **Círculo Infrastructure**
   - Cor: Laranja claro
   - Componentes: Adapters, Entities JPA
   - Setas apontando para Domain (implementa Ports)
   - Setas saindo para serviços externos (AbacatePay, OpenAI, PostgreSQL)

4. **Círculo Presentation**
   - Cor: Roxo claro
   - Componentes: Controllers, DTOs
   - Setas apontando para Application

5. **Serviços Externos**
   - AbacatePay API (retângulo externo)
   - OpenAI API (retângulo externo)
   - PostgreSQL (banco de dados, retângulo externo)

6. **Fluxo Saga**
   - Setas numeradas (1, 2, 3) mostrando os steps
   - Setas de compensação (tracejadas) se falhar

---

## 📝 Instruções para o Gemini

**Prompt Sugerido:**

"Crie um diagrama de arquitetura hexagonal (Ports and Adapters) baseado na descrição acima. O diagrama deve mostrar:

1. Um hexágono central (Domain) com as entidades Order, OrderItem e as interfaces (Ports)
2. Um círculo ao redor (Application) com os Use Cases e o Saga Orchestrator
3. Um círculo externo (Infrastructure) com os Adapters que implementam as Ports
4. Um círculo mais externo (Presentation) com os Controllers REST
5. Serviços externos (AbacatePay, OpenAI, PostgreSQL) conectados aos Adapters
6. Fluxo da Saga mostrando os 3 steps e a compensação
7. Setas mostrando direção das dependências (apontando para dentro, para o núcleo)

Use cores diferentes para cada camada e mantenha o Domain no centro, sem dependências saindo dele."

---

**Documento criado para facilitar a geração de diagramas arquiteturais no Gemini ou outras ferramentas de IA.**

