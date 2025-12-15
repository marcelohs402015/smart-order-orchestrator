## 1. Elevator pitch (1–2 minutos)

> “Eu implementei um Orquestrador de Pedidos usando Java 21 e Spring Boot 3, em Arquitetura Hexagonal, com Saga Pattern e Event‑Driven Architecture sobre Kafka.  
> A ideia é orquestrar todo o fluxo de criação de pedidos – criação, pagamento e análise de risco – de forma transacional distribuída, resiliente e observável, sem acoplamento forte entre serviços.”

Pontos-chave:
- Domínio forte de **DDD + Hexagonal + Saga + Kafka**.
- Código organizado em **camadas e ports/adapters**, com foco em **testabilidade e SOLID**.
- Pensado para **produção**: idempotência, resiliência, performance (Virtual Threads), auditoria.

---

## 2. Arquitetura Hexagonal & organização (3–4 minutos)

Mensagem:

- “A arquitetura é Hexagonal: o domínio é independente de frameworks e infraestrutura.”

Pacotes principais:
- `domain` → modelos, **Domain Events**, ports (`OrderRepositoryPort`, `EventPublisherPort`).
- `application` → use cases + `OrderSagaOrchestrator`.
- `infrastructure` → adapters JPA, Kafka, HTTP etc.
- `presentation` → REST controllers + DTOs (Java Records).

Ganhos:
- **SOLID / DIP**:
  - Domínio depende de **interfaces** (`ports`), não de implementações concretas.
  - Facilita trocar banco, mensageria ou meios de pagamento sem tocar no domínio.
- **Testabilidade**:
  - Use cases e orquestrador testam com mocks dos ports.
- **Organização clara**:
  - `domain/event/**` para eventos de domínio.
  - `infrastructure/messaging/**` para integrações (Kafka, InMemory).

Onde estudar no código:
- `docs/CONTEXTO-PROJETO.md` – visão da arquitetura.
- `backend/src/main/java/com/marcelo/orchestrator/domain/port/` – ports principais.

---

## 3. Domain Events & pasta `domain/event` (3–4 minutos)

Mensagem:

- “Modelamos **Domain Events** como cidadãos de primeira classe no domínio, isolados em `domain/event`.”

Contrato base:
- `DomainEvent` (`domain/event/DomainEvent.java`):
  - `eventId`, `aggregateId`, `occurredAt`, `eventType`, `eventVersion`.

Eventos da Saga:
- `domain/event/saga/OrderCreatedEvent`
- `PaymentProcessedEvent`
- `SagaCompletedEvent`
- `SagaFailedEvent`

Benefícios:
- **DDD / Domain Events**:
  - Representam coisas de negócio que **aconteceram** (imutáveis).
  - Fáceis de auditar, logar, persistir.
- **Acoplamento fraco**:
  - Outros serviços podem reagir a `OrderCreatedEvent` sem que o Orchestrator conheça esses serviços.
- **Pronto para Event Sourcing / auditoria**:
  - Estrutura já traz IDs, timestamps, versionamento.

Pontos para entrevista:
- Explique por que `event` está dentro de `domain`:
  - É parte do **modelo de domínio**, não da infraestrutura.
- Cite a integração com Saga / Kafka:
  - Orchestrator publica esses eventos via `EventPublisherPort`.

Onde estudar:
- `backend/src/main/java/com/marcelo/orchestrator/domain/event/DomainEvent.java`.
- `docs/fases/FASE7-SAGA-PATTERN.md` – seção “Event-Driven Architecture Implementada”.

---

## 4. Saga Pattern & Idempotência (5 minutos)

Mensagem:

- “Implementei a criação de pedido como uma **Saga orquestrada** de 3 steps: criar pedido, processar pagamento, analisar risco.”

Orquestrador:
- `OrderSagaOrchestrator` na camada `application`.
- Usa ports para:
  - Persistir pedido.
  - Chamar gateway de pagamento.
  - Analisar risco.
  - Publicar eventos de domínio.

Pontos fortes:
- **Orquestração centralizada**:
  - O Orchestrator conhece a ordem e compensação dos passos, mas não detalhes de infraestrutura.
- **Idempotência**:
  - `idempotencyKey` na criação de pedidos.
  - Garante que requisições duplicadas não criam múltiplas sagas/pedidos.
- **Persistência de estado da Saga**:
  - Tabelas `saga_executions` e `saga_steps` para rastrear cada step.

Ganhos:
- **Consistência eventual entre serviços** sem transações distribuídas (2PC).
- **Observabilidade**:
  - Cada step da saga é rastreável (ID da saga + steps + eventos).

Onde estudar:
- `docs/fases/FASE7-SAGA-PATTERN.md`.
- `OrderSagaOrchestrator` (camada application).
- Tabelas de saga em `db/migration/V1__create_orders_table.sql`.

---

## 5. Event‑Driven + Kafka (3–4 minutos)

Mensagem:

- “Usei Kafka como **message broker** para emissão de Domain Events da saga.”

Fluxo:
- Port:
  - `EventPublisherPort` (domínio) — contrato.
- Factory:
  - `EventPublisherFactory` — escolhe implementação (Kafka, InMemory) via `app.message.broker.type`.
