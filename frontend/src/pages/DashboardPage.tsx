/**
 * Página Dashboard com visão geral do sistema.
 * 
 * <h3>Funcionalidades:</h3>
 * <ul>
 *   <li>Estatísticas gerais</li>
 *   <li>Pedidos recentes</li>
 *   <li>Ações rápidas</li>
 * </ul>
 */

import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useOrderStore } from '../store/orderStore';
import { OrderStatus, RiskLevel } from '../types';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { OrderCard } from '../components/OrderCard';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { Alert } from '../components/ui/Alert';

export const DashboardPage = () => {
  const navigate = useNavigate();
  const { orders, loading, error, fetchOrders, clearError } = useOrderStore();

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  // Estatísticas
  const totalOrders = orders.length;
  const paidOrders = orders.filter((o) => o.status === OrderStatus.PAID).length;
  const pendingOrders = orders.filter((o) => o.status === OrderStatus.PENDING).length;
  const highRiskOrders = orders.filter((o) => o.riskLevel === RiskLevel.HIGH).length;
  const recentOrders = orders.slice(0, 5);

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p className="mt-2 text-gray-600">
          Visão geral do sistema de pedidos
        </p>
      </div>

      {error && (
        <Alert variant="error" onClose={clearError} className="mb-6">
          <div>
            <p className="font-semibold mb-1">Erro ao carregar dados</p>
            <p>{error.message}</p>
            {error.status === 500 && (
              <div className="mt-3 p-3 bg-red-50 border border-red-200 rounded text-sm">
                <p className="font-medium mb-2">O backend está respondendo, mas ocorreu um erro interno.</p>
                <p className="mb-2 text-gray-700">Possíveis causas:</p>
                <ul className="list-disc list-inside space-y-1 text-gray-700">
                  <li>Banco de dados não está conectado ou configurado corretamente</li>
                  <li>Erro na consulta ao banco de dados (verifique se as tabelas existem)</li>
                  <li>Erro na conversão de dados (verifique os logs do backend)</li>
                  <li>Problema na configuração do Spring Boot</li>
                </ul>
                <p className="mt-2 text-gray-600">
                  <strong>Status:</strong> {error.status} | <strong>Path:</strong> {error.path || 'N/A'}
                </p>
                <p className="mt-2 text-xs text-gray-500">
                  💡 Dica: Verifique os logs do backend em http://localhost:8080 para mais detalhes sobre o erro.
                </p>
                <div className="mt-3">
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => fetchOrders()}
                    disabled={loading === 'loading'}
                  >
                    Tentar Novamente
                  </Button>
                </div>
              </div>
            )}
            {error.details && Object.keys(error.details).length > 0 && (
              <div className="mt-2 text-sm">
                <p className="font-medium mb-1">Detalhes do erro:</p>
                <ul className="list-disc list-inside space-y-1">
                  {Object.entries(error.details).map(([field, message]) => (
                    <li key={field}>
                      <strong>{field}:</strong> {message}
                    </li>
                  ))}
                </ul>
              </div>
            )}
            {error.status !== 500 && (
              <div className="mt-3">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => fetchOrders()}
                  disabled={loading === 'loading'}
                >
                  Tentar Novamente
                </Button>
              </div>
            )}
          </div>
        </Alert>
      )}

      {loading === 'loading' ? (
        <div className="flex items-center justify-center py-12">
          <LoadingSpinner size="lg" />
        </div>
      ) : (
        <>
          {/* Estatísticas */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <Card padding="md">
              <div className="text-center">
                <p className="text-3xl font-bold text-blue-600">{totalOrders}</p>
                <p className="text-sm text-gray-500 mt-1">Total de Pedidos</p>
              </div>
            </Card>
            <Card padding="md">
              <div className="text-center">
                <p className="text-3xl font-bold text-green-600">{paidOrders}</p>
                <p className="text-sm text-gray-500 mt-1">Pedidos Pagos</p>
              </div>
            </Card>
            <Card padding="md">
              <div className="text-center">
                <p className="text-3xl font-bold text-yellow-600">{pendingOrders}</p>
                <p className="text-sm text-gray-500 mt-1">Pedidos Pendentes</p>
              </div>
            </Card>
            <Card padding="md">
              <div className="text-center">
                <p className="text-3xl font-bold text-red-600">{highRiskOrders}</p>
                <p className="text-sm text-gray-500 mt-1">Alto Risco</p>
              </div>
            </Card>
          </div>

          {/* Ações Rápidas */}
          <Card className="mb-8">
            <h2 className="text-xl font-semibold mb-4">Ações Rápidas</h2>
            <div className="flex flex-wrap gap-4">
              <Button onClick={() => navigate('/orders/create')}>
                + Criar Novo Pedido
              </Button>
              <Button
                variant="secondary"
                onClick={() => navigate('/orders')}
              >
                Ver Todos os Pedidos
              </Button>
            </div>
          </Card>

          {/* Pedidos Recentes */}
          <Card>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold">Pedidos Recentes</h2>
              <Button
                variant="outline"
                size="sm"
                onClick={() => navigate('/orders')}
              >
                Ver Todos
              </Button>
            </div>
            {recentOrders.length === 0 ? (
              <p className="text-gray-500 text-center py-8">
                Nenhum pedido encontrado. Crie seu primeiro pedido!
              </p>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {recentOrders.map((order) => (
                  <OrderCard
                    key={order.id}
                    order={order}
                    onClick={() => navigate(`/orders/${order.id}`)}
                  />
                ))}
              </div>
            )}
          </Card>
        </>
      )}
    </div>
  );
};

