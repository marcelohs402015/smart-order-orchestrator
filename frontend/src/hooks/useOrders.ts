import { useEffect, useCallback } from 'react';
import { useOrderStore } from '../store/orderStore';
import { OrderStatus, OrderResponse, LoadingState, ApiError } from '../types';

interface UseOrdersReturn {
  orders: OrderResponse[];
  loading: LoadingState;
  error: ApiError | null;
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

  useEffect(() => {
    if (autoFetch) {
      fetchOrders(initialStatus);
    }
  }, [autoFetch, initialStatus, fetchOrders]);

  const handleRefetch = useCallback(
    (status?: OrderStatus) => {
      return fetchOrders(status);
    },
    [fetchOrders]
  );

  return {
    orders,
    loading,
    error,
    refetch: handleRefetch,
    clearError,
  };
};