- Adapters:
  - `KafkaEventPublisherAdapter` → publica eventos nos tópicos:
    - `order-created`, `payment-processed`, `saga-completed`, `saga-failed`.
  - `InMemoryEventPublisherAdapter` → para testes / fallback.

Benefícios:
- **Hexagonal + DIP**:
  - Domínio não sabe sobre Kafka; só chama `EventPublisherPort`.
- **Escalabilidade**:
  - Kafka permite alta taxa de mensagens, particionamento por `aggregateId`.
- **Resiliência**:
  - Producer configurado com `acks=all`, `enable.idempotence=true`.

Performance:
- Particionamento por `aggregateId` mantém ordem dos eventos do mesmo pedido.
- Kafka é adequado para alta taxa de escrita/leitura.

Onde estudar:
- `backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/adapter/KafkaEventPublisherAdapter.java`.
- `backend/src/main/java/com/marcelo/orchestrator/infrastructure/messaging/config/KafkaConfiguration.java`.
- `docs/INSTRUCOES-TOPICOS-KAFKA.md`.

---

## 6. Performance & Resiliência (3–4 minutos)

Mensagem curta focada em **Latency, Throughput, Scalability**:

- “Pensamos em três pilares: latência baixa, throughput alto e escalabilidade horizontal.”

Pontos rápidos:

- **Java 21 + Virtual Threads**:
  - Configurado em `application.yml` (`spring.threads.virtual.enabled=true`).
  - Cada requisição HTTP usa virtual thread → alta concorrência com baixo custo.
- **DB + Flyway**:
  - PostgreSQL + HikariCP configurado para Virtual Threads.
  - `ddl-auto=validate` + migrations versionadas → segurança e reprodutibilidade.
- **Resilience4j**:
  - Circuit Breaker + Retry em integrações externas (AbacatePay, risk analysis).
  - Protege o Orchestrator de cascatas de falha.

Ganhos em termos de:
- **Latency:** 
  - Virtual Threads evitam espera de thread física bloqueada, reduzindo latência em I/O (DB, HTTP, Kafka).
  - Circuit Breaker falha rápido em integrações externas problemáticas.
- **Throughput:** 
  - Virtual Threads permitem milhares de requisições simultâneas com pouco custo de memória.
  - Kafka absorve alto volume de eventos (escrita/leitura) com particionamento por `aggregateId`.
- **Scalability:**
  - Stateless + Virtual Threads + Kafka → fácil escalar horizontalmente várias instâncias do Orchestrator.
  - Banco e tópicos Kafka dimensionáveis independentemente (microservices-friendly).

Onde estudar:
- `application.yml` (virtual threads, resilience4j, DB).
- `docs/CONTEXTO-PROJETO.md` – seção de performance / Virtual Threads.
- `docs/HIGHLIGHTS-TECNOLOGIAS.md` – Java 21, Resilience4j, Kafka.

---

## 7. Padrões de Projeto & SOLID (3–4 minutos)

Lista para citar:

- **GoF / Patterns:**
  - Factory Pattern:
    - `EventPublisherFactory` (seleção de broker: Kafka, InMemory).
  - Adapter Pattern:
    - `OrderRepositoryAdapter` (JPA ↔ domínio).
    - `KafkaEventPublisherAdapter`, `InMemoryEventPublisherAdapter`.
  - Strategy / Template Method (implícito):
    - Mapeamento `eventType → topic` no Kafka adapter.
  - Domain Events (DDD Pattern):
    - `domain/event/**`.

- **SOLID:**
  - **S – Single Responsibility**:
    - Cada classe com responsabilidade única (use case, adapter, mapper).
  - **O – Open/Closed**:
    - Fácil adicionar novo broker (RabbitMQ, SQS) via novos adapters/factory sem tocar no domínio.
  - **L – Liskov**:
    - Ports com implementações múltiplas (Kafka, InMemory) substituíveis.
  - **I – Interface Segregation**:
    - Ports enxutos (`EventPublisherPort`, `OrderRepositoryPort`).
  - **D – Dependency Inversion**:
    - Domínio depende de **interfaces**, adapters dependem de detalhes.

- **Boas práticas adicionais:**
  - DTOs como **Java Records** (imutáveis).
  - Injeção via `@RequiredArgsConstructor`.
  - Validação `@Valid` + Bean Validation.
  - Global Exception Handler + `ErrorResponse` record.

Onde revisar:
- `docs/HIGHLIGHTS-TECNOLOGIAS.md` – seção “Design Patterns e SOLID”.
- `docs/MELHORIAS-REGRAS-JAVA.md` – alinhamento com regras Java e Records.

---

## 8. Fechamento (1–2 minutos)

Sugestão de fechamento:

> “No fim, o projeto demonstra minha capacidade de:
> - Modelar domínio com DDD e Domain Events.
> - Aplicar Arquitetura Hexagonal + Saga + Kafka de forma coerente.
> - Garantir qualidade técnica via SOLID, testes, imutabilidade (Records) e boas práticas de produção (Virtual Threads, Resilience4j, Flyway).
>  
> É uma solução pensada não só para ‘rodar’, mas para ser escalável, observável e fácil de evoluir em um contexto de microserviços.”


