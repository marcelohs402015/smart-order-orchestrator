import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ErrorBoundary } from './components/ErrorBoundary';
import { Layout } from './components/Layout';
import { DashboardPage } from './pages/DashboardPage';
import { OrdersListPage } from './pages/OrdersListPage';
import { CreateOrderPage } from './pages/CreateOrderPage';
import { OrderDetailPage } from './pages/OrderDetailPage';

function App() {
  return (
    <ErrorBoundary>
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
    </ErrorBoundary>
  );
}

export default App;
