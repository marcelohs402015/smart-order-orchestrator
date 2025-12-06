/**
 * Componente principal da aplicação.
 * 
 * <h3>Arquitetura Frontend:</h3>
 * <ul>
 *   <li><strong>React Router:</strong> Roteamento client-side (SPA)</li>
 *   <li><strong>Zustand:</strong> State management global (leve e performático)</li>
 *   <li><strong>Axios:</strong> Cliente HTTP para comunicação com API REST</li>
 *   <li><strong>React Hook Form + Zod:</strong> Validação de formulários</li>
 * </ul>
 * 
 * <h3>Estrutura de Pastas:</h3>
 * <pre>
 * src/
 *   ├── components/     # Componentes reutilizáveis
 *   ├── pages/          # Páginas/rotas da aplicação
 *   ├── hooks/           # Custom hooks
 *   ├── store/           # Zustand stores (state management)
 *   ├── services/        # Serviços de API (Axios)
 *   ├── types/           # TypeScript types/interfaces
 *   ├── utils/           # Funções utilitárias
 *   └── lib/             # Configurações de bibliotecas
 * </pre>
 */

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from './components/Layout';
import { DashboardPage } from './pages/DashboardPage';
import { OrdersListPage } from './pages/OrdersListPage';
import { CreateOrderPage } from './pages/CreateOrderPage';
import { OrderDetailPage } from './pages/OrderDetailPage';

function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/orders" element={<OrdersListPage />} />
          <Route path="/orders/create" element={<CreateOrderPage />} />
          <Route path="/orders/:id" element={<OrderDetailPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}

export default App;
