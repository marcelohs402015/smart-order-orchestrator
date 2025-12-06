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

  // Actions
  fetchOrders: () => Promise<void>;
  fetchOrderById: (id: string) => Promise<void>;
  createOrder: (request: CreateOrderRequest) => Promise<CreateOrderResponse>;
  clearError: () => void;
  clearCurrentOrder: () => void;
}

export const useOrderStore = create<OrderState>((set, get) => ({
  // Estado inicial
  orders: [],
  currentOrder: null,
  loading: 'idle',
  error: null,

  // Buscar todos os pedidos
  fetchOrders: async () => {
    set({ loading: 'loading', error: null });
    try {
      const orders = await orderService.getAllOrders();
      set({ orders, loading: 'success', error: null });
    } catch (error) {
      const apiError = error as ApiError;
      set({
        loading: 'error',
        error: apiError,
      });
    }
  },

  // Buscar pedido por ID
  fetchOrderById: async (id: string) => {
    set({ loading: 'loading', error: null, currentOrder: null });
    try {
      const order = await orderService.getOrderById(id);
      set({ currentOrder: order, loading: 'success', error: null });
    } catch (error) {
      const apiError = error as ApiError;
      set({
        loading: 'error',
        error: apiError,
        currentOrder: null,
      });
    }
  },

  // Criar novo pedido
  createOrder: async (request: CreateOrderRequest) => {
    set({ loading: 'loading', error: null });
    try {
      const response = await orderService.createOrder(request);
      
      if (response.success && response.order) {
        // Adicionar novo pedido à lista
        const currentOrders = get().orders;
        set({
          orders: [response.order, ...currentOrders],
          currentOrder: response.order,
          loading: 'success',
          error: null,
        });
      } else {
        // Pedido criado mas com erro na saga
        set({
          loading: 'error',
          error: {
            message: response.errorMessage || 'Erro ao processar pedido',
          },
        });
      }
      
      return response;
    } catch (error) {
      const apiError = error as ApiError;
      set({
        loading: 'error',
        error: apiError,
      });
      throw error;
    }
  },

  // Limpar erro
  clearError: () => {
    set({ error: null });
  },

  // Limpar pedido atual
  clearCurrentOrder: () => {
    set({ currentOrder: null });
  },
}));

