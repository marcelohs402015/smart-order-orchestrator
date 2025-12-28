import { useState, useCallback } from 'react';
import { getOrderByNumber } from '../services/orderService';
import { OrderResponse, ApiError } from '../types';

interface UseOrderSearchReturn {
  searchResult: OrderResponse | null;
  searchLoading: boolean;
  searchError: string | null;
  searchByNumber: (orderNumber: string) => Promise<void>;
  clearSearch: () => void;
}

export const useOrderSearch = (): UseOrderSearchReturn => {
  const [searchResult, setSearchResult] = useState<OrderResponse | null>(null);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);

  const searchByNumber = useCallback(async (orderNumber: string) => {
    if (!orderNumber.trim()) {
      setSearchError('Digite um número de pedido');
      setSearchResult(null);
      return;
    }

    setSearchLoading(true);
    setSearchError(null);
    setSearchResult(null);

    try {
      const order = await getOrderByNumber(orderNumber.trim());
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
  }, []);

  const clearSearch = useCallback(() => {
    setSearchResult(null);
    setSearchError(null);
  }, []);

  return {
    searchResult,
    searchLoading,
    searchError,
    searchByNumber,
    clearSearch,
  };
};
