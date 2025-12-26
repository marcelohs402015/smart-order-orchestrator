/**
 * Custom hook para gerenciar um pedido específico.
 * 
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Encapsula lógica de busca de pedido por ID</li>
 *   <li>Gerencia loading e error states</li>
 *   <li>Evita dependências circulares no useEffect</li>
 * </ul>
 */

import { useEffect, useCallback } from 'react';
import { useOrderStore } from '../store/orderStore';

interface UseOrderReturn {
  order: ReturnType<typeof useOrderStore>['currentOrder'];
  loading: ReturnType<typeof useOrderStore>['loading'];
  error: ReturnType<typeof useOrderStore>['error'];
  refetch: (id: string) => Promise<void>;
  refreshPaymentStatus: (orderId: string) => Promise<void>;
  clearError: () => void;
}

/**
 * Hook para buscar e gerenciar um pedido específico.
 * 
 * @param orderId ID do pedido
 * @param autoFetch Se deve buscar automaticamente ao montar (padrão: true)
 * @returns Objeto com order, loading, error e funções de controle
 */
export const useOrder = (
  orderId: string | undefined,
  autoFetch: boolean = true
): UseOrderReturn => {
  // Zustand garante que as funções são estáveis, mas vamos usar selectors para melhor performance
  const currentOrder = useOrderStore((state) => state.currentOrder);
  const loading = useOrderStore((state) => state.loading);
  const error = useOrderStore((state) => state.error);
  const fetchOrderById = useOrderStore((state) => state.fetchOrderById);
  const refreshPaymentStatus = useOrderStore((state) => state.refreshPaymentStatus);
  const clearError = useOrderStore((state) => state.clearError);

  // useCallback garante que a função seja estável e não cause loops no useEffect
  const refetch = useCallback(
    (id: string) => {
      return fetchOrderById(id);
    },
    [fetchOrderById]
  );

  useEffect(() => {
    if (autoFetch && orderId) {
      refetch(orderId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [autoFetch, orderId]); // Removido refetch da dependência pois é estável

  const handleRefreshPaymentStatus = useCallback(
    (id: string) => {
      return refreshPaymentStatus(id);
    },
    [refreshPaymentStatus]
  );

  return {
    order: currentOrder,
    loading,
    error,
    refetch,
    refreshPaymentStatus: handleRefreshPaymentStatus,
    clearError,
  };
};

