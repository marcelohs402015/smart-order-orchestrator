import { useEffect, useCallback } from 'react';
import { useOrderStore } from '../store/orderStore';
import { OrderResponse, LoadingState, ApiError } from '../types';

interface UseOrderReturn {
  order: OrderResponse | null;
  loading: LoadingState;
  error: ApiError | null;
  refetch: (id: string) => Promise<void>;
  refreshPaymentStatus: (orderId: string) => Promise<void>;
  clearError: () => void;
}

export const useOrder = (
  orderId: string | undefined,
  autoFetch: boolean = true
): UseOrderReturn => {
  const currentOrder = useOrderStore((state) => state.currentOrder);
  const loading = useOrderStore((state) => state.loading);
  const error = useOrderStore((state) => state.error);
  const fetchOrderById = useOrderStore((state) => state.fetchOrderById);
  const refreshPaymentStatus = useOrderStore((state) => state.refreshPaymentStatus);
  const clearError = useOrderStore((state) => state.clearError);

  useEffect(() => {
    if (autoFetch && orderId) {
      fetchOrderById(orderId);
    }
  }, [autoFetch, orderId, fetchOrderById]);

  const handleRefetch = useCallback(
    (id: string) => {
      return fetchOrderById(id);
    },
    [fetchOrderById]
  );

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
    refetch: handleRefetch,
    refreshPaymentStatus: handleRefreshPaymentStatus,
    clearError,
  };
};
