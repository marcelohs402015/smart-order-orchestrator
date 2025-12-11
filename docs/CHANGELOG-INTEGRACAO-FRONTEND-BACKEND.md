# Changelog - Integração Frontend-Backend

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

