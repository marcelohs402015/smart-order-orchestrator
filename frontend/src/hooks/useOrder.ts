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
  }, [autoFetch, orderId, refetch]);

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
