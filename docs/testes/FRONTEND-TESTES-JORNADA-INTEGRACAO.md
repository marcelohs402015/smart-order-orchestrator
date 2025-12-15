# Frontend - Testes de Jornada e Integração

## 🎯 Estratégia de Testes

### Pirâmide de Testes

```
        ┌─────────────┐
        │   E2E Tests │  Poucos, mas críticos
        │  (Jornadas) │
        └─────────────┘
       ┌───────────────┐
       │ Integration   │  Testes de fluxo completo
       │    Tests      │
       └───────────────┘
      ┌─────────────────┐
      │  Unit Tests     │  Muitos, rápidos
      │  (Components)   │
      └─────────────────┘
```

---

## 🧪 Tipos de Testes

### 1. Testes Unitários (Componentes)

**O que testam:** Componentes isolados, funções utilitárias, hooks.

**Ferramentas:**
- **Vitest:** Framework de testes (substitui Jest)
- **React Testing Library:** Testar componentes React
- **@testing-library/user-event:** Simular interações do usuário

**Exemplo:**
```typescript
// Button.test.tsx
import { render, screen } from '@testing-library/react';
import { Button } from './Button';

describe('Button', () => {
  it('should render button with text', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });

  it('should be disabled when loading', () => {
    render(<Button isLoading>Click me</Button>);
    expect(screen.getByRole('button')).toBeDisabled();
  });
});
```

**O que testar:**
- ✅ Renderização correta
- ✅ Props funcionando
- ✅ Estados (loading, disabled)
- ✅ Event handlers (onClick, etc.)
- ✅ Acessibilidade (ARIA labels)

---

### 2. Testes de Integração (Páginas)

**O que testam:** Fluxo completo de páginas, integração entre componentes.

**Ferramentas:**
- **Vitest:** Framework de testes
- **React Testing Library:** Renderizar páginas completas
- **MSW (Mock Service Worker):** Mock de API

**Exemplo:**
```typescript
// CreateOrderPage.test.tsx
import { render, screen, waitFor } from '@testing-library/react';
import { CreateOrderPage } from './CreateOrderPage';
import { server } from '../mocks/server';
import { rest } from 'msw';

describe('CreateOrderPage', () => {
  it('should create order successfully', async () => {
    server.use(
      rest.post('/api/v1/orders', (req, res, ctx) => {
        return res(ctx.json({ success: true, order: mockOrder }));
      })
    );

    render(<CreateOrderPage />);
    
    // Preencher formulário
    await userEvent.type(screen.getByLabelText('Nome do Cliente'), 'João');
    // ... preencher outros campos
    
    // Submeter
    await userEvent.click(screen.getByText('Criar Pedido'));
    
    // Verificar resultado
    await waitFor(() => {
      expect(screen.getByText('Pedido criado com sucesso')).toBeInTheDocument();
    });
  });
});
```

**O que testar:**
- ✅ Formulários completos
- ✅ Validação de campos
- ✅ Integração com store
- ✅ Integração com serviços de API
- ✅ Navegação após ações
- ✅ Tratamento de erros

---

### 3. Testes E2E (Jornada do Usuário)

**O que testam:** Fluxo completo do usuário, integração real com backend.

**Ferramentas:**
- **Playwright** ou **Cypress:** Testes E2E
- **Backend real:** Ou mock completo

**Jornadas Críticas:**

#### Jornada 1: Criar Pedido Completo

```
1. Acessar Dashboard
2. Clicar em "Criar Novo Pedido"
3. Preencher dados do cliente
4. Adicionar itens do pedido
5. Submeter formulário
6. Verificar sucesso
7. Verificar redirecionamento para lista
8. Verificar pedido na lista
```

#### Jornada 2: Visualizar Detalhes

```
1. Acessar Lista de Pedidos
2. Clicar em um pedido
3. Verificar detalhes exibidos
4. Verificar informações corretas
5. Voltar para lista
```

#### Jornada 3: Listar e Filtrar

```
1. Acessar Lista de Pedidos
2. Verificar todos os pedidos exibidos
3. Verificar informações de cada card
4. Clicar em diferentes pedidos
```

**Exemplo (Playwright):**
```typescript
// e2e/create-order.spec.ts
import { test, expect } from '@playwright/test';

test('should create order end-to-end', async ({ page }) => {
  // 1. Acessar dashboard
  await page.goto('http://localhost:5173');
  
  // 2. Clicar em criar pedido
  await page.click('text=Criar Novo Pedido');
  
  // 3. Preencher formulário
  await page.fill('[name="customerName"]', 'João Silva');
  await page.fill('[name="customerEmail"]', 'joao@example.com');
  
  // 4. Adicionar item
  await page.fill('[name="items.0.productName"]', 'Produto 1');
  await page.fill('[name="items.0.quantity"]', '2');
  await page.fill('[name="items.0.unitPrice"]', '10.50');
  
  // 5. Submeter
  await page.click('button:has-text("Criar Pedido")');
  
  // 6. Verificar sucesso
  await expect(page.locator('text=Pedido criado com sucesso')).toBeVisible();
  
  // 7. Verificar redirecionamento
  await expect(page).toHaveURL(/\/orders$/);
});
```

---

## 🛠️ Configuração de Testes

### Vitest (Unit e Integration)

**Arquivo:** `frontend/vitest.config.ts`

