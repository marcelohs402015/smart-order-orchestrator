# Guia Completo de Testes - Smart Order Orchestrator

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Como Executar Testes](#como-executar-testes)
3. [Tipos de Testes](#tipos-de-testes)
4. [Estrutura de Testes](#estrutura-de-testes)
5. [Testando Cada Camada](#testando-cada-camada)
6. [Testando a Aplicação Completa](#testando-a-aplicação-completa)
7. [Configuração de Ambiente de Teste](#configuração-de-ambiente-de-teste)
8. [CI/CD e Testes Automatizados](#cicd-e-testes-automatizados)
9. [Boas Práticas](#boas-práticas)

---

## 🎯 Visão Geral

Este projeto utiliza uma estratégia de testes em **pirâmide de testes**, priorizando testes unitários (base) e complementando com testes de integração e end-to-end.

### Stack de Testes

- **JUnit 5**: Framework de testes (incluído no `spring-boot-starter-test`)
- **Mockito**: Framework de mocking para testes unitários
- **Spring Boot Test**: Suporte para testes de integração
- **H2 Database**: Banco in-memory para testes (sem necessidade de Docker)

### Cobertura Atual

- ✅ **Testes Unitários**: Domain, Application, Infrastructure
- ✅ **Testes de Integração**: Adapters, Repositories
- ✅ **Testes de Saga**: Orquestração completa
- ⚠️ **Testes End-to-End**: A ser implementado (REST API)

---

## 🚀 Como Executar Testes

### Executar Todos os Testes

```bash
cd backend
mvn test
```

### Executar Testes de uma Classe Específica

```bash
mvn test -Dtest=OrderTest
```

### Executar Testes de um Pacote

```bash
mvn test -Dtest=com.marcelo.orchestrator.domain.model.*
```

### Executar Testes com Cobertura (JaCoCo - se configurado)

```bash
mvn clean test jacoco:report
```

### Executar Apenas Testes de Integração

```bash
mvn test -Dtest=*IntegrationTest
```

### Executar Testes em Modo Verbose

```bash
mvn test -X
```

### Executar Testes Específicos via IDE

**IntelliJ IDEA:**
1. Clique com botão direito na classe de teste
2. Selecione "Run 'ClassNameTest'"
3. Ou use atalho: `Ctrl+Shift+F10` (Windows/Linux) ou `Cmd+Shift+R` (Mac)

**VS Code:**
1. Instale extensão "Java Test Runner"
2. Clique no ícone de "Run Test" acima do método de teste

---

## 📊 Tipos de Testes

### 1. Testes Unitários

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

### 2. Testes de Integração

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

### 3. Testes End-to-End (E2E)

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

## 📁 Estrutura de Testes

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

### Convenções de Nomenclatura

- **Testes Unitários**: `*Test.java` (ex: `OrderTest.java`)
- **Testes de Integração**: `*IntegrationTest.java` ou `*AdapterTest.java`
- **Testes E2E**: `*E2ETest.java` (futuro)

---

## 🧪 Testando Cada Camada

### 1. Camada Domain (Modelos de Domínio)

**Objetivo:** Testar regras de negócio encapsuladas nas entidades.

**Testes Existentes:**
- ✅ `OrderTest.java` - Cálculo de total, transições de status
- ✅ `OrderStatusTest.java` - Validação de transições de estado
- ✅ `MoneyTest.java` - Operações matemáticas com valores monetários

**Como Executar:**
```bash
mvn test -Dtest=com.marcelo.orchestrator.domain.model.*
```

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

---

### 2. Camada Application (Use Cases)

**Objetivo:** Testar orquestração de casos de uso.

**Testes Existentes:**
- ✅ `AnalyzeRiskUseCaseTest.java` - Análise de risco
- ⚠️ `CreateOrderUseCaseTest.java` - A ser implementado
- ⚠️ `ProcessPaymentUseCaseTest.java` - A ser implementado

**Como Executar:**
```bash
mvn test -Dtest=com.marcelo.orchestrator.application.usecase.*
```

**Estratégia:**
- **Mocks:** Portas (interfaces) são mockadas
- **Isolamento:** Testa apenas lógica do use case
- **Cenários:** Sucesso, falha, validação

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

**O que testar:**
- ✅ Orquestração completa do use case
- ✅ Validação de entrada
- ✅ Integração com portas (mocks)
- ✅ Tratamento de erros
- ✅ Persistência de resultados

---

### 3. Camada Application (Saga Pattern)

**Objetivo:** Testar orquestração de transações distribuídas.

**Testes Existentes:**
- ✅ `OrderSagaOrchestratorTest.java` - Orquestração completa da saga

**Como Executar:**
```bash
mvn test -Dtest=OrderSagaOrchestratorTest
```

**Estratégia:**
- **Mocks:** Use cases são mockados
- **Validação:** Ordem de execução, compensação, rastreamento

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

---

### 4. Camada Infrastructure (Persistence)

**Objetivo:** Testar adaptadores de persistência.

**Testes Existentes:**
- ✅ `OrderRepositoryAdapterTest.java` - Conversão domínio ↔ JPA

**Como Executar:**
```bash
mvn test -Dtest=OrderRepositoryAdapterTest
```

**Estratégia:**
- **Mocks:** JPA Repository é mockado
- **Validação:** Conversão entre domínio e JPA

**Exemplo de Teste:**
```java
@Test
void shouldSaveOrder() {
    // Arrange
    Order order = createTestOrder();
    OrderEntity entity = orderMapper.toEntity(order);
    
    // Act
    when(jpaOrderRepository.save(any())).thenReturn(entity);
    Order saved = adapter.save(order);
    
    // Assert
    assertNotNull(saved);
    verify(jpaOrderRepository).save(any());
}
```

**O que testar:**
- ✅ Conversão domínio → JPA
- ✅ Conversão JPA → domínio
- ✅ Operações CRUD
- ✅ Tratamento de erros de persistência

---

### 5. Camada Infrastructure (Payment Gateway)

**Objetivo:** Testar integração com gateway de pagamento.

**Testes Existentes:**
- ✅ `AbacatePayAdapterTest.java` - Integração com AbacatePay

**Como Executar:**
```bash
mvn test -Dtest=AbacatePayAdapterTest
```

**Estratégia:**
- **Mocks:** WebClient é mockado (não faz chamadas HTTP reais)
- **Validação:** Conversão DTO, tratamento de erros, Circuit Breaker

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

---

### 6. Camada Infrastructure (AI Integration)

**Objetivo:** Testar integração com OpenAI.

**Testes Existentes:**
- ✅ `OpenAIRiskAnalysisAdapterTest.java` - Integração com OpenAI

**Como Executar:**
```bash
mvn test -Dtest=OpenAIRiskAnalysisAdapterTest
```

**Estratégia:**
- **Mocks:** WebClient é mockado
- **Validação:** Construção de prompt, parsing de resposta, fallback

**Exemplo de Teste:**
```java
@Test
void shouldAnalyzeRiskAsLow() {
    // Arrange
    RiskAnalysisRequest request = createRequest();
    OpenAIResponse response = createLowRiskResponse();
    
    // Act
    when(webClient.post()).thenReturn(responseSpec);
    RiskAnalysisResult result = adapter.analyzeRisk(request);
    
    // Assert
    assertEquals(RiskLevel.LOW, result.getRiskLevel());
}
```

**O que testar:**
- ✅ Construção de prompt estruturado
- ✅ Parsing de resposta (LOW/HIGH)
- ✅ Tratamento de erros (401, 500, timeout)
- ✅ Fallback gracioso (retorna PENDING)
- ✅ Circuit Breaker

---

## 🔄 Testando a Aplicação Completa

### 1. Teste Manual via Swagger UI

**Passo a Passo:**

1. **Iniciar Aplicação:**
```bash
cd backend
mvn spring-boot:run
```

2. **Acessar Swagger UI:**
   - URL: http://localhost:8080/swagger-ui.html
   - Documentação interativa da API

3. **Criar Pedido:**
   - Endpoint: `POST /api/v1/orders`
   - Body:
   ```json
   {
     "customerId": "550e8400-e29b-41d4-a716-446655440000",
     "customerName": "João Silva",
     "customerEmail": "joao@example.com",
     "items": [
       {
         "productId": "660e8400-e29b-41d4-a716-446655440001",
         "productName": "Produto 1",
         "quantity": 2,
         "unitPrice": 10.50
       }
     ]
   }
   ```
   - Verificar resposta: Status `201 Created`, pedido com status `PENDING`

4. **Verificar Pedido:**
   - Endpoint: `GET /api/v1/orders/{id}`
   - Verificar status e dados do pedido

5. **Listar Pedidos:**
   - Endpoint: `GET /api/v1/orders`
   - Verificar lista de pedidos

### 2. Teste via cURL

**Criar Pedido:**
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "customerName": "João Silva",
    "customerEmail": "joao@example.com",
    "items": [
      {
        "productId": "660e8400-e29b-41d4-a716-446655440001",
        "productName": "Produto 1",
        "quantity": 2,
        "unitPrice": 10.50
      }
    ]
  }'
```

**Buscar Pedido:**
```bash
curl http://localhost:8080/api/v1/orders/{orderId}
```

**Listar Pedidos:**
```bash
curl http://localhost:8080/api/v1/orders
```

### 3. Teste de Health Check

```bash
curl http://localhost:8080/actuator/health
```

**Resposta Esperada:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    }
  }
}
```

### 4. Teste de Métricas

```bash
curl http://localhost:8080/actuator/metrics
```

---

## ⚙️ Configuração de Ambiente de Teste

### Perfil de Teste (application-test.yml)

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

### H2 Database (In-Memory)

**Vantagens:**
- Não precisa de Docker
- Rápido para testes
- Isolado (cada teste tem seu próprio banco)

**Configuração Automática:**
- Spring Boot detecta H2 no classpath
- Cria banco in-memory automaticamente
- Limpa dados entre testes

### Variáveis de Ambiente para Testes

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

## 🔄 CI/CD e Testes Automatizados

### GitHub Actions

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

### Executar Testes Localmente (Como no CI)

```bash
# Compilar
mvn clean compile -DskipTests

# Executar testes
mvn test
```

---

## ✅ Boas Práticas

### 1. Nomenclatura de Testes

**Use:** `should[ExpectedBehavior]When[StateUnderTest]`

**Exemplo:**
```java
@Test
@DisplayName("Deve calcular total corretamente quando pedido tem múltiplos itens")
void shouldCalculateTotalCorrectlyWhenOrderHasMultipleItems() {
    // ...
}
```

### 2. Estrutura AAA (Arrange-Act-Assert)

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

### 3. Testes Isolados

- Cada teste deve ser independente
- Não compartilhar estado entre testes
- Usar `@BeforeEach` para setup comum

### 4. Mocks Apropriados

- **Mock:** Dependências externas (HTTP, Database)
- **Não Mock:** Objetos de domínio (Value Objects, Entities)

### 5. Cobertura de Testes

**Foco em:**
- Regras de negócio (Domain)
- Orquestração (Use Cases)
- Integrações críticas (Adapters)

**Não precisa testar:**
- Getters/Setters (Lombok)
- Mappers simples (MapStruct)
- DTOs simples

### 6. Testes Rápidos

- Testes unitários: < 100ms cada
- Testes de integração: < 1s cada
- Suite completa: < 30s

---

## 📚 Links e Referências

### Documentação de Fases

- [Fase 1: Fundação e Estrutura](FASE1-FUNDACAO-ESTRUTURA.md) - Configuração inicial
- [Fase 2: Camada Domain](FASE2-CAMADA-DOMAIN.md) - Modelos de domínio
- [Fase 3: Camada Application](FASE3-CAMADA-APPLICATION.md) - Use cases
- [Fase 4: Infrastructure - Persistência](FASE4-CAMADA-INFRASTRUCTURE-PERSISTENCIA.md) - JPA e Repositories
- [Fase 6: Integração OpenAI](README-OPENAI.md) - Testes específicos de IA
- [Fase 7: Saga Pattern](FASE7-SAGA-PATTERN.md) - Testes de orquestração

### Documentação Técnica

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)

---

## 🎯 Checklist de Testes

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

**Última Atualização:** 2024

