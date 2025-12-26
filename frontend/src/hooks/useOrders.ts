/**
 * Custom hook para gerenciar lista de pedidos.
 * 
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Encapsula lógica de busca de pedidos</li>
 *   <li>Gerencia loading e error states</li>
 *   <li>Evita dependências circulares no useEffect</li>
 * </ul>
 */

import { useEffect, useCallback } from 'react';
import { useOrderStore } from '../store/orderStore';
import { OrderStatus } from '../types';

interface UseOrdersReturn {
  orders: ReturnType<typeof useOrderStore>['orders'];
  loading: ReturnType<typeof useOrderStore>['loading'];
  error: ReturnType<typeof useOrderStore>['error'];
  refetch: (status?: OrderStatus) => Promise<void>;
  clearError: () => void;
}

/**
 * Hook para buscar e gerenciar lista de pedidos.
 * 
 * @param initialStatus Status inicial para filtrar pedidos (opcional)
 * @param autoFetch Se deve buscar automaticamente ao montar (padrão: true)
 * @returns Objeto com orders, loading, error e funções de controle
 */
export const useOrders = (
  initialStatus?: OrderStatus,
  autoFetch: boolean = true
): UseOrdersReturn => {
  // Zustand garante que as funções são estáveis, mas vamos usar selectors para melhor performance
  const orders = useOrderStore((state) => state.orders);
  const loading = useOrderStore((state) => state.loading);
  const error = useOrderStore((state) => state.error);
  const fetchOrders = useOrderStore((state) => state.fetchOrders);
  const clearError = useOrderStore((state) => state.clearError);

  // useCallback garante que a função seja estável e não cause loops no useEffect
  const refetch = useCallback(
    (status?: OrderStatus) => {
      return fetchOrders(status);
    },
    [fetchOrders]
  );

  useEffect(() => {
    if (autoFetch) {
      refetch(initialStatus);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [autoFetch, initialStatus]); // Removido refetch da dependência pois é estável

  return {
    orders,
    loading,
    error,
    refetch,
    clearError,
  };
};

