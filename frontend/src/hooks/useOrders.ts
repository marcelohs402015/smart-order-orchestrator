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

export const useOrders = (
  initialStatus?: OrderStatus,
  autoFetch: boolean = true
): UseOrdersReturn => {
  const orders = useOrderStore((state) => state.orders);
  const loading = useOrderStore((state) => state.loading);
  const error = useOrderStore((state) => state.error);
  const fetchOrders = useOrderStore((state) => state.fetchOrders);
  const clearError = useOrderStore((state) => state.clearError);

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
  }, [autoFetch, initialStatus, refetch]);

  return {
    orders,
    loading,
    error,
    refetch,
    clearError,
  };
};
