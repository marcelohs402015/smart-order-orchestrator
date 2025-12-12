# Changelog - Integração Frontend-Backend

## [11/12/2024] - Correção de Status de Pagamento

### ✅ Adicionado

#### Backend
- **Endpoint para buscar pedidos por status:** `GET /api/v1/orders?status={status}`
  - Suporta filtro por: `PENDING`, `PAID`, `PAYMENT_FAILED`, `CANCELED`
  - Documentação Swagger completa
  - Parâmetro `status` opcional (retorna todos se não fornecido)

### 🔧 Corrigido

- **Status PAYMENT_FAILED não persistido:** 
  - Problema: Status era sobrescrito para `CANCELED` durante compensação da saga
  - Solução: Método `compensate()` agora mantém `PAYMENT_FAILED` quando pagamento falha
  - Impacto: Frontend pode identificar corretamente falhas de pagamento

- **Teste unitário desatualizado:**
  - `OrderSagaOrchestratorTest.shouldCompensateWhenPaymentFails` atualizado
  - Agora valida que status `PAYMENT_FAILED` é mantido (não muda para `CANCELED`)

### 📝 Modificado

- `backend/.../OrderSagaOrchestrator.java`
  - Método `compensate()`: Lógica para manter `PAYMENT_FAILED` quando pagamento falha
  - Diferenciação entre falhas de pagamento e outros tipos de cancelamento

- `backend/.../OrderController.java`
  - Método `getAllOrders()`: Adicionado parâmetro opcional `status` para filtro
  - Documentação Swagger expandida

- `backend/.../OrderSagaOrchestratorTest.java`
  - Teste `shouldCompensateWhenPaymentFails`: Atualizado para novo comportamento
  - Comentários explicativos adicionados

### 🎯 Resultado

- ✅ Status `PAYMENT_FAILED` corretamente persistido na base de dados
- ✅ Endpoint disponível para frontend consultar pedidos por status
- ✅ Todos os testes passando (38/38)
- ✅ Código alinhado com padrão Saga e arquitetura hexagonal

---

## [2024] - Integração Completa

### ✅ Adicionado

#### Frontend
- Suporte a `idempotencyKey` em `CreateOrderRequest`
- Campo `status`, `error` e `details` em `ApiError`
- Tratamento de status HTTP 202 (saga em progresso)
- Campo `validationErrors` no store Zustand
- Busca por número de pedido na `OrdersListPage`
- Validação de UUID no frontend (`isValidUUID`)
- Mensagens de erro específicas por status HTTP
- Botão "Tentar Novamente" em páginas com erro
- Suporte a `helperText` no componente `Input`
- Acessibilidade melhorada no `OrderCard` (keyboard navigation)

#### Backend
- Query `findAllWithItems()` com JOIN FETCH
- Query `findByIdWithItems()` com JOIN FETCH
- Queries customizadas com JOIN FETCH para:
  - `findByOrderNumber()`
  - `findByStatus()`
  - `findByCustomerId()`

### 🔧 Corrigido

- **LazyInitializationException:** Queries agora carregam items junto com pedidos
- **Proxy do Vite:** Removido `rewrite` que removia `/api` do path
- **Mensagens de erro:** Agora específicas e informativas
- **Build TypeScript:** Erros de tipo corrigidos
- **OrderCard onClick:** Refatorado para melhor acessibilidade

### 📝 Modificado

- `frontend/src/types/index.ts` - Types atualizados
- `frontend/src/lib/axios.ts` - Tratamento de erros melhorado
- `frontend/src/services/orderService.ts` - JSDoc expandido
- `frontend/src/store/orderStore.ts` - Estados intermediários
- `frontend/src/pages/*.tsx` - Todas as páginas melhoradas
- `frontend/src/components/*.tsx` - Componentes atualizados
- `frontend/vite.config.ts` - Proxy corrigido
- `backend/.../JpaOrderRepository.java` - Queries com JOIN FETCH
- `backend/.../OrderRepositoryAdapter.java` - Uso de queries otimizadas

### 🎯 Resultado

- ✅ Integração frontend-backend completa
- ✅ Erro 500 resolvido (lazy loading)
- ✅ Mensagens de erro informativas
- ✅ Build sem erros
- ✅ Melhor UX e acessibilidade

