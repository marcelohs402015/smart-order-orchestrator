# Smart Order Orchestrator - Frontend

Frontend do Smart Order Orchestrator construído com React + Vite + TypeScript.

## 🚀 Stack Tecnológica

- **React 18+**: Biblioteca UI moderna e performática
- **Vite**: Build tool rápido com HMR (Hot Module Replacement)
- **TypeScript**: Type safety e melhor DX (Developer Experience)
- **TailwindCSS**: Utility-first CSS framework
- **Zustand**: State management leve e simples
- **Axios**: Cliente HTTP para comunicação com API REST
- **React Hook Form + Zod**: Validação de formulários type-safe

## 📦 Instalação

```bash
# Instalar dependências
npm install

# Ou com yarn
yarn install

# Ou com pnpm
pnpm install
```

## 🛠️ Scripts Disponíveis

```bash
# Desenvolvimento (servidor local na porta 5173)
npm run dev

# Build para produção
npm run build

# Preview do build de produção
npm run preview

# Linting
npm run lint

# Linting com auto-fix
npm run lint:fix

# Formatação de código (Prettier)
npm run format
```

## 🌐 Configuração

### Variáveis de Ambiente

Copie `.env.example` para `.env` e configure:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_ENV=development
```

### Proxy para API

O Vite está configurado para fazer proxy de `/api/*` para `http://localhost:8080/*`.

Exemplo:
- Frontend: `http://localhost:5173/api/orders`
- Backend: `http://localhost:8080/orders`

## 📁 Estrutura de Pastas

```
frontend/
├── src/
│   ├── components/     # Componentes reutilizáveis
│   ├── pages/          # Páginas/rotas
│   ├── hooks/          # Custom hooks
│   ├── store/          # Zustand stores
│   ├── services/       # Serviços de API
│   ├── types/          # TypeScript types
│   ├── utils/          # Funções utilitárias
│   └── lib/            # Configurações
├── public/             # Arquivos estáticos
└── dist/              # Build de produção
```

## 🔗 Integração com Backend

O frontend consome APIs REST do backend Spring Boot:

- **Base URL**: `http://localhost:8080` (desenvolvimento)
- **Formato**: JSON
- **Autenticação**: (a ser implementado)

## 📝 Próximos Passos

1. Configurar React Router para rotas
2. Criar componentes base (Button, Input, Card, etc.)
3. Implementar store Zustand para state management
4. Criar serviços de API (Axios)
5. Implementar páginas principais (Dashboard, Checkout, etc.)

