/**
 * Página para listar todos os pedidos.
 * 
 * <h3>Funcionalidades:</h3>
 * <ul>
 *   <li>Lista de pedidos com cards</li>
 *   <li>Filtros por status (futuro)</li>
 *   <li>Busca (futuro)</li>
 *   <li>Paginação (futuro)</li>
 * </ul>
 */

import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useOrderStore } from '../store/orderStore';
import { OrderCard } from '../components/OrderCard';
import { Button } from '../components/ui/Button';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { Alert } from '../components/ui/Alert';

export const OrdersListPage = () => {
  const navigate = useNavigate();
  const { orders, loading, error, fetchOrders, clearError } = useOrderStore();

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  if (loading === 'loading') {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Pedidos</h1>
          <p className="mt-2 text-gray-600">
            Gerencie todos os pedidos do sistema
          </p>
        </div>
        <Button onClick={() => navigate('/orders/create')}>
          + Novo Pedido
        </Button>
      </div>

      {error && (
        <Alert variant="error" onClose={clearError} className="mb-6">
          {error.message}
        </Alert>
      )}

      {orders.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg mb-4">Nenhum pedido encontrado</p>
          <Button onClick={() => navigate('/orders/create')}>
            Criar Primeiro Pedido
          </Button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {orders.map((order) => (
            <OrderCard
              key={order.id}
              order={order}
              onClick={() => navigate(`/orders/${order.id}`)}
            />
          ))}
        </div>
      )}
    </div>
  );
};

