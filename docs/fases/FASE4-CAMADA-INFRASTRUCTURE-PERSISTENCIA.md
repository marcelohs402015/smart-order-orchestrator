# Fase 4: Camada Infrastructure - Persistência

## 🎯 Objetivo

Implementar adaptadores de persistência usando JPA/Hibernate e PostgreSQL, mantendo o domínio independente de tecnologias de persistência.

## ✅ Entregas

### 1. Entidades JPA

#### OrderEntity

**Características:**
- **Entidade JPA Separada**: Diferente da entidade de domínio (`Order`)
- **Anotações JPA**: `@Entity`, `@Table`, `@OneToMany`, etc.
- **Índices**: Otimizados para consultas frequentes

**Campos Principais:**
- `id`: UUID (evita problemas de sequência em sistemas distribuídos)
- `orderNumber`: String único e indexado
- `status`: Enum salvo como String
- `items`: Relacionamento One-to-Many com cascade
- `version`: Controle de concorrência otimista (Optimistic Locking)

**Por que Entidade JPA Separada?**
- **Separação de Concerns**: Persistência vs. Regras de Negócio
- **Independência do Domínio**: Domínio não conhece JPA
- **Flexibilidade**: Pode ter campos diferentes (ex: campos técnicos de auditoria)
- **Otimização**: Pode otimizar para banco (índices, constraints) sem afetar domínio

#### OrderItemEntity

**Características:**
- Relacionamento Many-to-One com OrderEntity
- Cascade: Salvar/carregar itens junto com pedido

#### SagaExecutionEntity

**Características:**
- Rastreamento de execuções de saga
- Relacionamento One-to-Many com SagaStepEntity
- Campos: status, currentStep, errorMessage, durationMs

#### SagaStepEntity

**Características:**
- Histórico detalhado de cada passo da saga
- Campos: stepName, status, startedAt, completedAt, durationMs, errorMessage

### 2. Repositories (Spring Data JPA)

#### JpaOrderRepository

**Características:**
- Interface Spring Data JPA
- Métodos automáticos: `save()`, `findById()`, `findAll()`, `deleteById()`
- Query Methods: `findByOrderNumber()`, `findByStatus()`

**Por que Spring Data JPA?**
- **Produtividade**: Menos código boilerplate
- **Type Safety**: Queries baseadas em métodos
- **Flexibilidade**: Pode usar JPQL, SQL nativo, ou convenções

#### JpaSagaExecutionRepository

**Características:**
- Métodos customizados: `findByOrderId()`, `findFirstByOrderIdOrderByStartedAtDesc()`, `findByStatus()`

### 3. Adapters (Ports and Adapters)

#### OrderRepositoryAdapter

**Responsabilidades:**
- Implementa `OrderRepositoryPort` (definida no domínio)
- Converte entre `Order` (domínio) e `OrderEntity` (JPA)
- Chama `JpaOrderRepository` (Spring Data JPA)

**Fluxo:**
1. Recebe `Order` (domínio)
2. Converte para `OrderEntity` (JPA) usando MapStruct
3. Salva usando `JpaOrderRepository`
4. Converte de volta para `Order` (domínio)

**Por que Adapter?**
- **Ports and Adapters**: Implementa porta definida no domínio
- **Inversão de Dependência**: Domínio não conhece esta implementação
- **Testabilidade**: Fácil mockar para testes

### 4. Mappers (Manuais - Spring Components)

#### OrderPersistenceMapper

**Características:**
- Classe `@Component` com injeção explícita de dependências
- Métodos: `toEntity()`, `toDomain()`, `updateEntity()`, `mapItemsToEntity()`, `mapItemsToDomain()`
- Mapeamento manual com controle total sobre lógica
- Mapeamento customizado para `items` (relacionamento One-to-Many)

**Por que Mappers Manuais?**
- **Dependency Inversion (SOLID)**: Injeção explícita, controle total
- **Arquitetura Hexagonal**: Alinhado com princípios de inversão de controle
- **Testabilidade**: Fácil mockar e testar
- **Manutenibilidade**: Código explícito e fácil de entender

**Estratégia de Mapeamento:**
- Campos simples: Mapeamento direto
- `items`: Mapeamento customizado com `@AfterMapping`
- `version`: Ignorado no mapeamento para domínio

### 5. Migrations (Flyway)

#### V1__create_orders_table.sql

**Tabelas Criadas:**
- `orders`: Tabela principal de pedidos
- `order_items`: Tabela de itens de pedido

**Características:**
- UUID como chave primária
- Índices para performance
- Constraints de foreign key
- Comentários para documentação

#### V2__create_saga_tables.sql

**Tabelas Criadas:**
- `saga_executions`: Rastreamento de execuções de saga
- `saga_steps`: Histórico detalhado de cada passo

**Características:**
- Relacionamento One-to-Many
- Índices para consultas rápidas
- Campos de rastreamento (timestamps, duration)

**Por que Flyway?**
- **Versionamento**: Migrations versionadas e rastreáveis
- **Reprodutibilidade**: Mesmo schema em todos os ambientes
- **Rollback**: Pode reverter migrations se necessário
- **Auditoria**: Histórico completo de mudanças no schema

### 6. Configuração JPA

#### JpaConfig

**Anotações:**
- `@EnableJpaRepositories`: Habilita repositórios Spring Data JPA
- `@EnableJpaAuditing`: Habilita auditoria JPA
- `@EnableTransactionManagement`: Habilita gerenciamento de transações

**Configurações:**
- Base package para repositórios
- Auditoria habilitada para `@CreatedDate`, `@LastModifiedDate`

## 🏗️ Arquitetura

### Fluxo de Persistência

```
Use Case
  ↓
OrderRepositoryPort (interface no domínio)
  ↓
OrderRepositoryAdapter (implementação)
  ↓
OrderMapper (conversão)
  ↓
OrderEntity (JPA)
  ↓
JpaOrderRepository (Spring Data JPA)
  ↓
PostgreSQL
```

### Separação de Camadas

1. **Domain**: Define `OrderRepositoryPort` (interface)
2. **Application**: Usa `OrderRepositoryPort` (não conhece implementação)
3. **Infrastructure**: Implementa `OrderRepositoryAdapter` (conhece JPA)

## 📦 Estrutura de Pacotes

```
infrastructure/persistence/
├── entity/
│   ├── OrderEntity.java
│   ├── OrderItemEntity.java
│   ├── SagaExecutionEntity.java
│   └── SagaStepEntity.java
├── repository/
│   ├── JpaOrderRepository.java
│   └── JpaSagaExecutionRepository.java
├── adapter/
│   └── OrderRepositoryAdapter.java
├── mapper/
│   └── OrderMapper.java
└── config/
    └── JpaConfig.java
```

## 🔧 Configurações

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smartorder
    username: postgres
    password: postgres
  
  jpa:
    hibernate:
      ddl-auto: validate  # Flyway gerencia schema
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

## ✅ Critérios de Conclusão

- [x] CRUD funcionando
- [x] Persistência isolada da camada de domínio
- [x] Migrations versionadas (Flyway)
- [x] Mapeamento entre domínio e JPA (MapStruct)
- [x] Testes de integração com banco
- [x] Controle de concorrência otimista

## 📚 Próximos Passos

- **Fase 5**: Implementar camada Infrastructure (gateway de pagamento)
- **Fase 6**: Implementar camada Infrastructure (integração OpenAI)

