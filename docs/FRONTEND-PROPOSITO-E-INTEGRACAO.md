# Frontend - Propósito e Integração com Backend

## 🎯 Propósito do Frontend

### Smart Order Orchestrator - Interface Web

**O que é:**
Interface web moderna e responsiva para interagir com o sistema de orquestração de pedidos. Permite que usuários criem, visualizem e gerenciem pedidos através de uma experiência de usuário intuitiva.

**Problema de Negócio que Resolve:**
- **Interface Amigável:** Usuários não precisam usar APIs REST diretamente
- **Visualização Clara:** Dashboard com estatísticas e status dos pedidos
- **Criação Simplificada:** Formulário intuitivo para criar pedidos
- **Rastreamento:** Visualização completa do ciclo de vida do pedido (criação → pagamento → análise de risco)

**Cenário de Uso Real:**
1. Usuário acessa o dashboard e vê estatísticas gerais
2. Cria um novo pedido através do formulário
3. Sistema executa saga completa (criação → pagamento → análise de risco)
4. Usuário visualiza resultado e detalhes do pedido
5. Pode listar e filtrar todos os pedidos

---

## 🔗 Integração com Backend

### Arquitetura de Comunicação

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (React)                      │
│  ┌──────────────┐         ┌──────────────┐             │
│  │   Pages      │────────▶│   Services   │             │
│  │  (UI)        │         │   (Axios)    │             │
│  └──────────────┘         └──────┬───────┘             │
│         │                         │                      │
│         ▼                         ▼                      │
│  ┌──────────────┐         ┌──────────────┐             │
│  │   Store      │         │   Types      │             │
│  │  (Zustand)   │         │ (TypeScript) │             │
│  └──────────────┘         └──────────────┘             │
└─────────────────────────────────────────────────────────┘
                            │
                            │ HTTP REST
                            ▼
┌─────────────────────────────────────────────────────────┐
│              Backend (Spring Boot)                      │
│  ┌──────────────┐         ┌──────────────┐             │
│  │ Controllers │────────▶│   Use Cases  │             │
│  │   (REST)    │         │   (Saga)     │             │
│  └──────────────┘         └──────────────┘             │
└─────────────────────────────────────────────────────────┘
```

### Fluxo de Dados

#### 1. Criar Pedido

```
Frontend (CreateOrderPage)
  ↓
useOrderStore.createOrder()
  ↓
orderService.createOrder()
  ↓
Axios POST /api/v1/orders
  ↓
Backend (OrderController)
  ↓
OrderSagaOrchestrator.execute()
  ↓
Response (CreateOrderResponse)
  ↓
Store atualiza estado
  ↓
UI atualiza (redirect para lista)
```

#### 2. Listar Pedidos

```
Frontend (OrdersListPage)
  ↓
useOrderStore.fetchOrders()
  ↓
orderService.getAllOrders()
  ↓
Axios GET /api/v1/orders
  ↓
Backend (OrderController)
  ↓
OrderRepositoryPort.findAll()
  ↓
Response (List<OrderResponse>)
  ↓
Store atualiza orders[]
  ↓
UI renderiza cards
```

#### 3. Visualizar Detalhes

```
Frontend (OrderDetailPage)
  ↓
useOrderStore.fetchOrderById(id)
  ↓
orderService.getOrderById(id)
  ↓
Axios GET /api/v1/orders/{id}
  ↓
Backend (OrderController)
  ↓
OrderRepositoryPort.findById(id)
  ↓
Response (OrderResponse)
  ↓
Store atualiza currentOrder
  ↓
