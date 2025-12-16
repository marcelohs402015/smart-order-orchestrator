# Smart Order Orchestrator - Backend

Backend do Smart Order Orchestrator construído com Spring Boot 3.3+ e Java 21.

## 🚀 Stack Tecnológica

- **Java 21** - Virtual Threads para alta concorrência
- **Spring Boot 3.3+** - Framework enterprise
- **PostgreSQL** - Banco relacional robusto
- **Resilience4j** - Circuit Breaker, Retry, Fallback
- **Kafka** - Event-driven architecture
- **OpenAI** - Análise de risco com IA
- **Flyway** - Migrations versionadas
- **Swagger/OpenAPI** - Documentação automática da API
- **JUnit 5 + Mockito** - Testes unitários e de integração

## 📁 Estrutura do Projeto

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/marcelo/orchestrator/
│   │   │   ├── domain/              # Camada de Domínio (Core)
│   │   │   │   ├── model/           # Entidades e Value Objects
│   │   │   │   ├── port/            # Interfaces (Ports)
│   │   │   │   └── event/           # Domain Events
│   │   │   ├── application/         # Camada de Aplicação
│   │   │   │   ├── usecase/         # Use Cases
│   │   │   │   └── saga/            # Saga Orchestrator
│   │   │   ├── infrastructure/      # Camada de Infraestrutura
│   │   │   │   ├── persistence/     # JPA, Repositories
│   │   │   │   ├── payment/         # AbacatePay Adapter
│   │   │   │   ├── ai/              # OpenAI Adapter
│   │   │   │   └── messaging/       # Kafka Adapter
│   │   │   └── presentation/        # Camada de Apresentação
│   │   │       ├── controller/      # REST Controllers
│   │   │       └── dto/             # DTOs
│   │   └── resources/
│   │       ├── application.yml      # Configuração principal
│   │       └── db/migration/       # Flyway migrations
│   └── test/                        # Testes
└── pom.xml                          # Dependências Maven
```

## 🏗️ Arquitetura

**Hexagonal Architecture (Ports and Adapters)** para isolar o domínio:

- **Domain:** Regras de negócio puras, sem dependências de frameworks
- **Application:** Casos de uso e orquestração (Saga Pattern)
- **Infrastructure:** Adaptadores para PostgreSQL, AbacatePay, OpenAI e Kafka
- **Presentation:** Controllers REST, DTOs, validações

## 📡 API Endpoints

### Orders
- `POST /api/v1/orders` - Criar pedido (executa saga completa)
- `GET /api/v1/orders` - Listar todos os pedidos
- `GET /api/v1/orders/{id}` - Buscar pedido por ID
- `GET /api/v1/orders/number/{orderNumber}` - Buscar pedido por número
- `POST /api/v1/orders/{id}/analyze-risk` - Análise manual de risco

### Payments
- `GET /api/v1/payments/{paymentId}/status` - Verificar status do pagamento
- `POST /api/v1/payments/orders/{orderId}/refresh-status` - Atualizar status do pagamento

### Actuator
- `GET /actuator/health` - Health check
- `GET /actuator/metrics` - Métricas do sistema
- `GET /actuator/circuitbreakers` - Status dos circuit breakers

### Swagger
- `GET /swagger-ui/index.html` - Documentação interativa da API

## 🚀 Como Rodar

### 1. Pré-requisitos

- Java 21+
- Maven 3.8+
- PostgreSQL 14+
- Docker (para PostgreSQL)

### 2. Iniciar PostgreSQL

```bash
cd scripts
docker-compose up -d
```

### 3. Configurar Variáveis de Ambiente

**Windows (PowerShell):**
```powershell
cd src/main/resources/variaveis
. .\environment.ps1
```

**Linux/Mac:**
```bash
cd src/main/resources/variaveis
source environment.sh
```

**Variáveis necessárias:**
- `DATABASE_URL` - URL do PostgreSQL (padrão: jdbc:postgresql://localhost:5432/smartorder)
- `DATABASE_USERNAME` - Usuário do banco (padrão: postgres)
- `DATABASE_PASSWORD` - Senha do banco (padrão: postgres)
- `ABACATEPAY_API_KEY` - Chave da API AbacatePay
- `OPENAI_API_KEY` - Chave da API OpenAI (opcional)
- `OPENAI_MODEL` - Modelo OpenAI (padrão: gpt-4o-mini)
- `KAFKA_BOOTSTRAP_SERVERS` - Servidores Kafka (opcional, padrão: localhost:9092)

### 4. Executar Aplicação

```bash
mvn spring-boot:run
```

**Acesso:**
- API REST: http://localhost:8080/api/v1/orders
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Health Check: http://localhost:8080/actuator/health

## 🧪 Testes

```bash
# Executar todos os testes
mvn test

# Executar testes com cobertura
mvn test jacoco:report

# Executar testes de integração
mvn verify
```

## 🔄 Fluxo Principal (Saga Pattern)

1. **Verificação de Idempotência** → Se `idempotencyKey` já existe, retorna resultado anterior
2. **Criar Pedido** → Status `PENDING` + Publica `OrderCreatedEvent`
3. **Processar Pagamento** (AbacatePay com Circuit Breaker)
   - Sucesso: Status `PAID` + Publica `PaymentProcessedEvent`
   - Falha: **Compensação Automática** → Status `CANCELED` + Publica `SagaFailedEvent`
4. **Analisar Risco** (OpenAI com Circuit Breaker) → `RISK_LOW` / `RISK_HIGH` / `PENDING`
5. **Conclusão** → Publica `SagaCompletedEvent` para outros serviços

## ✨ Features Principais

1. **Saga Pattern (Orchestration)** - Orquestração completa com compensação automática
2. **Idempotência** - Prevenção de duplicação via `idempotencyKey`
3. **Circuit Breaker (Resilience4j)** - Proteção contra falhas em cascata
4. **Retry com Backoff Exponencial** - Recuperação automática de falhas transitórias
5. **Event-Driven Architecture** - Publicação de eventos via Kafka
6. **Virtual Threads (Java 21)** - Alta concorrência com baixo consumo de memória
7. **OpenAI Integration** - Análise inteligente de risco de pagamento
8. **Observabilidade Completa** - Rastreamento de cada execução

## 📊 Configurações Importantes

### Circuit Breaker (Resilience4j)

Configurado em `application.yml`:
- **Sliding Window Size:** 10 requisições
- **Failure Rate Threshold:** 50%
- **Wait Duration:** 60 segundos
- **Retry:** 3 tentativas com backoff exponencial

### Virtual Threads

Habilitado por padrão no Spring Boot 3.3+ com Java 21. Configuração em `application.yml`:
```yaml
spring:
  threads:
    virtual:
      enabled: true
```

### Feature Flags

- `features.riskAnalysis.enabled` - Habilita/desabilita análise de risco com OpenAI

## 🔒 Segurança

- Validação de entrada com Bean Validation
- Tratamento de erros global com exception handlers
- Logs estruturados para auditoria
- Secrets gerenciados via variáveis de ambiente

## 📖 Documentação Adicional

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **Actuator:** http://localhost:8080/actuator

