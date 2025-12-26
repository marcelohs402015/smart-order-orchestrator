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

import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useOrders } from '../hooks/useOrders';
import { useOrderSearch } from '../hooks/useOrderSearch';
import { useOrderStore } from '../store/orderStore';
import { OrderCard } from '../components/OrderCard';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { Alert } from '../components/ui/Alert';
import { Card } from '../components/ui/Card';
import { OrderStatus } from '../types';

type StatusFilter = 'ALL' | OrderStatus;

export const OrdersListPage = () => {
  const navigate = useNavigate();
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [searchNumber, setSearchNumber] = useState('');
  
  // Hook para buscar pedidos (sem auto-fetch, vamos controlar manualmente)
  const { orders, loading, error, refetch, clearError } = useOrders(undefined, false);
  
  // Hook para busca por número
  const {
    searchResult,
    searchLoading,
    searchError,
    searchByNumber,
    clearSearch,
  } = useOrderSearch();
  
  // Buscar pedidos com falha de pagamento do store
  const failedPaymentOrders = useOrderStore((state) => state.failedPaymentOrders);
  const fetchFailedPaymentOrders = useOrderStore((state) => state.fetchFailedPaymentOrders);

  const isLoading = loading === 'loading';
  
  // Buscar pedidos quando o filtro mudar
  useEffect(() => {
    const statusToFetch = statusFilter === 'ALL' ? undefined : statusFilter;
    refetch(statusToFetch);
  }, [statusFilter, refetch]);
  
  // Buscar pedidos com falha de pagamento ao montar
  useEffect(() => {
    fetchFailedPaymentOrders();
  }, [fetchFailedPaymentOrders]);

  const handleSearchByNumber = () => {
    searchByNumber(searchNumber);
  };

  const handleClearSearch = () => {
    setSearchNumber('');
    clearSearch();
  };

  if (isLoading) {
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
          <div>
            <p className="font-semibold mb-1">Erro ao carregar pedidos</p>
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
                    onClick={() => {
                      const statusToFetch = statusFilter === 'ALL' ? undefined : statusFilter;
                      refetch(statusToFetch);
                    }}
                    disabled={isLoading}
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
                  onClick={() => {
                    const statusToFetch = statusFilter === 'ALL' ? undefined : statusFilter;
                    refetch(statusToFetch);
                  }}
                  disabled={isLoading}
                >
                  Tentar Novamente
                </Button>
              </div>
            )}
          </div>
        </Alert>
      )}

      <Card className="mb-6">
        <h2 className="text-xl font-semibold mb-4">Buscar Pedido por Número</h2>
        <div className="flex gap-4">
          <div className="flex-1">
            <Input
              label="Número do Pedido"
              value={searchNumber}
              onChange={(e) => setSearchNumber(e.target.value)}
              placeholder="Ex: ORD-1234567890"
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  handleSearchByNumber();
                }
              }}
              error={searchError || undefined}
              value={searchNumber}
              onChange={(e) => setSearchNumber(e.target.value)}
            />
          </div>
          <div className="flex items-end gap-2">
            <Button
              onClick={handleSearchByNumber}
              isLoading={searchLoading}
              disabled={searchLoading}
            >
              Buscar
            </Button>
            {(searchResult || searchError) && (
              <Button
                variant="secondary"
                onClick={handleClearSearch}
              >
                Limpar
              </Button>
            )}
          </div>
        </div>
        {searchResult && (
          <div className="mt-4">
            <OrderCard
              order={searchResult}
              onClick={() => navigate(`/orders/${searchResult.id}`)}
            />
          </div>
        )}
      </Card>

      {/* Seção de Pedidos com Falha de Pagamento */}
      {failedPaymentOrders.length > 0 && (
        <Card className="mb-6 border-2 border-red-300 bg-red-50">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-xl font-semibold text-red-900 flex items-center gap-2">
                <span>⚠️</span> Pedidos com Falha de Pagamento
              </h2>
              <p className="text-sm text-red-700 mt-1">
                {failedPaymentOrders.length} {failedPaymentOrders.length === 1 ? 'pedido' : 'pedidos'} com pagamento não processado
              </p>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setStatusFilter(OrderStatus.PAYMENT_FAILED)}
            >
              Ver Todos
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
          {failedPaymentOrders.length > 3 && (
            <div className="mt-4 text-center">
              <Button
                variant="outline"
                onClick={() => setStatusFilter(OrderStatus.PAYMENT_FAILED)}
              >
                Ver todos os {failedPaymentOrders.length} pedidos com falha
              </Button>
            </div>
          )}
        </Card>
      )}

      {/* Filtros por Status */}
      <Card className="mb-6">
        <h2 className="text-xl font-semibold mb-4">Filtros</h2>
        <div className="flex flex-wrap gap-2">
          <Button
            variant={statusFilter === 'ALL' ? 'primary' : 'secondary'}
            size="sm"
            onClick={() => setStatusFilter('ALL')}
          >
            Todos ({orders.length})
          </Button>
          <Button
            variant={statusFilter === OrderStatus.PENDING ? 'primary' : 'secondary'}
            size="sm"
            onClick={() => setStatusFilter(OrderStatus.PENDING)}
          >
            Pendentes ({orders.filter(o => o.status === OrderStatus.PENDING).length})
          </Button>
          <Button
            variant={statusFilter === OrderStatus.PAID ? 'primary' : 'secondary'}
            size="sm"
            onClick={() => setStatusFilter(OrderStatus.PAID)}
          >
            Pagos ({orders.filter(o => o.status === OrderStatus.PAID).length})
          </Button>
          <Button
            variant={statusFilter === OrderStatus.PAYMENT_FAILED ? 'primary' : 'secondary'}
            size="sm"
            onClick={() => setStatusFilter(OrderStatus.PAYMENT_FAILED)}
          >
            Falha de Pagamento ({failedPaymentOrders.length})
          </Button>
          <Button
            variant={statusFilter === OrderStatus.CANCELED ? 'primary' : 'secondary'}
            size="sm"
            onClick={() => setStatusFilter(OrderStatus.CANCELED)}
          >
            Cancelados ({orders.filter(o => o.status === OrderStatus.CANCELED).length})
          </Button>
        </div>
      </Card>

      {/* Lista de Pedidos Filtrados */}
      {orders.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg mb-4">
            {statusFilter === 'ALL' 
              ? 'Nenhum pedido encontrado'
              : `Nenhum pedido com status "${statusFilter}" encontrado`}
          </p>
          {statusFilter !== 'ALL' && (
            <Button
              variant="outline"
              onClick={() => setStatusFilter('ALL')}
            >
              Ver Todos os Pedidos
            </Button>
          )}
          {statusFilter === 'ALL' && (
            <Button onClick={() => navigate('/orders/create')}>
              Criar Primeiro Pedido
            </Button>
          )}
        </div>
      ) : (
        <>
          {statusFilter !== 'ALL' && (
            <div className="mb-4">
              <p className="text-sm text-gray-600">
                Mostrando {orders.length} {orders.length === 1 ? 'pedido' : 'pedidos'} 
                {` com status "${statusFilter}"`}
              </p>
            </div>
          )}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {orders.map((order) => (
              <OrderCard
                key={order.id}
                order={order}
                onClick={() => navigate(`/orders/${order.id}`)}
              />
            ))}
          </div>
        </>
      )}
    </div>
  );
};

