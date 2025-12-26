/**
 * Página para visualizar detalhes de um pedido.
 * 
 * <h3>Funcionalidades:</h3>
 * <ul>
 *   <li>Detalhes completos do pedido</li>
 *   <li>Lista de itens</li>
 *   <li>Informações de pagamento e risco</li>
 * </ul>
 */

import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useOrder } from '../hooks/useOrder';
import { formatCurrency, formatDate, getOrderStatusInfo, getRiskLevelInfo } from '../utils';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';
import { Alert } from '../components/ui/Alert';
import { ApiError } from '../types';

export const OrderDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const {
    order: currentOrder,
    loading,
    error,
    refreshPaymentStatus,
    clearError,
  } = useOrder(id);
  const [isRefreshingPayment, setIsRefreshingPayment] = useState(false);
  const [refreshError, setRefreshError] = useState<typeof error>(null);

  if (loading === 'loading' && !currentOrder) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (error || !currentOrder) {
    const isNotFound = error?.status === 404;
    return (
      <div className="max-w-4xl mx-auto px-4 py-8">
        <Alert variant="error" onClose={clearError}>
          <div>
            <p className="font-semibold mb-1">
              {isNotFound ? 'Pedido não encontrado' : 'Erro ao carregar pedido'}
            </p>
            <p>{error?.message || 'Pedido não encontrado'}</p>
            {error?.details && Object.keys(error.details).length > 0 && (
              <div className="mt-2 text-sm">
                <ul className="list-disc list-inside space-y-1">
                  {Object.entries(error.details).map(([field, message]) => (
                    <li key={field}>
                      <strong>{field}:</strong> {message}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </Alert>
        <div className="mt-6">
          <Button onClick={() => navigate('/orders')}>
            Voltar para Lista
          </Button>
        </div>
      </div>
    );
  }

  const statusInfo = getOrderStatusInfo(currentOrder.status);
  const riskInfo = getRiskLevelInfo(currentOrder.riskLevel);

  const handleRefreshPaymentStatus = async () => {
    if (!id) return;
    
    setIsRefreshingPayment(true);
    setRefreshError(null);
    clearError();
    
    try {
      await refreshPaymentStatus(id);
    } catch (error) {
      // Capturar erro para exibir na UI
      const apiError = error as ApiError;
      setRefreshError(apiError);
      console.error('Erro ao atualizar status do pagamento:', error);
    } finally {
      setIsRefreshingPayment(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Pedido {currentOrder.orderNumber}
          </h1>
          <p className="mt-2 text-gray-600">
            Detalhes completos do pedido
          </p>
        </div>
        <div className="flex gap-3">
          {currentOrder.paymentId && (
            <Button
              variant="outline"
              onClick={handleRefreshPaymentStatus}
              disabled={isRefreshingPayment}
            >
              {isRefreshingPayment ? 'Atualizando...' : '🔄 Atualizar Status Pagamento'}
            </Button>
          )}
          <Button variant="secondary" onClick={() => navigate('/orders')}>
            ← Voltar
          </Button>
        </div>
      </div>

      {refreshError && refreshError.status !== 404 && (
        <Alert variant="error" onClose={() => setRefreshError(null)} className="mb-6">
          <div>
            <p className="font-semibold mb-1">Erro ao atualizar status</p>
            <p>{refreshError.message || 'Erro desconhecido'}</p>
            {refreshError.details && Object.keys(refreshError.details).length > 0 && (
              <div className="mt-2 text-sm">
                <ul className="list-disc list-inside space-y-1">
                  {Object.entries(refreshError.details).map(([field, message]) => (
                    <li key={field}>
                      <strong>{field}:</strong> {String(message)}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </Alert>
      )}

      <div className="space-y-6">
        {/* Status e Risco */}
        <Card>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <p className="text-sm text-gray-500 mb-2">Status</p>
              <span
                className={`inline-flex items-center px-4 py-2 rounded-lg text-sm font-medium ${statusInfo.color}`}
              >
                {statusInfo.icon} {statusInfo.label}
              </span>
            </div>
            <div>
              <p className="text-sm text-gray-500 mb-2">Nível de Risco</p>
              <span
                className={`inline-flex items-center px-4 py-2 rounded-lg text-sm font-medium ${riskInfo.color}`}
              >
                {riskInfo.icon} {riskInfo.label}
              </span>
            </div>
          </div>
        </Card>

        {/* Informações do Cliente */}
        <Card>
          <h2 className="text-xl font-semibold mb-4">Cliente</h2>
          <div className="space-y-2">
            <div>
              <p className="text-sm text-gray-500">Nome</p>
              <p className="font-medium">{currentOrder.customerName}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500">Email</p>
              <p className="font-medium">{currentOrder.customerEmail}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500">ID</p>
              <p className="font-mono text-sm">{currentOrder.customerId}</p>
            </div>
          </div>
        </Card>

        {/* Itens do Pedido */}
        <Card>
          <h2 className="text-xl font-semibold mb-4">Itens do Pedido</h2>
          <div className="space-y-4">
            {currentOrder.items.map((item, index) => (
              <div
                key={index}
                className="flex items-center justify-between p-4 bg-gray-50 rounded-lg"
              >
                <div className="flex-1">
                  <p className="font-medium">{item.productName}</p>
                  <p className="text-sm text-gray-500">
                    Quantidade: {item.quantity} × {formatCurrency(item.unitPrice)}
                  </p>
                </div>
                <p className="font-semibold text-lg">
                  {formatCurrency(item.subtotal)}
                </p>
              </div>
            ))}
            <div className="pt-4 border-t border-gray-200">
              <div className="flex items-center justify-between">
                <p className="text-lg font-semibold">Total</p>
                <p className="text-2xl font-bold text-blue-600">
                  {formatCurrency(currentOrder.totalAmount)}
                </p>
              </div>
            </div>
          </div>
        </Card>

        {/* Informações Adicionais */}
        <Card>
          <h2 className="text-xl font-semibold mb-4">Informações Adicionais</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-gray-500">ID do Pedido</p>
              <p className="font-mono text-sm">{currentOrder.id}</p>
            </div>
            {currentOrder.paymentId && (
              <div>
                <p className="text-sm text-gray-500">ID do Pagamento</p>
                <p className="font-mono text-sm">{currentOrder.paymentId}</p>
              </div>
            )}
            <div>
              <p className="text-sm text-gray-500">Criado em</p>
              <p className="font-medium">{formatDate(currentOrder.createdAt)}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500">Atualizado em</p>
              <p className="font-medium">{formatDate(currentOrder.updatedAt)}</p>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};

