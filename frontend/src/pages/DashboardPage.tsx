import { useNavigate } from 'react-router-dom';
import { useOrders } from '../hooks/useOrders';
import { OrderStatus } from '../types';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { OrderCard } from '../components/OrderCard';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { Alert } from '../components/ui/Alert';

export const DashboardPage = () => {
  const navigate = useNavigate();
  const { orders, loading, error, refetch, clearError } = useOrders();

  const totalOrders = orders.length;
  const paidOrders = orders.filter((o) => o.status === OrderStatus.PAID).length;
  const pendingOrders = orders.filter((o) => o.status === OrderStatus.PENDING).length;
  const failedPaymentOrders = orders.filter((o) => o.status === OrderStatus.PAYMENT_FAILED);
  const failedPaymentCount = failedPaymentOrders.length;
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
                    onClick={() => refetch()}
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
                  onClick={() => refetch()}
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
                <p className="text-3xl font-bold text-orange-600">{failedPaymentCount}</p>
                <p className="text-sm text-gray-500 mt-1">Falha de Pagamento</p>
              </div>
            </Card>
          </div>

          {failedPaymentCount > 0 && (
            <Card className="mb-8 border-2 border-red-300 bg-red-50">
              <div className="flex items-center justify-between mb-4">
                <div>
                  <h2 className="text-xl font-semibold text-red-900 flex items-center gap-2">
                    <span>⚠️</span> Atenção: Pedidos com Falha de Pagamento
                  </h2>
                  <p className="text-sm text-red-700 mt-1">
                    {failedPaymentCount} {failedPaymentCount === 1 ? 'pedido' : 'pedidos'} com pagamento não processado
                  </p>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => navigate('/orders')}
                >
                  Ver na Lista
                </Button>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {failedPaymentOrders.slice(0, 3).map((order) => (
                  <OrderCard
                    key={order.id}
                    order={order}
                    onClick={() => navigate(`/orders/${order.id}`)}
                  />
                ))}
              </div>
              {failedPaymentCount > 3 && (
                <div className="mt-4 text-center">
                  <Button
                    variant="outline"
                    onClick={() => navigate('/orders')}
                  >
                    Ver todos os {failedPaymentCount} pedidos com falha
                  </Button>
                </div>
              )}
            </Card>
          )}

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
