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
import { useOrderStore } from '../store/orderStore';
import { OrderCard } from '../components/OrderCard';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { Alert } from '../components/ui/Alert';
import { Card } from '../components/ui/Card';
import * as orderService from '../services/orderService';
import { OrderResponse, ApiError } from '../types';

export const OrdersListPage = () => {
  const navigate = useNavigate();
  const { orders, loading, error, fetchOrders, clearError } = useOrderStore();
  const [searchNumber, setSearchNumber] = useState('');
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);
  const [searchResult, setSearchResult] = useState<OrderResponse | null>(null);

  const isLoading = loading === 'loading';

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  const handleSearchByNumber = async () => {
    if (!searchNumber.trim()) {
      setSearchError('Digite um número de pedido');
      setSearchResult(null);
      return;
    }

    setSearchLoading(true);
    setSearchError(null);
    setSearchResult(null);

    try {
      const order = await orderService.getOrderByNumber(searchNumber.trim());
      setSearchResult(order);
    } catch (err) {
      const apiError = err as ApiError;
      if (apiError.status === 404) {
        setSearchError('Pedido não encontrado');
      } else {
        setSearchError(apiError.message || 'Erro ao buscar pedido');
      }
      setSearchResult(null);
    } finally {
      setSearchLoading(false);
    }
  };

  const handleClearSearch = () => {
    setSearchNumber('');
    setSearchError(null);
    setSearchResult(null);
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
                    onClick={() => fetchOrders()}
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
                  onClick={() => fetchOrders()}
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
            {searchResult && (
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

