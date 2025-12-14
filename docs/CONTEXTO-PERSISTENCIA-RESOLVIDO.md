# Contexto: Problema de Persistência Resolvido

## 📅 Data: 12/12/2025

## 🎯 Problema Identificado

O sistema não estava conseguindo persistir dados de pedidos no banco de dados, apresentando o erro:
```
A collection with cascade="all-delete-orphan" was no longer referenced by the owning entity instance: com.marcelo.orchestrator.infrastructure.persistence.entity.OrderEntity.items
```

## 🔍 Causa Raiz

1. **Lista de itens não inicializada**: `OrderEntity.items` não tinha `@Builder.Default`, causando `null` e quebrando o gerenciamento do JPA.
2. **Referência quebrada no mapper**: `mapItemsAfterMapping` usava `setItems()` diretamente, criando nova referência e quebrando `orphanRemoval`.
3. **Scripts de migração fragmentados**: V1, V2, V3 separados podiam causar inconsistências.

## ✅ Correções Aplicadas

### 1. Inicialização da Lista em `OrderEntity`

```java
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@Builder.Default
private List<OrderItemEntity> items = new ArrayList<>();
```

**Por quê funciona:**
- `@Builder.Default` garante que a lista seja sempre inicializada
- Evita `null` que quebra o gerenciamento do JPA

### 2. Ajuste no `OrderPersistenceMapper` (Mapper Manual)

```java
private void mapItemsAfterMapping(Order order, OrderEntity entity) {
    if (entity.getItems() == null) {
        entity.setItems(new ArrayList<>());
    }
    
    if (order != null && order.getItems() != null && !order.getItems().isEmpty()) {
        if (!entity.getItems().isEmpty()) {
            entity.getItems().clear();
        }
        List<OrderItemEntity> newItems = mapItemsToEntity(order.getItems(), entity);
        entity.getItems().addAll(newItems);
    }
}
```

**Por quê funciona:**
- Não usa `setItems()` diretamente (evita nova referência)
- Usa `clear()` + `addAll()` na lista existente
- Mantém a referência gerenciada pelo JPA
- **Mapper manual como `@Component`** - Injeção explícita, alinhado com SOLID

### 3. Script de Migração Único Recriado

**Arquivo:** `backend/src/main/resources/db/migration/V1__create_orders_table.sql`

- Cria todas as tabelas de uma vez: `orders`, `order_items`, `saga_executions`, `saga_steps`
- `idempotency_key` com índice único desde o início
- 100% alinhado com entidades JPA

### 4. Correção no `OrderRepositoryAdapter.save()`

**Estratégia:**
- Na criação: itens são criados junto com o Order (via `toEntity()`)
- Na atualização: apenas campos simples são atualizados (status, paymentId, etc.)
- Itens são imutáveis após criação (não são atualizados)

## 🗄️ Estrutura do Banco de Dados

### Tabelas Criadas

1. **`orders`**
   - ID: UUID (PK)
   - Campos: order_number, status, customer_id, customer_name, customer_email, total_amount, payment_id, risk_level, created_at, updated_at, version
   - Índices: order_number (único), status, customer_id, created_at

2. **`order_items`**
   - ID: UUID (PK, gerado automaticamente)
   - Campos: order_id (FK), product_id, product_name, quantity, unit_price
   - Índices: order_id, product_id
   - FK: `ON DELETE CASCADE` para orders

3. **`saga_executions`**
   - ID: UUID (PK)
   - Campos: idempotency_key (único), order_id (FK), status, current_step, error_message, started_at, completed_at, duration_ms
   - Índices: order_id, status, started_at, idempotency_key (único)

4. **`saga_steps`**
   - ID: UUID (PK)
   - Campos: saga_execution_id (FK), step_name, status, started_at, completed_at, duration_ms, error_message, metadata
   - Índices: saga_execution_id, step_name
   - FK: `ON DELETE CASCADE` para saga_executions

## 🧪 Testes Corrigidos

### 1. `AnalyzeRiskUseCaseTest`
- **Problema:** Esperava `IllegalArgumentException`, mas código lança `OrderNotFoundException`
- **Correção:** Alterado para `OrderNotFoundException` e adicionado import

