/**
 * Store Zustand para gerenciamento de estado de pedidos.
 * 
 * <h3>Por que Zustand?</h3>
 * <ul>
 *   <li><strong>Leve:</strong> ~1KB minificado</li>
 *   <li><strong>Simples:</strong> API intuitiva, sem boilerplate</li>
 *   <li><strong>TypeScript:</strong> Suporte completo a tipos</li>
 *   <li><strong>Performance:</strong> Re-renderiza apenas componentes que usam estado alterado</li>
 * </ul>
 * 
 * <h3>Estrutura do Store:</h3>
 * <ul>
 *   <li><strong>orders:</strong> Lista de pedidos</li>
 *   <li><strong>currentOrder:</strong> Pedido sendo visualizado/editado</li>
 *   <li><strong>loading:</strong> Estado de carregamento</li>
 *   <li><strong>error:</strong> Erro atual (se houver)</li>
 * </ul>
 */

import { create } from 'zustand';
import {
  OrderResponse,
  CreateOrderRequest,
  CreateOrderResponse,
  ApiError,
  LoadingState,
} from '../types';
import * as orderService from '../services/orderService';

interface OrderState {
  // Estado
  orders: OrderResponse[];
  currentOrder: OrderResponse | null;
  loading: LoadingState;
  error: ApiError | null;
  validationErrors: Record<string, string> | null; // Erros de validação por campo

  // Actions
  fetchOrders: () => Promise<void>;
  fetchOrderById: (id: string) => Promise<void>;
  createOrder: (request: CreateOrderRequest) => Promise<CreateOrderResponse>;
  clearError: () => void;
  clearValidationErrors: () => void;
  clearCurrentOrder: () => void;
}

export const useOrderStore = create<OrderState>((set, get) => ({
  // Estado inicial
  orders: [],
  currentOrder: null,
  loading: 'idle',
  error: null,
  validationErrors: null,

  // Buscar todos os pedidos
  fetchOrders: async () => {
    set({ loading: 'loading', error: null, validationErrors: null });
    try {
      const orders = await orderService.getAllOrders();
      set({ orders, loading: 'success', error: null, validationErrors: null });
    } catch (error) {
      const apiError = error as ApiError;
      set({
        loading: 'error',
        error: apiError,
        validationErrors: apiError.details || null,
      });
    }
  },

  // Buscar pedido por ID
  fetchOrderById: async (id: string) => {
    set({ loading: 'loading', error: null, currentOrder: null, validationErrors: null });
    try {
      const order = await orderService.getOrderById(id);
      set({ currentOrder: order, loading: 'success', error: null, validationErrors: null });
    } catch (error) {
      const apiError = error as ApiError;
      set({
        loading: 'error',
        error: apiError,
        currentOrder: null,
        validationErrors: apiError.details || null,
      });
    }
  },

  // Criar novo pedido
  createOrder: async (request: CreateOrderRequest) => {
    set({ loading: 'loading', error: null, validationErrors: null });
    try {
      const response = await orderService.createOrder(request);
      
      if (response.success && response.order) {
        // Sucesso: Pedido criado com sucesso (201)
        const currentOrders = get().orders;
        set({
          orders: [response.order, ...currentOrders],
          currentOrder: response.order,
          loading: 'success',
          error: null,
          validationErrors: null,
        });
      } else if (response.sagaExecutionId && !response.success) {
        // Saga em progresso (202 ACCEPTED) - não é erro, mas estado intermediário
        // O backend retorna 202 quando a saga já está em progresso (idempotência)
        set({
          loading: 'success', // Não é erro, mas sucesso parcial
          error: {
            message: response.errorMessage || 'Pedido está sendo processado. Aguarde a conclusão.',
            status: 202,
          },
          validationErrors: null,
        });
      } else {
        // Falha na saga (400)
        set({
          loading: 'error',
          error: {
            message: response.errorMessage || 'Erro ao processar pedido',
            status: 400,
          },
          validationErrors: null,
        });
      }
      
      return response;
    } catch (error) {
      const apiError = error as ApiError;
      // Extrair erros de validação (details) se disponível
      const validationErrors = apiError.details || null;
      
      set({
        loading: 'error',
        error: apiError,
        validationErrors,
      });
      throw error;
    }
  },

  // Limpar erro
  clearError: () => {
    set({ error: null, validationErrors: null });
  },

  // Limpar erros de validação
  clearValidationErrors: () => {
    set({ validationErrors: null });
  },

  // Limpar pedido atual
  clearCurrentOrder: () => {
    set({ currentOrder: null });
  },
}));

