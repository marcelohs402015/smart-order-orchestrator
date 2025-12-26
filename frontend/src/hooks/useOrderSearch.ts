/**
 * Custom hook para busca de pedido por número.
 * 
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Encapsula lógica de busca por número de pedido</li>
 *   <li>Gerencia estado local de busca</li>
 *   <li>Isola lógica de busca do componente</li>
 * </ul>
 */

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

/**
 * Hook para buscar pedido por número.
 * 
 * @returns Objeto com resultado da busca, loading, error e funções de controle
 */
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