```typescript
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
});
```

### MSW (Mock Service Worker)

**Arquivo:** `frontend/src/mocks/handlers.ts`

```typescript
import { rest } from 'msw';

export const handlers = [
  rest.post('/api/v1/orders', (req, res, ctx) => {
    return res(
      ctx.status(201),
      ctx.json({
        success: true,
        order: mockOrder,
        sagaExecutionId: 'saga-123',
      })
    );
  }),
  
  rest.get('/api/v1/orders', (req, res, ctx) => {
    return res(ctx.json([mockOrder1, mockOrder2]));
  }),
  
  rest.get('/api/v1/orders/:id', (req, res, ctx) => {
    return res(ctx.json(mockOrder));
  }),
];
```

### Playwright (E2E)

**Arquivo:** `frontend/playwright.config.ts`

```typescript
import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  use: {
    baseURL: 'http://localhost:5173',
  },
  webServer: {
    command: 'npm run dev',
    port: 5173,
  },
});
```

---

## 📋 Checklist de Testes

### Testes Unitários

- [ ] **Button Component**
  - [ ] Renderiza com texto
  - [ ] Variantes funcionando
  - [ ] Estados (loading, disabled)
  - [ ] onClick funciona

- [ ] **Input Component**
  - [ ] Renderiza com label
  - [ ] Validação de erro
  - [ ] Tipos de input (text, email, number)

- [ ] **OrderCard Component**
  - [ ] Renderiza informações do pedido
  - [ ] Formatação de valores
  - [ ] onClick funciona

- [ ] **Utils Functions**
  - [ ] formatCurrency
  - [ ] formatDate
  - [ ] getOrderStatusInfo
  - [ ] getRiskLevelInfo

### Testes de Integração

- [ ] **CreateOrderPage**
  - [ ] Formulário completo funciona
  - [ ] Validação de campos
  - [ ] Adicionar/remover itens
  - [ ] Submissão com sucesso
  - [ ] Tratamento de erro

- [ ] **OrdersListPage**
  - [ ] Carrega lista de pedidos
  - [ ] Exibe cards corretamente
  - [ ] Loading state
  - [ ] Error state
  - [ ] Navegação para detalhes

- [ ] **OrderDetailPage**
  - [ ] Carrega detalhes do pedido
  - [ ] Exibe todas as informações
  - [ ] Loading state
  - [ ] Error state (pedido não encontrado)

- [ ] **DashboardPage**
  - [ ] Carrega estatísticas
  - [ ] Exibe pedidos recentes
  - [ ] Ações rápidas funcionam

### Testes E2E (Jornadas)

- [ ] **Jornada: Criar Pedido**
  - [ ] Dashboard → Criar Pedido
  - [ ] Preencher formulário
  - [ ] Submeter
  - [ ] Verificar sucesso
  - [ ] Verificar na lista

- [ ] **Jornada: Visualizar Detalhes**
  - [ ] Lista → Detalhes
  - [ ] Verificar informações
  - [ ] Voltar para lista

- [ ] **Jornada: Navegação**
  - [ ] Navegar entre páginas
  - [ ] Links funcionam
  - [ ] Breadcrumbs (se houver)

---

## 🚀 Como Executar Testes

### Testes Unitários e de Integração

```bash
cd frontend

# Executar todos os testes
npm run test

# Executar em modo watch
npm run test:watch

# Executar com cobertura
npm run test:coverage

# Executar testes específicos
npm run test Button.test.tsx
```

### Testes E2E

```bash
cd frontend

# Executar testes E2E
npm run test:e2e

# Executar em modo UI (interativo)
npm run test:e2e:ui

# Executar testes específicos
npm run test:e2e create-order.spec.ts
```

---

## 📊 Cobertura de Testes

### Meta de Cobertura

- **Componentes:** > 80%
- **Páginas:** > 70%
- **Services:** > 90%
- **Utils:** > 90%

### Relatório de Cobertura

```bash
npm run test:coverage
```

Gera relatório em `coverage/` com:
- Cobertura por arquivo
- Linhas não cobertas
- Branches não cobertos

---

## 🔄 Sincronização com Backend

### Quando Backend Mudar

**Se DTOs mudarem:**
1. Atualizar `frontend/src/types/index.ts`
2. Atualizar mocks de teste (`src/mocks/`)
3. Atualizar testes que usam os types
4. Executar testes e verificar

**Se Endpoints mudarem:**
1. Atualizar `frontend/src/services/orderService.ts`
2. Atualizar handlers do MSW
3. Atualizar testes de integração
4. Atualizar testes E2E

### Quando Frontend Mudar

**Se Components mudarem:**
1. Atualizar testes unitários
2. Atualizar testes de integração se necessário
3. Verificar impacto em E2E

**Se Páginas mudarem:**
1. Atualizar testes de integração
2. Atualizar testes E2E
3. Verificar jornadas do usuário

---

## 📚 Documentação Relacionada

- [Guia Completo de Testes (Backend)](GUIA-COMPLETO-DE-TESTES.md) - Estratégia de testes do backend
- [Frontend - Propósito e Integração](FRONTEND-PROPOSITO-E-INTEGRACAO.md) - Integração frontend/backend
- [README do Frontend](../frontend/README.md) - Documentação técnica

---

**Última Atualização:** 2024