UI exibe detalhes completos
```

---

## 🏗️ Arquitetura Frontend

### Camadas

#### 1. **Presentation Layer (Pages)**
- **Responsabilidade:** UI e interação do usuário
- **Componentes:** `DashboardPage`, `OrdersListPage`, `CreateOrderPage`, `OrderDetailPage`
- **Tecnologias:** React, React Router, TailwindCSS

#### 2. **State Management Layer (Store)**
- **Responsabilidade:** Gerenciamento de estado global
- **Componentes:** `orderStore.ts` (Zustand)
- **Tecnologias:** Zustand

#### 3. **Service Layer (API)**
- **Responsabilidade:** Comunicação com backend
- **Componentes:** `orderService.ts`
- **Tecnologias:** Axios

#### 4. **Domain Layer (Types)**
- **Responsabilidade:** Tipos TypeScript e contratos
- **Componentes:** `types/index.ts`
- **Tecnologias:** TypeScript

#### 5. **Infrastructure Layer (Lib)**
- **Responsabilidade:** Configurações de bibliotecas
- **Componentes:** `lib/axios.ts`
- **Tecnologias:** Axios, configurações

---

## 🚀 Stack Tecnológica

### Por que estas escolhas?

#### 1. **React 18+**
- **Por quê:** Biblioteca UI moderna, performática e amplamente adotada
- **Benefício:** Grande ecossistema, fácil encontrar desenvolvedores, suporte a hooks modernos
- **Alinhamento:** Usado em projetos enterprise modernos

#### 2. **Vite**
- **Por quê:** Build tool extremamente rápido (ESM nativo)
- **Benefício:** HMR instantâneo, build otimizado, melhor DX
- **Alinhamento:** Substitui Webpack em projetos modernos

#### 3. **TypeScript**
- **Por quê:** Type safety, melhor autocomplete, menos bugs
- **Benefício:** Compatibilidade com tipos do backend, refatoração segura
- **Alinhamento:** Padrão em projetos enterprise

#### 4. **TailwindCSS**
- **Por quê:** Utility-first, desenvolvimento rápido, consistência visual
- **Benefício:** Menos CSS customizado, design system consistente
- **Alinhamento:** Usado em projetos modernos (Next.js, Vercel, etc.)

#### 5. **Zustand**
- **Por quê:** Leve (~1KB), simples, TypeScript-first
- **Benefício:** Menos boilerplate que Redux, performance melhor que Context API
- **Alinhamento:** Alternativa moderna ao Redux

#### 6. **Axios**
- **Por quê:** Interceptors, cancelamento, melhor tratamento de erros
- **Benefício:** Tratamento centralizado de erros, autenticação fácil
- **Alinhamento:** Padrão em projetos React

#### 7. **React Hook Form + Zod**
- **Por quê:** Validação type-safe, performance (menos re-renders)
- **Benefício:** Validação no cliente e servidor com mesmo schema
- **Alinhamento:** Melhor prática moderna para formulários

---

## 📊 Mapeamento Frontend ↔ Backend

### DTOs e Types

| Backend (DTO) | Frontend (Type) | Descrição |
|---------------|-----------------|-----------|
| `CreateOrderRequest` | `CreateOrderRequest` | Dados para criar pedido |
| `CreateOrderResponse` | `CreateOrderResponse` | Resultado da criação |
| `OrderResponse` | `OrderResponse` | Dados do pedido |
| `OrderItemRequest` | `OrderItemRequest` | Item do pedido (request) |
| `OrderItemResponse` | `OrderItemResponse` | Item do pedido (response) |
| `OrderStatus` (enum) | `OrderStatus` (enum) | Status do pedido |
| `RiskLevel` (enum) | `RiskLevel` (enum) | Nível de risco |

### Endpoints

| Endpoint Backend | Serviço Frontend | Página |
|------------------|------------------|--------|
| `POST /api/v1/orders` | `createOrder()` | `CreateOrderPage` |
| `GET /api/v1/orders` | `getAllOrders()` | `OrdersListPage` |
| `GET /api/v1/orders/{id}` | `getOrderById()` | `OrderDetailPage` |
| `GET /api/v1/orders/number/{number}` | `getOrderByNumber()` | (Futuro) |

---

## 🔄 Sincronização de Estados

### Estados Gerenciados

#### 1. **Orders List**
- **Fonte:** `GET /api/v1/orders`
- **Store:** `orders: OrderResponse[]`
- **Atualização:** Manual (fetchOrders) ou após criar pedido

#### 2. **Current Order**
- **Fonte:** `GET /api/v1/orders/{id}`
- **Store:** `currentOrder: OrderResponse | null`
- **Atualização:** Ao navegar para detalhes

#### 3. **Loading State**
- **Fonte:** Estado interno do store
- **Store:** `loading: 'idle' | 'loading' | 'success' | 'error'`
- **Uso:** Indicadores de carregamento na UI

#### 4. **Error State**
- **Fonte:** Erros da API ou validação
- **Store:** `error: ApiError | null`
- **Uso:** Exibir mensagens de erro

---

## 🎨 Design System

### Cores e Status

#### Order Status
- **PENDING:** Amarelo (`bg-yellow-100 text-yellow-800`)
- **PAID:** Verde (`bg-green-100 text-green-800`)
- **PAYMENT_FAILED:** Vermelho (`bg-red-100 text-red-800`)
- **CANCELED:** Cinza (`bg-gray-100 text-gray-800`)

#### Risk Level
- **LOW:** Verde (`bg-green-100 text-green-800`)
- **HIGH:** Vermelho (`bg-red-100 text-red-800`)
- **PENDING:** Amarelo (`bg-yellow-100 text-yellow-800`)

### Componentes Reutilizáveis

Todos os componentes seguem princípios de:
- **Acessibilidade:** ARIA labels, keyboard navigation
- **Responsividade:** Mobile-first design
- **Consistência:** Design system unificado
- **Type Safety:** TypeScript em todos os componentes

---

## 🧪 Testes

### Estratégia de Testes

#### 1. **Testes Unitários** (Componentes)
- Testar componentes isolados
- Mock de dependências (store, services)
- Validar renderização e interações

#### 2. **Testes de Integração** (Páginas)
- Testar fluxo completo de páginas
- Mock de API (MSW - Mock Service Worker)
- Validar integração entre componentes

#### 3. **Testes E2E** (Jornada do Usuário)
- Testar fluxo completo: Criar → Visualizar → Listar
- Usar Playwright ou Cypress
- Validar integração real com backend

### Ferramentas de Teste

- **Vitest:** Framework de testes (substitui Jest)
- **React Testing Library:** Testar componentes
- **MSW:** Mock de API para testes
- **Playwright:** Testes E2E (futuro)

---

## 📚 Documentação Relacionada

### Backend
- [Fase 8: REST API](fases/FASE8-CAMADA-PRESENTATION-REST-API.md) - Endpoints do backend
- [Guia Completo de Testes](GUIA-COMPLETO-DE-TESTES.md) - Como testar backend

### Frontend
- [README do Frontend](../frontend/README.md) - Documentação técnica do frontend
- [Estrutura e Componentes](../frontend/README.md#estrutura-de-pastas) - Organização do código

---

## ✅ Checklist de Integração

### Backend → Frontend
- [x] DTOs mapeados para Types TypeScript
- [x] Endpoints documentados e implementados
- [x] Tratamento de erros alinhado
- [x] Validações consistentes (Zod + Bean Validation)

### Frontend → Backend
- [x] Serviço de API configurado
- [x] Store conectado com backend
- [x] Formulários validados antes de enviar
- [x] Tratamento de erros da API

### Sincronização
- [x] Types atualizados quando DTOs mudam
- [x] Documentação atualizada quando endpoints mudam
- [x] Testes atualizados quando contratos mudam

---

## 🔄 Manutenção e Evolução

### Quando Alterar Backend

**Se alterar DTOs:**
1. Atualizar `frontend/src/types/index.ts`
2. Atualizar serviços se necessário
3. Atualizar componentes que usam os types
4. Atualizar testes

**Se alterar Endpoints:**
1. Atualizar `frontend/src/services/orderService.ts`
2. Atualizar store se necessário
3. Atualizar páginas que usam os serviços
4. Atualizar documentação

### Quando Alterar Frontend

**Se alterar Types:**
1. Verificar compatibilidade com DTOs do backend
2. Atualizar serviços se necessário
3. Atualizar documentação

**Se alterar Componentes:**
1. Verificar impacto em páginas
2. Atualizar testes
3. Atualizar documentação de componentes

---

**Última Atualização:** 2024