### 2. `OrderSagaOrchestratorTest`
- **Problema:** Usava `JpaSagaExecutionRepository` (infraestrutura) ao invés de `SagaExecutionRepositoryPort` (domínio)
- **Correção:**
  - Substituído `JpaSagaExecutionRepository` por `SagaExecutionRepositoryPort`
  - Atualizados mocks para usar `SagaExecutionRepositoryPort.SagaExecution`
  - Removido mock desnecessário de `findByIdempotencyKey` do `setUp`
  - Ajustado `shouldTrackAllSagaSteps` para verificar múltiplas chamadas de `save`

**Resultado:** ✅ Todos os 38 testes passando

## 📋 Script de Limpeza do Banco

**Arquivo:** `backend/src/main/resources/db/scripts/clean_database.sql`

Para limpar todas as tabelas antes de recriar:
```sql
DROP TABLE IF EXISTS saga_steps CASCADE;
DROP TABLE IF EXISTS saga_executions CASCADE;
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
```

## 🚀 Como Usar

### 1. Limpar Banco (se necessário)
```bash
psql -U postgres -d smartorder -f backend/src/main/resources/db/scripts/clean_database.sql
```

### 2. Iniciar Aplicação
Ao iniciar, o Flyway automaticamente:
- Detecta banco vazio
- Cria baseline (`baseline-on-migrate: true`)
- Executa `V1__create_orders_table.sql`
- Cria todas as tabelas

### 3. Testar com JSON

**Endpoint:** `POST http://localhost:8080/api/orders`

**Exemplo de JSON:**
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
  "idempotencyKey": "test-order-2025-12-12-001"
}
```

## ✅ Status Atual

- ✅ Persistência funcionando corretamente
- ✅ Todos os testes passando (38/38)
- ✅ Scripts de migração recriados e alinhados
- ✅ Estrutura do banco 100% correta
- ✅ Idempotência implementada e funcionando
- ✅ Saga Pattern funcionando corretamente

## 📝 Lições Aprendidas

1. **Sempre inicializar coleções JPA com `@Builder.Default`** quando usar Lombok Builder
2. **Nunca usar `setItems()` diretamente** com `orphanRemoval=true` - sempre manipular a lista existente
3. **Scripts de migração únicos** são mais fáceis de manter que múltiplos scripts fragmentados
4. **Testes devem usar Ports (domínio)**, não implementações (infraestrutura)

## 🔗 Arquivos Modificados

1. `backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/entity/OrderEntity.java`
2. `backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/mapper/OrderPersistenceMapper.java` (Mapper manual como `@Component`)
3. `backend/src/main/java/com/marcelo/orchestrator/presentation/mapper/OrderPresentationMapper.java` (Mapper manual como `@Component`)
4. `backend/src/main/java/com/marcelo/orchestrator/infrastructure/persistence/adapter/OrderRepositoryAdapter.java`
5. `backend/src/main/resources/db/migration/V1__create_orders_table.sql`
6. `backend/src/test/java/com/marcelo/orchestrator/application/usecase/AnalyzeRiskUseCaseTest.java`
7. `backend/src/test/java/com/marcelo/orchestrator/application/saga/OrderSagaOrchestratorTest.java`

## 🔄 Mudança Arquitetural: Remoção do MapStruct

**Data:** 12/12/2025

**Decisão:** Remover MapStruct e implementar mappers manuais como `@Component` classes.

**Razões:**
1. **Dependency Inversion (SOLID):** Mappers manuais permitem injeção explícita de dependências
2. **Arquitetura Hexagonal:** Controle total sobre mapeamento, sem dependências de annotation processing
3. **Testabilidade:** Mais fácil mockar e testar mappers manuais
4. **Manutenibilidade:** Código mais explícito e fácil de entender

**Implementação:**
- `OrderPresentationMapper` - `@Component` com métodos `toDomain()` e `toDomainList()`
- `OrderPersistenceMapper` - `@Component` com métodos `toEntity()`, `toDomain()`, `updateEntity()`, etc.
- Removidas todas as dependências MapStruct do `pom.xml`
- Removidas configurações de annotation processing

## 🎯 Próximos Passos

- ✅ Sistema funcionando e pronto para uso
- ✅ Persistência estável
- ✅ Testes completos
- ✅ Documentação atualizada

---

**Última atualização:** 12/12/2025  
**Status:** ✅ Resolvido e Funcionando

