# 🚀 Highlights de Tecnologias - Smart Order Orchestrator

> **📋 Índice Navegável das Tecnologias do Projeto**  
> Links diretos para código e documentação detalhada

---

## 📑 Índice Rápido

- [🎯 Design Patterns e SOLID (Destaque para Recrutamento)](#-design-patterns-e-solid-destaque-para-recrutamento) ⭐
- [Linguagem e Runtime](#-linguagem-e-runtime)
- [Framework e Core](#-framework-e-core)
- [Banco de Dados e Persistência](#-banco-de-dados-e-persistência)
- [Resiliência e Circuit Breaker](#-resiliência-e-circuit-breaker)
- [Arquitetura e Padrões](#-arquitetura-e-padrões)
- [Event-Driven Architecture](#-event-driven-architecture)
- [Performance e Concorrência](#-performance-e-concorrência)
- [IA e MCP Code Review](#-ia-e-mcp-code-review)
- [API e Documentação](#-api-e-documentação)
- [Frontend](#-frontend)
- [Infraestrutura e DevOps](#-infraestrutura-e-devops)

---

## 🎯 Design Patterns e SOLID (Destaque para Recrutamento)

> **⭐ Seção Destacada para Processos Seletivos**  
> Esta seção demonstra conhecimento prático de Design Patterns e Princípios SOLID, requisitos frequentes em processos de recrutamento e seleção.

### 📚 Design Patterns Implementados

#### 1. **Factory Pattern** ⭐

**O que é:** Padrão que encapsula a criação de objetos, centralizando a lógica de instanciação.

**Implementação no Projeto:**
- **EventPublisherFactory:** Cria adapters de eventos baseado em configuração (Kafka, Pub/Sub, RabbitMQ, In-Memory)

**Por que usar:**
- ✅ **Flexibilidade:** Trocar message broker via configuração sem alterar código
- ✅ **Extensibilidade:** Fácil adicionar novos brokers (SQS, SNS, etc.)
- ✅ **Desacoplamento:** Clientes não conhecem qual implementação está sendo usada
- ✅ **Testabilidade:** Fácil usar implementação in-memory em testes

**📁 Código:**
- Factory: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/factory/EventPublisherFactory.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/factory/EventPublisherFactory.java)
- Configuração: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/config/EventPublisherConfig.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/config/EventPublisherConfig.java)

**💡 Exemplo de Uso:**
```java
// Factory cria o adapter correto baseado em configuração
EventPublisherPort publisher = eventPublisherFactory.create();
// publisher pode ser Kafka, Pub/Sub, RabbitMQ, etc. - código não precisa saber qual
```

**📚 Documentação:**
- [Contexto do Projeto - Factory Pattern](CONTEXTO-PROJETO.md#3-factory-pattern) - Explicação detalhada
- [Arquitetura para Diagrama - Padrões](ARQUITETURA-PARA-DIAGRAMA.md#-padrões-de-design-utilizados) - Visão geral

---

#### 2. **Adapter Pattern** ⭐

**O que é:** Padrão que permite que interfaces incompatíveis trabalhem juntas, convertendo uma interface em outra.

**Implementação no Projeto:**
- **OrderRepositoryAdapter:** Adapta OrderRepositoryPort (domínio) para JPA (infraestrutura)
- **AbacatePayAdapter:** Adapta PaymentGatewayPort (domínio) para API REST (infraestrutura)
- **OpenAIRiskAnalysisAdapter:** Adapta RiskAnalysisPort (domínio) para OpenAI API (infraestrutura)
- **KafkaEventPublisherAdapter, PubSubEventPublisherAdapter, etc.:** Adaptam EventPublisherPort para diferentes message brokers

**Por que usar:**
- ✅ **Isolamento:** Domínio não conhece JPA, HTTP, Kafka
- ✅ **Troca de Implementação:** Pode trocar PostgreSQL por MongoDB sem alterar domínio
- ✅ **Testabilidade:** Fácil mockar adapters em testes
- ✅ **Hexagonal Architecture:** Core do padrão Ports and Adapters

**📁 Código:**
- Repository Adapter: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/adapter/OrderRepositoryAdapter.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/adapter/OrderRepositoryAdapter.java)
- Payment Adapter: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/payment/adapter/AbacatePayAdapter.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/payment/adapter/AbacatePayAdapter.java)
- AI Adapter: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/ai/adapter/OpenAIRiskAnalysisAdapter.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/ai/adapter/OpenAIRiskAnalysisAdapter.java)
- Event Adapters: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/)

**💡 Exemplo de Uso:**
```java
// Port (interface no domínio)
public interface OrderRepositoryPort {
    Order save(Order order);
}

// Adapter (implementação na infraestrutura)
@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {
    // Converte Order (domínio) ↔ OrderEntity (JPA)
    // Domínio não conhece JPA!
}
```

**📚 Documentação:**
- [Fase 4: Infrastructure - Persistência](fases/FASE4-CAMADA-INFRASTRUCTURE-PERSISTENCIA.md) - Implementação completa
- [Contexto do Projeto - Adapter Pattern](CONTEXTO-PROJETO.md#2-adapter-pattern-ports-and-adapters) - Explicação detalhada

---

#### 3. **Repository Pattern** ⭐

**O que é:** Padrão que abstrai a lógica de acesso a dados, fornecendo uma interface mais orientada a objetos.

**Implementação no Projeto:**
- **OrderRepositoryPort:** Interface no domínio que define operações de persistência
- **OrderRepositoryAdapter:** Implementação usando JPA/Spring Data

**Por que usar:**
- ✅ **Abstração:** Domínio não conhece detalhes de persistência (SQL, JPA, etc.)
- ✅ **Testabilidade:** Fácil criar mock repository para testes
- ✅ **Flexibilidade:** Pode trocar JPA por MongoDB, Cassandra, etc.
- ✅ **Single Responsibility:** Separa lógica de negócio de acesso a dados

**📁 Código:**
- Port (Interface): [`backend/src/main/java/com/marcelo/orchestrator/domain/port/OrderRepositoryPort.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/port/OrderRepositoryPort.java)
- Adapter (Implementação): [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/adapter/OrderRepositoryAdapter.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/adapter/OrderRepositoryAdapter.java)
- JPA Repository: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/repository/JpaOrderRepository.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/repository/JpaOrderRepository.java)

**📚 Documentação:**
- [Fase 2: Camada Domain](fases/FASE2-CAMADA-DOMAIN.md) - Ports e interfaces
- [Fase 4: Infrastructure - Persistência](fases/FASE4-CAMADA-INFRASTRUCTURE-PERSISTENCIA.md) - Implementação

---

#### 4. **Builder Pattern** ⭐

**O que é:** Padrão que permite construir objetos complexos passo a passo, separando a construção da representação.

**Implementação no Projeto:**
- **Lombok @Builder:** Usado em todas as entidades, DTOs e Value Objects

**Por que usar:**
- ✅ **Legibilidade:** Código mais limpo e expressivo
- ✅ **Imutabilidade:** Facilita criação de objetos imutáveis
- ✅ **Validação:** Pode validar durante construção
- ✅ **Flexibilidade:** Permite construir objetos com diferentes combinações de parâmetros

**📁 Código:**
- Order Entity: [`backend/src/main/java/com/marcelo/orchestrator/domain/model/Order.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/model/Order.java) (linha 54)
- DTOs: [`backend/src/main/java/com/marcelo/orchestrator/presentation/dto/`](../backend/src/main/java/com/marcelo/orchestrator/presentation/dto/)
- Saga Command: [`backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaCommand.java`](../backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaCommand.java)

**💡 Exemplo de Uso:**
```java
Order order = Order.builder()
    .id(UUID.randomUUID())
    .orderNumber("ORD-123")
    .status(OrderStatus.PENDING)
    .customerId(customerId)
    .items(items)
    .build();
```

---

#### 5. **Saga Pattern (Orchestration)** ⭐

**O que é:** Padrão para gerenciar transações distribuídas em microserviços, garantindo consistência eventual através de compensação.

**Implementação no Projeto:**
- **OrderSagaOrchestrator:** Orquestra 3 steps sequenciais (Criar Pedido → Processar Pagamento → Analisar Risco)
- **Compensação Automática:** Rollback em caso de falha

**Por que usar:**
- ✅ **Transações Distribuídas:** Não há ACID em microserviços
- ✅ **Consistência Eventual:** Garantida através de compensação
- ✅ **Observabilidade:** Cada step é rastreado e persistido
- ✅ **Padrão Enterprise:** Usado por Uber, Amazon, Mercado Livre

**📁 Código:**
- Orchestrator: [`backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaOrchestrator.java`](../backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaOrchestrator.java)
- Command: [`backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaCommand.java`](../backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaCommand.java)
- Saga Execution Entity: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/entity/SagaExecutionEntity.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/entity/SagaExecutionEntity.java)

**📚 Documentação:**
- [Fase 7: Saga Pattern](fases/FASE7-SAGA-PATTERN.md) - **Documentação completa** com explicação detalhada
- [Contexto do Projeto - Saga Pattern](CONTEXTO-PROJETO.md#1-saga-pattern-orchestration-completo) - Fluxo e benefícios

---

#### 6. **State Machine Pattern** ⭐

**O que é:** Padrão que modela comportamentos baseados em estados e transições entre estados.

**Implementação no Projeto:**
- **OrderStatus:** Enum com transições controladas e validação

**Por que usar:**
- ✅ **Type Safety:** Compilador garante que apenas estados válidos existem
- ✅ **Encapsulamento:** Regras de transição ficam no próprio enum
- ✅ **Prevenção de Erros:** Previne transições inválidas (ex: PAID → PENDING)
- ✅ **Manutenibilidade:** Centraliza lógica de negócio no domínio

**📁 Código:**
- OrderStatus Enum: [`backend/src/main/java/com/marcelo/orchestrator/domain/model/OrderStatus.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/model/OrderStatus.java)
- Uso no Order: [`backend/src/main/java/com/marcelo/orchestrator/domain/model/Order.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/model/Order.java) (método `updateStatus`)

**💡 Exemplo de Uso:**
```java
// Validação de transição
if (order.getStatus().canTransitionTo(OrderStatus.PAID)) {
    order.updateStatus(OrderStatus.PAID);
} else {
    throw new InvalidOrderStatusException("Cannot transition from " + order.getStatus() + " to PAID");
}
```

---

#### 7. **Strategy Pattern** ⭐

**O que é:** Padrão que define uma família de algoritmos, encapsula cada um e os torna intercambiáveis.

**Implementação no Projeto:**
- **Event Publishers:** Diferentes estratégias (Kafka, Pub/Sub, RabbitMQ) implementam a mesma interface
- **Payment Gateways:** Diferentes gateways podem ser trocados (AbacatePay, Stripe, PayPal)

**Por que usar:**
- ✅ **Intercambiabilidade:** Algoritmo pode ser trocado em runtime
- ✅ **Extensibilidade:** Fácil adicionar novas estratégias
- ✅ **Desacoplamento:** Cliente não conhece qual estratégia está sendo usada

**📁 Código:**
- Event Publishers: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/)
- Port (Interface): [`backend/src/main/java/com/marcelo/orchestrator/domain/port/EventPublisherPort.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/port/EventPublisherPort.java)

---

#### 8. **Value Objects Pattern** ⭐

**O que é:** Objetos imutáveis que representam conceitos descritivos do domínio, identificados apenas por seus valores.

**Implementação no Projeto:**
- **Money:** Representa valores monetários (amount + currency)
- **OrderNumber:** Representa número único do pedido
- **OrderItem:** Representa item do pedido

**Por que usar:**
- ✅ **Imutabilidade:** Previne alterações acidentais
- ✅ **Validação:** Validação no construtor garante objetos sempre válidos
- ✅ **Semântica:** Código mais expressivo e legível
- ✅ **DDD:** Padrão fundamental do Domain-Driven Design

**📁 Código:**
- Money: [`backend/src/main/java/com/marcelo/orchestrator/domain/model/Money.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/model/Money.java)
- OrderNumber: [`backend/src/main/java/com/marcelo/orchestrator/domain/model/OrderNumber.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/model/OrderNumber.java)
- OrderItem: [`backend/src/main/java/com/marcelo/orchestrator/domain/model/OrderItem.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/model/OrderItem.java)

---

#### 9. **Rich Domain Model** ⭐

**O que é:** Modelo de domínio onde as entidades contêm lógica de negócio, não apenas dados.

**Implementação no Projeto:**
- **Order:** Entidade rica com métodos de negócio (updateStatus, markAsPaid, calculateTotal)

**Por que usar:**
- ✅ **Encapsulamento:** Regras de negócio próximas aos dados
- ✅ **Coesão:** Tudo relacionado a um pedido está em um único lugar
- ✅ **Sem Anemia:** Evita "Anemic Domain Model" (entidades apenas com getters/setters)
- ✅ **Testabilidade:** Regras podem ser testadas sem dependências externas

**📁 Código:**
- Order Entity: [`backend/src/main/java/com/marcelo/orchestrator/domain/model/Order.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/model/Order.java)

**💡 Exemplo:**
```java
// Rich Domain Model - lógica de negócio na entidade
public class Order {
    public void updateStatus(OrderStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusException("Invalid transition");
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }
}
```

---

#### 10. **CQRS (Command Query Responsibility Segregation)** ⭐

**O que é:** Padrão que separa operações de leitura (queries) de operações de escrita (commands).

**Implementação no Projeto:**
- **Commands:** CreateOrderCommand, ProcessPaymentCommand, AnalyzeRiskCommand
- **Queries:** Via OrderRepositoryPort.findAll(), findById()

**Por que usar:**
- ✅ **Separação de Concerns:** Leitura e escrita otimizadas separadamente
- ✅ **Escalabilidade:** Pode escalar leitura e escrita independentemente
- ✅ **Clareza:** Código mais claro sobre intenção (comando vs. consulta)

**📁 Código:**
- Commands: [`backend/src/main/java/com/marcelo/orchestrator/application/usecase/`](../backend/src/main/java/com/marcelo/orchestrator/application/usecase/)
- Queries: [`backend/src/main/java/com/marcelo/orchestrator/domain/port/OrderRepositoryPort.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/port/OrderRepositoryPort.java)

---

### 🎯 Princípios SOLID Aplicados

#### **S - Single Responsibility Principle (Princípio da Responsabilidade Única)** ⭐

**O que é:** Uma classe deve ter apenas uma razão para mudar, ou seja, deve ter apenas uma responsabilidade.

**Aplicação no Projeto:**
- ✅ **OrderSagaOrchestrator:** Responsável apenas por orquestrar a saga
- ✅ **CreateOrderUseCase:** Responsável apenas por criar pedidos
- ✅ **OrderRepositoryAdapter:** Responsável apenas por adaptar domínio para JPA
- ✅ **AbacatePayAdapter:** Responsável apenas por integrar com gateway de pagamento

**📁 Exemplos de Código:**
- Orchestrator: [`backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaOrchestrator.java`](../backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaOrchestrator.java)
- Use Cases: [`backend/src/main/java/com/marcelo/orchestrator/application/usecase/`](../backend/src/main/java/com/marcelo/orchestrator/application/usecase/)

**💡 Exemplo:**
```java
// ❌ Violação: Classe com múltiplas responsabilidades
class OrderService {
    void createOrder() { }
    void sendEmail() { }
    void generateInvoice() { }
}

// ✅ Correto: Cada classe uma responsabilidade
class CreateOrderUseCase {
    void execute(CreateOrderCommand command) { }
}
class EmailService {
    void send(Email email) { }
}
class InvoiceService {
    void generate(Order order) { }
}
```

---

#### **O - Open/Closed Principle (Princípio Aberto/Fechado)** ⭐

**O que é:** Entidades devem estar abertas para extensão, mas fechadas para modificação.

**Aplicação no Projeto:**
- ✅ **Event Publishers:** Pode adicionar novos brokers (SQS, SNS) sem modificar código existente
- ✅ **Payment Gateways:** Pode adicionar novos gateways (Stripe, PayPal) sem modificar domínio
- ✅ **OrderStatus:** Pode adicionar novos estados sem quebrar código existente

**📁 Exemplos de Código:**
- Factory Pattern: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/factory/EventPublisherFactory.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/factory/EventPublisherFactory.java)

**💡 Exemplo:**
```java
// ✅ Aberto para extensão: Adicionar novo adapter sem modificar Factory
public class SqsEventPublisherAdapter implements EventPublisherPort {
    // Nova implementação
}

// Factory pode ser estendida sem modificar código existente
case SQS -> new SqsEventPublisherAdapter();
```

---

#### **L - Liskov Substitution Principle (Princípio da Substituição de Liskov)** ⭐

**O que é:** Objetos de uma superclasse devem ser substituíveis por objetos de suas subclasses sem quebrar a aplicação.

**Aplicação no Projeto:**
- ✅ **Adapters:** Qualquer adapter (Kafka, Pub/Sub, RabbitMQ) pode substituir EventPublisherPort sem quebrar código
- ✅ **Repositories:** OrderRepositoryAdapter pode ser substituído por MongoDBAdapter sem alterar domínio
- ✅ **Payment Gateways:** AbacatePayAdapter pode ser substituído por StripeAdapter sem alterar código cliente

**📁 Exemplos de Código:**
- Event Publishers: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/)
- Port (Interface): [`backend/src/main/java/com/marcelo/orchestrator/domain/port/EventPublisherPort.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/port/EventPublisherPort.java)

**💡 Exemplo:**
```java
// ✅ Qualquer implementação pode substituir a interface
EventPublisherPort publisher = new KafkaEventPublisherAdapter();
EventPublisherPort publisher = new PubSubEventPublisherAdapter();
EventPublisherPort publisher = new RabbitMqEventPublisherAdapter();
// Código cliente não precisa mudar!
```

---

#### **I - Interface Segregation Principle (Princípio da Segregação de Interface)** ⭐

**O que é:** Clientes não devem ser forçados a depender de interfaces que não usam.

**Aplicação no Projeto:**
- ✅ **Ports Específicos:** OrderRepositoryPort, PaymentGatewayPort, RiskAnalysisPort (não uma interface grande)
- ✅ **Separação de Concerns:** Cada port tem responsabilidade específica

**📁 Exemplos de Código:**
- Ports: [`backend/src/main/java/com/marcelo/orchestrator/domain/port/`](../backend/src/main/java/com/marcelo/orchestrator/domain/port/)
  - OrderRepositoryPort: [`backend/src/main/java/com/marcelo/orchestrator/domain/port/OrderRepositoryPort.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/port/OrderRepositoryPort.java)
  - PaymentGatewayPort: [`backend/src/main/java/com/marcelo/orchestrator/domain/port/PaymentGatewayPort.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/port/PaymentGatewayPort.java)
  - RiskAnalysisPort: [`backend/src/main/java/com/marcelo/orchestrator/domain/port/RiskAnalysisPort.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/port/RiskAnalysisPort.java)

**💡 Exemplo:**
```java
// ❌ Violação: Interface grande forçando implementação de métodos não usados
interface OrderService {
    void createOrder();
    void sendEmail();
    void generateInvoice();
    void processPayment();
}

// ✅ Correto: Interfaces segregadas
interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(UUID id);
}
interface PaymentGatewayPort {
    PaymentResult processPayment(PaymentRequest request);
}
```

---

#### **D - Dependency Inversion Principle (Princípio da Inversão de Dependência)** ⭐

**O que é:** Módulos de alto nível não devem depender de módulos de baixo nível. Ambos devem depender de abstrações.

**Aplicação no Projeto:**
- ✅ **Domain define Ports:** Interfaces são definidas no domínio (alto nível)
- ✅ **Infrastructure implementa:** Adapters implementam as interfaces (baixo nível)
- ✅ **Dependency Injection:** Spring injeta implementações via interfaces

**📁 Exemplos de Código:**
- Port (Interface no Domain): [`backend/src/main/java/com/marcelo/orchestrator/domain/port/OrderRepositoryPort.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/port/OrderRepositoryPort.java)
- Adapter (Implementação na Infrastructure): [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/adapter/OrderRepositoryAdapter.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/adapter/OrderRepositoryAdapter.java)
- Uso no Use Case: [`backend/src/main/java/com/marcelo/orchestrator/application/usecase/CreateOrderUseCase.java`](../backend/src/main/java/com/marcelo/orchestrator/application/usecase/CreateOrderUseCase.java)

**💡 Exemplo:**
```java
// ✅ Alto nível (Application) depende de abstração (Port)
public class CreateOrderUseCase {
    private final OrderRepositoryPort repository; // Interface, não implementação!
    
    // Spring injeta OrderRepositoryAdapter (implementação)
    // Mas Use Case não conhece JPA!
}

// ✅ Baixo nível (Infrastructure) implementa abstração
@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {
    // Implementação com JPA
}
```

---

### 📊 Resumo para Entrevistas

**Design Patterns Implementados:**
1. ✅ Factory Pattern
2. ✅ Adapter Pattern
3. ✅ Repository Pattern
4. ✅ Builder Pattern
5. ✅ Saga Pattern
6. ✅ State Machine Pattern
7. ✅ Strategy Pattern
8. ✅ Value Objects Pattern
9. ✅ Rich Domain Model
10. ✅ CQRS

**Princípios SOLID Aplicados:**
1. ✅ **S** - Single Responsibility
2. ✅ **O** - Open/Closed
3. ✅ **L** - Liskov Substitution
4. ✅ **I** - Interface Segregation
5. ✅ **D** - Dependency Inversion

**📚 Documentação Completa:**
- [Contexto do Projeto - Padrões e SOLID](CONTEXTO-PROJETO.md#padrões-de-design-implementados) - Explicação detalhada de todos os padrões
- [Arquitetura para Diagrama - Padrões](ARQUITETURA-PARA-DIAGRAMA.md#-padrões-de-design-utilizados) - Visão geral

---

## ☕ Linguagem e Runtime

### Java 21

**O que é:** Última versão LTS do Java com suporte nativo a Virtual Threads (Project Loom).

**Por que usar:**
- ✅ **Virtual Threads:** 100.000+ requisições simultâneas com ~100MB de memória
- ✅ **LTS até 2029:** Suporte de longo prazo
- ✅ **Modernidade:** Stack mais moderna que requisitos enterprise (Java 8+)

**📁 Código:**
- Configuração: [`backend/pom.xml`](../backend/pom.xml) (linhas 26-30)
- Aplicação Principal: [`backend/src/main/java/com/marcelo/orchestrator/OrchestratorApplication.java`](../backend/src/main/java/com/marcelo/orchestrator/OrchestratorApplication.java)

**📚 Documentação Detalhada:**
- [Fase 9: Virtual Threads e Performance](fases/FASE9-VIRTUAL-THREADS.md) - Conceitos, benefícios, otimizações e métricas
- [README - Tech Stack](../README.md#-tech-stack) - Visão geral da stack
- [Contexto do Projeto - Stack Tecnológico](CONTEXTO-PROJETO.md#-stack-tecnológico-e-justificativas) - Justificativas detalhadas

**🔗 Links Externos:**
- [Java 21 Release Notes](https://openjdk.org/projects/jdk/21/)
- [Project Loom (Virtual Threads)](https://openjdk.org/projects/loom/)

---

## 🌱 Framework e Core

### Spring Boot 3.2+

**O que é:** Framework enterprise para desenvolvimento rápido de aplicações Java.

**Por que usar:**
- ✅ **Suporte nativo a Virtual Threads:** Configuração automática
- ✅ **Autoconfiguração:** Reduz boilerplate
- ✅ **Ecosystem maduro:** Spring Data, Spring WebFlux, Spring Actuator

**📁 Código:**
- Configuração Maven: [`backend/pom.xml`](../backend/pom.xml) (linhas 12-17, 43-47)
- Aplicação Principal: [`backend/src/main/java/com/marcelo/orchestrator/OrchestratorApplication.java`](../backend/src/main/java/com/marcelo/orchestrator/OrchestratorApplication.java)

**📚 Documentação Detalhada:**
- [Fase 1: Fundação e Estrutura](fases/FASE1-FUNDACAO-ESTRUTURA.md) - Configuração inicial do projeto
- [Contexto do Projeto - Stack Tecnológico](CONTEXTO-PROJETO.md#backend) - Justificativas

**🔗 Links Externos:**
- [Spring Boot 3.2 Documentation](https://docs.spring.io/spring-boot/docs/3.2.0/reference/html/)

---

### Spring Data JPA

**O que é:** Abstração para persistência com JPA/Hibernate.

**Por que usar:**
- ✅ **Repositories:** Reduz código boilerplate
- ✅ **Transações:** Gerenciamento automático
- ✅ **Query Methods:** Queries derivadas automaticamente

**📁 Código:**
- Configuração: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/config/JpaConfig.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/config/JpaConfig.java)
- Repository Adapter: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/adapter/OrderRepositoryAdapter.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/adapter/OrderRepositoryAdapter.java)
- JPA Repository: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/repository/JpaOrderRepository.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/repository/JpaOrderRepository.java)

**📚 Documentação Detalhada:**
- [Fase 4: Infrastructure - Persistência](fases/FASE4-CAMADA-INFRASTRUCTURE-PERSISTENCIA.md) - Implementação completa de JPA
- [Fase 2: Camada Domain](fases/FASE2-CAMADA-DOMAIN.md) - Ports e interfaces

---

### Spring WebFlux (WebClient)

**O que é:** Cliente HTTP reativo para chamadas não-bloqueantes.

**Por que usar:**
- ✅ **Não-bloqueante:** Ideal para Virtual Threads
- ✅ **Reativo:** Melhor utilização de recursos
- ✅ **Integrações externas:** AbacatePay, OpenAI

**📁 Código:**
- AbacatePay Adapter: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/payment/adapter/AbacatePayAdapter.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/payment/adapter/AbacatePayAdapter.java)
- OpenAI Adapter: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/ai/adapter/OpenAIRiskAnalysisAdapter.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/ai/adapter/OpenAIRiskAnalysisAdapter.java)
- Configuração OpenAI: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/ai/config/OpenAIConfig.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/ai/config/OpenAIConfig.java)

**📚 Documentação Detalhada:**
- [README - AbacatePay Integration](README-ABACATEPAY.md) - Integração com gateway de pagamento
- [README - OpenAI Integration](README-OPENAI.md) - Integração com IA

---

## 🗄️ Banco de Dados e Persistência

### PostgreSQL

**O que é:** Banco relacional robusto com garantias ACID.

**Por que usar:**
- ✅ **ACID:** Consistência garantida para dados críticos
- ✅ **Robustez:** Banco maduro e confiável
- ✅ **Performance:** Excelente para operações complexas

**📁 Código:**
- Configuração: [`backend/src/main/resources/application.yml`](../backend/src/main/resources/application.yml)
- Entity: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/entity/OrderEntity.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/entity/OrderEntity.java)
- Saga Execution Entity: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/entity/SagaExecutionEntity.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/entity/SagaExecutionEntity.java)

**📚 Documentação Detalhada:**
- [Fase 4: Infrastructure - Persistência](fases/FASE4-CAMADA-INFRASTRUCTURE-PERSISTENCIA.md) - Schema, migrations, entities
- [Fase 1: Fundação e Estrutura](fases/FASE1-FUNDACAO-ESTRUTURA.md) - Docker Compose setup

---

### Flyway

**O que é:** Ferramenta de migrations versionadas para banco de dados.

**Por que usar:**
- ✅ **Versionamento:** Schema versionado como código
- ✅ **Reprodutibilidade:** Fácil deploy em diferentes ambientes
- ✅ **Auditoria:** Histórico completo de mudanças

**📁 Código:**
- Migrations: [`backend/src/main/resources/db/migration/`](../backend/src/main/resources/db/migration/)
- Configuração: [`backend/src/main/resources/application.yml`](../backend/src/main/resources/application.yml)

**📚 Documentação Detalhada:**
- [Fase 4: Infrastructure - Persistência](fases/FASE4-CAMADA-INFRASTRUCTURE-PERSISTENCIA.md) - Migrations e schema

---

### MapStruct

**O que é:** Gerador de código para mappers type-safe entre camadas.

**Por que usar:**
- ✅ **Type-safe:** Erros em tempo de compilação
- ✅ **Performático:** Geração de código, sem overhead de runtime
- ✅ **Reduz boilerplate:** Mapeamento automático entre Domain e Infrastructure

**📁 Código:**
- Order Mapper: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/mapper/OrderMapper.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/mapper/OrderMapper.java)
- Presentation Mapper: [`backend/src/main/java/com/marcelo/orchestrator/presentation/mapper/OrderMapper.java`](../backend/src/main/java/com/marcelo/orchestrator/presentation/mapper/OrderMapper.java)

**📚 Documentação Detalhada:**
- [Fase 4: Infrastructure - Persistência](fases/FASE4-CAMADA-INFRASTRUCTURE-PERSISTENCIA.md) - Mappers e conversão de entidades

---

## 🛡️ Resiliência e Circuit Breaker

### Resilience4j

**O que é:** Biblioteca para resiliência em sistemas distribuídos (Circuit Breaker, Retry, Fallback).

**Por que usar:**
- ✅ **Circuit Breaker:** Proteção contra falhas em cascata
- ✅ **Retry:** Recuperação automática de falhas transitórias
- ✅ **Fallback:** Sistema continua funcionando mesmo com serviços offline

**📁 Código:**
- AbacatePay com Circuit Breaker: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/payment/adapter/AbacatePayAdapter.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/payment/adapter/AbacatePayAdapter.java)
- OpenAI com Circuit Breaker: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/ai/adapter/OpenAIRiskAnalysisAdapter.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/ai/adapter/OpenAIRiskAnalysisAdapter.java)
- Configuração: [`backend/src/main/resources/application.yml`](../backend/src/main/resources/application.yml) (seção `resilience4j`)

**📚 Documentação Detalhada:**
- [README - AbacatePay Integration](README-ABACATEPAY.md) - Circuit Breaker em pagamentos
- [README - OpenAI Integration](README-OPENAI.md) - Circuit Breaker em IA
- [Contexto do Projeto - Features](CONTEXTO-PROJETO.md#4-circuit-breaker-resilience4j) - Explicação detalhada

**🔗 Links Externos:**
- [Resilience4j Documentation](https://resilience4j.readme.io/)

---

## 🏗️ Arquitetura e Padrões

### Arquitetura Hexagonal (Ports and Adapters)

**O que é:** Padrão arquitetural que isola o domínio das tecnologias externas.

**Por que usar:**
- ✅ **Isolamento:** Domínio não conhece JPA, HTTP, Kafka
- ✅ **Testabilidade:** Fácil mockar adapters
- ✅ **Flexibilidade:** Trocar implementações sem alterar domínio

**📁 Código:**
- Estrutura de Pacotes: [`backend/src/main/java/com/marcelo/orchestrator/`](../backend/src/main/java/com/marcelo/orchestrator/)
  - Domain: [`backend/src/main/java/com/marcelo/orchestrator/domain/`](../backend/src/main/java/com/marcelo/orchestrator/domain/)
  - Application: [`backend/src/main/java/com/marcelo/orchestrator/application/`](../backend/src/main/java/com/marcelo/orchestrator/application/)
  - Infrastructure: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/)
  - Presentation: [`backend/src/main/java/com/marcelo/orchestrator/presentation/`](../backend/src/main/java/com/marcelo/orchestrator/presentation/)

**📚 Documentação Detalhada:**
- [Fase 1: Fundação e Estrutura](fases/FASE1-FUNDACAO-ESTRUTURA.md) - Estrutura de pacotes
- [Fase 2: Camada Domain](fases/FASE2-CAMADA-DOMAIN.md) - Modelos e Ports
- [Fase 3: Camada Application](fases/FASE3-CAMADA-APPLICATION.md) - Use Cases
- [Fase 4: Infrastructure - Persistência](fases/FASE4-CAMADA-INFRASTRUCTURE-PERSISTENCIA.md) - Adapters
- [Arquitetura para Diagrama](ARQUITETURA-PARA-DIAGRAMA.md) - Descrição completa da arquitetura
- [Contexto do Projeto - Arquitetura](CONTEXTO-PROJETO.md#arquitetura-hexagonal-ports-and-adapters) - Explicação detalhada

---

### Saga Pattern (Orchestration)

**O que é:** Padrão para gerenciar transações distribuídas com compensação automática.

**Por que usar:**
- ✅ **Consistência Eventual:** Garantida em microserviços
- ✅ **Compensação:** Rollback automático em caso de falha
- ✅ **Observabilidade:** Rastreamento completo de cada execução

**📁 Código:**
- Orquestrador: [`backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaOrchestrator.java`](../backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaOrchestrator.java)
- Command: [`backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaCommand.java`](../backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaCommand.java)
- Result: [`backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaResult.java`](../backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaResult.java)
- Saga Execution Entity: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/entity/SagaExecutionEntity.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/entity/SagaExecutionEntity.java)

**📚 Documentação Detalhada:**
- [Fase 7: Saga Pattern](fases/FASE7-SAGA-PATTERN.md) - Implementação completa e explicação detalhada
- [Contexto do Projeto - Saga Pattern](CONTEXTO-PROJETO.md#1-saga-pattern-orchestration-completo) - Fluxo e benefícios
- [README - Fluxo Principal](../README.md#-fluxo-principal-saga-pattern) - Visão geral

---

### Idempotência

**O que é:** Prevenção de duplicação de operações usando `idempotencyKey`.

**Por que usar:**
- ✅ **Zero duplicação:** Mesmo com retry/timeout
- ✅ **Retry seguro:** Usuário pode clicar várias vezes
- ✅ **Padrão obrigatório:** Stripe, PayPal, Mercado Livre

**📁 Código:**
- Verificação no Orchestrator: [`backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaOrchestrator.java`](../backend/src/main/java/com/marcelo/orchestrator/application/saga/OrderSagaOrchestrator.java) (método `execute`)
- Campo na Entity: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/entity/SagaExecutionEntity.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/entity/SagaExecutionEntity.java)
- Migration: [`backend/src/main/resources/db/migration/V3__add_idempotency_key_to_saga.sql`](../backend/src/main/resources/db/migration/V3__add_idempotency_key_to_saga.sql)

**📚 Documentação Detalhada:**
- [Fase 7: Saga Pattern](fases/FASE7-SAGA-PATTERN.md) - Seção sobre Idempotência
- [Contexto do Projeto - Idempotência](CONTEXTO-PROJETO.md#2-idempotência) - Implementação e benefícios

---

## 📡 Event-Driven Architecture

### Factory Pattern para Message Brokers

**O que é:** Factory que cria adapters de eventos baseado em configuração (Kafka, Pub/Sub, RabbitMQ, In-Memory).

**Por que usar:**
- ✅ **Desacoplamento:** Serviços não conhecem uns aos outros
- ✅ **Escalabilidade:** Processamento assíncrono
- ✅ **Flexibilidade:** Trocar broker via configuração

**📁 Código:**
- Factory: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/factory/EventPublisherFactory.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/factory/EventPublisherFactory.java)
- Port (Interface): [`backend/src/main/java/com/marcelo/orchestrator/domain/port/EventPublisherPort.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/port/EventPublisherPort.java)
- Kafka Adapter: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/KafkaEventPublisherAdapter.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/KafkaEventPublisherAdapter.java)
- Pub/Sub Adapter: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/PubSubEventPublisherAdapter.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/PubSubEventPublisherAdapter.java)
- RabbitMQ Adapter: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/RabbitMqEventPublisherAdapter.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/RabbitMqEventPublisherAdapter.java)
- In-Memory Adapter: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/InMemoryEventPublisherAdapter.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/InMemoryEventPublisherAdapter.java)
- Configuração: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/config/EventPublisherConfig.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/config/EventPublisherConfig.java)

**📁 Eventos de Domínio:**
- OrderCreatedEvent: [`backend/src/main/java/com/marcelo/orchestrator/domain/event/saga/OrderCreatedEvent.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/event/saga/OrderCreatedEvent.java)
- PaymentProcessedEvent: [`backend/src/main/java/com/marcelo/orchestrator/domain/event/saga/PaymentProcessedEvent.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/event/saga/PaymentProcessedEvent.java)
- SagaCompletedEvent: [`backend/src/main/java/com/marcelo/orchestrator/domain/event/saga/SagaCompletedEvent.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/event/saga/SagaCompletedEvent.java)
- SagaFailedEvent: [`backend/src/main/java/com/marcelo/orchestrator/domain/event/saga/SagaFailedEvent.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/event/saga/SagaFailedEvent.java)

**📚 Documentação Detalhada:**
- [Contexto do Projeto - Event-Driven Architecture](CONTEXTO-PROJETO.md#5-event-driven-architecture) - Explicação detalhada
- [Fase 3: Camada Application](fases/FASE3-CAMADA-APPLICATION.md) - Publicação de eventos

---

## ⚡ Performance e Concorrência

### Virtual Threads (Java 21)

**O que é:** Threads leves gerenciadas pela JVM, permitindo milhões de threads simultâneas com baixo consumo de memória.

**Por que usar:**
- ✅ **100.000+ threads:** Com apenas ~100MB de memória
- ✅ **1000x mais threads:** Comparado a Platform Threads
- ✅ **Não bloqueia OS:** CPU não fica ociosa esperando I/O

**📁 Código:**
- Configuração: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/config/PerformanceConfig.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/config/PerformanceConfig.java)
- Application.yml: [`backend/src/main/resources/application.yml`](../backend/src/main/resources/application.yml) (seção `spring.threads.virtual`)

**📚 Documentação Detalhada:**
- [Fase 9: Virtual Threads e Performance](fases/FASE9-VIRTUAL-THREADS.md) - **Documentação completa** com:
  - Conceitos e diferenças (Platform vs Virtual Threads)
  - Benefícios e métricas
  - Configuração e otimizações
  - Como explicar em entrevistas
- [Contexto do Projeto - Virtual Threads](CONTEXTO-PROJETO.md#6-virtual-threads-java-21) - Explicação e benefícios
- [README - Performance](../README.md#-números-concretos-de-performance) - Métricas concretas

**🔗 Links Externos:**
- [Project Loom (Virtual Threads)](https://openjdk.org/projects/loom/)

---

### HikariCP (Connection Pool)

**O que é:** Pool de conexões otimizado para alta concorrência.

**Por que usar:**
- ✅ **Performance:** Pool otimizado para Virtual Threads
- ✅ **Alta concorrência:** Suporta milhares de conexões simultâneas
- ✅ **Configuração:** Ajustado para Virtual Threads

**📁 Código:**
- Configuração: [`backend/src/main/resources/application.yml`](../backend/src/main/resources/application.yml) (seção `spring.datasource.hikari`)

**📚 Documentação Detalhada:**
- [Fase 9: Virtual Threads e Performance](fases/FASE9-VIRTUAL-THREADS.md) - Seção sobre otimizações de pool

---

## 🤖 IA e MCP Code Review

### OpenAI Integration

**O que é:** Integração com OpenAI API para análise inteligente de risco de pagamento.

**Por que usar:**
- ✅ **IA em Produção:** Demonstra uso de IA em sistemas enterprise
- ✅ **Análise Inteligente:** Risco de pagamento contextualizado
- ✅ **Circuit Breaker:** Proteção contra falhas da API

**📁 Código:**
- Adapter: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/ai/adapter/OpenAIRiskAnalysisAdapter.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/ai/adapter/OpenAIRiskAnalysisAdapter.java)
- Config: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/ai/config/OpenAIConfig.java`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/ai/config/OpenAIConfig.java)
- DTOs: [`backend/src/main/java/com/marcelo/orchestrator/infrastructure/ai/dto/`](../backend/src/main/java/com/marcelo/orchestrator/infrastructure/ai/dto/)
- Port: [`backend/src/main/java/com/marcelo/orchestrator/domain/port/RiskAnalysisPort.java`](../backend/src/main/java/com/marcelo/orchestrator/domain/port/RiskAnalysisPort.java)

**📚 Documentação Detalhada:**
- [README - OpenAI Integration](README-OPENAI.md) - Integração completa e exemplos
- [Contexto do Projeto - IA](CONTEXTO-PROJETO.md#7-integração-com-ia-openai) - Explicação e benefícios

---

### MCP Code Review Server

**O que é:** Servidor MCP (Model Context Protocol) para code review automatizado com IA.

**Por que usar:**
- ✅ **Protocolo MCP:** Padrão emergente para IA
- ✅ **Code Review Automatizado:** Análise estática + IA
- ✅ **Diferencial Competitivo:** Demonstra expertise em Engenharia de IA

**📁 Código:**
- Módulo Completo: [`mcp-code-review/`](../mcp-code-review/)
- Servidor: [`mcp-code-review/src/main/java/com/marcelo/orchestrator/mcp/server/McpServer.java`](../mcp-code-review/src/main/java/com/marcelo/orchestrator/mcp/server/McpServer.java)
- Controller: [`mcp-code-review/src/main/java/com/marcelo/orchestrator/mcp/controller/McpController.java`](../mcp-code-review/src/main/java/com/marcelo/orchestrator/mcp/controller/McpController.java)
- Code Review Tool: [`mcp-code-review/src/main/java/com/marcelo/orchestrator/mcp/tools/CodeReviewTool.java`](../mcp-code-review/src/main/java/com/marcelo/orchestrator/mcp/tools/CodeReviewTool.java)
- Pattern Analysis Tool: [`mcp-code-review/src/main/java/com/marcelo/orchestrator/mcp/tools/PatternAnalysisTool.java`](../mcp-code-review/src/main/java/com/marcelo/orchestrator/mcp/tools/PatternAnalysisTool.java)
- Code Analyzer: [`mcp-code-review/src/main/java/com/marcelo/orchestrator/mcp/analyzer/CodeAnalyzer.java`](../mcp-code-review/src/main/java/com/marcelo/orchestrator/mcp/analyzer/CodeAnalyzer.java)
- Pattern Detector: [`mcp-code-review/src/main/java/com/marcelo/orchestrator/mcp/analyzer/PatternDetector.java`](../mcp-code-review/src/main/java/com/marcelo/orchestrator/mcp/analyzer/PatternDetector.java)
- AI Feedback Service: [`mcp-code-review/src/main/java/com/marcelo/orchestrator/mcp/ai/AiFeedbackService.java`](../mcp-code-review/src/main/java/com/marcelo/orchestrator/mcp/ai/AiFeedbackService.java)

**📚 Documentação Detalhada:**
- [MCP Code Review - README](../mcp-code-review/README.md) - Documentação completa do módulo
- [Plano de Implementação MCP](PLANO-IMPLEMENTACAO-MCP-CODE-REVIEW.md) - Plano técnico detalhado
- [Contexto do Projeto - MCP](CONTEXTO-PROJETO.md#8-mcp-code-review-server) - Explicação e arquitetura

**🔗 Links Externos:**
- [Model Context Protocol](https://modelcontextprotocol.io/)
- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk)

---

## 📡 API e Documentação

### Spring Web (REST API)

**O que é:** Framework para criação de APIs RESTful.

**Por que usar:**
- ✅ **RESTful:** Padrão da indústria
- ✅ **Validação:** Bean Validation integrado
- ✅ **Exception Handling:** Global exception handler

**📁 Código:**
- Controller: [`backend/src/main/java/com/marcelo/orchestrator/presentation/controller/OrderController.java`](../backend/src/main/java/com/marcelo/orchestrator/presentation/controller/OrderController.java)
- DTOs: [`backend/src/main/java/com/marcelo/orchestrator/presentation/dto/`](../backend/src/main/java/com/marcelo/orchestrator/presentation/dto/)
- Exception Handler: [`backend/src/main/java/com/marcelo/orchestrator/presentation/exception/GlobalExceptionHandler.java`](../backend/src/main/java/com/marcelo/orchestrator/presentation/exception/GlobalExceptionHandler.java)

**📚 Documentação Detalhada:**
- [Fase 8: REST API](fases/FASE8-CAMADA-PRESENTATION-REST-API.md) - Controllers, DTOs, validação
- [Guia de Teste Backend com Bruno](GUIA-TESTE-BACKEND-BRUNO.md) - Como testar a API

---

### Swagger/OpenAPI (SpringDoc)

**O que é:** Documentação automática da API REST.

**Por que usar:**
- ✅ **Documentação Automática:** Sempre atualizada
- ✅ **Teste Interativo:** Swagger UI para testar endpoints
- ✅ **Contrato de API:** Especificação OpenAPI

**📁 Código:**
- Configuração: [`backend/src/main/java/com/marcelo/orchestrator/presentation/config/OpenApiConfig.java`](../backend/src/main/java/com/marcelo/orchestrator/presentation/config/OpenApiConfig.java)

**📚 Documentação Detalhada:**
- [Fase 8: REST API](fases/FASE8-CAMADA-PRESENTATION-REST-API.md) - Seção sobre Swagger
- [README - Como Rodar](../README.md#-como-rodar) - Acesso ao Swagger UI

---

## 🎨 Frontend

### React 18+

**O que é:** Biblioteca JavaScript para construção de interfaces de usuário.

**Por que usar:**
- ✅ **Componentização:** Código reutilizável
- ✅ **Ecosystem:** Grande ecossistema de bibliotecas
- ✅ **Performance:** Virtual DOM otimizado

**📁 Código:**
- Módulo Frontend: [`frontend/`](../frontend/)

**📚 Documentação Detalhada:**
- [Frontend - Propósito e Integração](FRONTEND-PROPOSITO-E-INTEGRACAO.md) - Integração frontend/backend
- [Frontend - Testes](FRONTEND-TESTES-JORNADA-INTEGRACAO.md) - Estratégia de testes
- [Frontend README](../frontend/README.md) - Documentação do frontend

---

### TypeScript

**O que é:** Superset do JavaScript com type safety.

**Por que usar:**
- ✅ **Type Safety:** Erros em tempo de compilação
- ✅ **Autocomplete:** Melhor DX
- ✅ **Menos Bugs:** Código mais seguro

**📁 Código:**
- Configuração: [`frontend/tsconfig.json`](../frontend/tsconfig.json)

---

### TailwindCSS

**O que é:** Framework CSS utility-first.

**Por que usar:**
- ✅ **Estilização Rápida:** Classes utilitárias
- ✅ **Consistência:** Design system integrado
- ✅ **Responsividade:** Mobile-first

**📁 Código:**
- Configuração: [`frontend/tailwind.config.js`](../frontend/tailwind.config.js)

---

### Zustand

**O que é:** Biblioteca leve para gerenciamento de estado.

**Por que usar:**
- ✅ **Leve:** Sem boilerplate excessivo
- ✅ **Simples:** API intuitiva
- ✅ **Performático:** Re-renders otimizados

**📁 Código:**
- Stores: [`frontend/src/stores/`](../frontend/src/stores/)

---

## 🔧 Infraestrutura e DevOps

### Docker Compose

**O que é:** Orquestração de containers para desenvolvimento local.

**Por que usar:**
- ✅ **Isolamento:** Ambiente isolado e reprodutível
- ✅ **Fácil Setup:** PostgreSQL sem instalação local
- ✅ **Consistência:** Mesmo ambiente para todos

**📁 Código:**
- Docker Compose: [`backend/scripts/docker-compose.yml`](../backend/scripts/docker-compose.yml)

**📚 Documentação Detalhada:**
- [Fase 1: Fundação e Estrutura](fases/FASE1-FUNDACAO-ESTRUTURA.md) - Setup do Docker Compose
- [README - Como Rodar](../README.md#-como-rodar) - Iniciar PostgreSQL

---

### Google Cloud Secret Manager

**O que é:** Gerenciamento seguro de secrets (chaves de API, senhas) no GCP.

**Por que usar:**
- ✅ **Segurança:** Secrets não ficam no código
- ✅ **IAM:** Controle de acesso granular
- ✅ **Rotação:** Fácil rotação de chaves

**📁 Código:**
- Configuração: [`backend/src/main/resources/application-prod.yml`](../backend/src/main/resources/application-prod.yml)
- Script Setup: [`backend/scripts/setup-secrets-gcp.sh`](../backend/scripts/setup-secrets-gcp.sh)

**📚 Documentação Detalhada:**
- [Segurança - Gerenciamento de Secrets](SEGURANCA-GERENCIAMENTO-SECRETS.md) - Estratégia completa de segurança
- [Deploy GCP - Recursos Necessários](DEPLOY-GCP-RECURSOS-NECESSARIOS.md) - Recursos e configuração

---

### Flyway Migrations

**O que é:** Versionamento de schema do banco de dados.

**Por que usar:**
- ✅ **Versionamento:** Schema como código
- ✅ **Reprodutibilidade:** Fácil deploy em diferentes ambientes
- ✅ **Auditoria:** Histórico completo de mudanças

**📁 Código:**
- Migrations: [`backend/src/main/resources/db/migration/`](../backend/src/main/resources/db/migration/)

**📚 Documentação Detalhada:**
- [Fase 4: Infrastructure - Persistência](fases/FASE4-CAMADA-INFRASTRUCTURE-PERSISTENCIA.md) - Migrations e schema

---

## 📊 Resumo de Tecnologias

| Categoria | Tecnologia | Versão | Propósito |
|-----------|-----------|--------|-----------|
| **Linguagem** | Java | 21 | Virtual Threads, LTS até 2029 |
| **Framework** | Spring Boot | 3.2+ | Framework enterprise |
| **Banco de Dados** | PostgreSQL | Latest | ACID, robustez |
| **Resiliência** | Resilience4j | 2.1.0 | Circuit Breaker, Retry, Fallback |
| **Migrations** | Flyway | Latest | Versionamento de schema |
| **Mapeamento** | MapStruct | 1.5.5 | Type-safe mappers |
| **Boilerplate** | Lombok | 1.18.30 | Redução de código |
| **Documentação** | SpringDoc | 2.3.0 | Swagger/OpenAPI |
| **HTTP Reativo** | WebFlux | 3.2+ | WebClient para integrações |
| **IA** | OpenAI | Latest | Análise de risco |
| **Análise** | JavaParser | 3.25.1 | AST para code review |
| **Frontend** | React | 18+ | Biblioteca UI |
| **Frontend** | TypeScript | Latest | Type safety |
| **Frontend** | TailwindCSS | Latest | Utility-first CSS |
| **Frontend** | Zustand | Latest | State management |

---

## 🔗 Links Rápidos

### Documentação Principal
- [README Principal](../README.md) - Visão geral do projeto
- [Contexto Completo do Projeto](CONTEXTO-PROJETO.md) - Contexto detalhado
- [Arquitetura para Diagrama](ARQUITETURA-PARA-DIAGRAMA.md) - Descrição da arquitetura

### Documentação por Fase
- [Fase 1: Fundação e Estrutura](fases/FASE1-FUNDACAO-ESTRUTURA.md)
- [Fase 2: Camada Domain](fases/FASE2-CAMADA-DOMAIN.md)
- [Fase 3: Camada Application](fases/FASE3-CAMADA-APPLICATION.md)
- [Fase 4: Infrastructure - Persistência](fases/FASE4-CAMADA-INFRASTRUCTURE-PERSISTENCIA.md)
- [Fase 7: Saga Pattern](fases/FASE7-SAGA-PATTERN.md)
- [Fase 8: REST API](fases/FASE8-CAMADA-PRESENTATION-REST-API.md)
- [Fase 9: Virtual Threads](fases/FASE9-VIRTUAL-THREADS.md)

### Guias e Tutoriais
- [Guia Completo de Testes](GUIA-COMPLETO-DE-TESTES.md)
- [Guia de Teste Backend com Bruno](GUIA-TESTE-BACKEND-BRUNO.md)
- [Segurança - Gerenciamento de Secrets](SEGURANCA-GERENCIAMENTO-SECRETS.md)
- [Deploy GCP - Recursos Necessários](DEPLOY-GCP-RECURSOS-NECESSARIOS.md)

---

**📅 Última Atualização:** Dezembro 2024  
**👨‍💻 Mantido por:** Marcelo Hernandes da Silva

