import { create } from 'zustand';
import {
  OrderResponse,
  CreateOrderRequest,
  CreateOrderResponse,
  ApiError,
  LoadingState,
  OrderStatus,
} from '../types';
import * as orderService from '../services/orderService';

interface OrderState {
  orders: OrderResponse[];
  currentOrder: OrderResponse | null;
  failedPaymentOrders: OrderResponse[];
  loading: LoadingState;
  error: ApiError | null;
  validationErrors: Record<string, string> | null;

  fetchOrders: (status?: OrderStatus) => Promise<void>;
  fetchOrderById: (id: string) => Promise<void>;
  fetchFailedPaymentOrders: () => Promise<void>;
  createOrder: (request: CreateOrderRequest) => Promise<CreateOrderResponse>;
  refreshPaymentStatus: (orderId: string) => Promise<void>;
  clearError: () => void;
  clearValidationErrors: () => void;
  clearCurrentOrder: () => void;
}

export const useOrderStore = create<OrderState>((set, get) => ({
  orders: [],
  currentOrder: null,
  failedPaymentOrders: [],
  loading: 'idle',
  error: null,
  validationErrors: null,

  fetchOrders: async (status?: OrderStatus) => {
    set({ loading: 'loading', error: null, validationErrors: null });
    try {
      const orders = await orderService.getAllOrders(status);
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

  fetchFailedPaymentOrders: async () => {
    try {
      const failedOrders = await orderService.getAllOrders(OrderStatus.PAYMENT_FAILED);
      set({ failedPaymentOrders: failedOrders });
    } catch (error) {
      console.warn('Erro ao buscar pedidos com falha de pagamento:', error);
    }
  },

  createOrder: async (request: CreateOrderRequest) => {
    set({ loading: 'loading', error: null, validationErrors: null });
    try {
      const response = await orderService.createOrder(request);
      
      if (response.success && response.order) {
        const currentOrders = get().orders;
        set({
          orders: [response.order, ...currentOrders],
          currentOrder: response.order,
          loading: 'success',
          error: null,
          validationErrors: null,
        });
      } else if (response.sagaExecutionId && !response.success) {
        set({
          loading: 'success',
          error: {
            message: response.errorMessage || 'Pedido está sendo processado. Aguarde a conclusão.',
            status: 202,
          },
          validationErrors: null,
        });
      } else {
        set({
          loading: 'error',
          error: {
            message: response.errorMessage || 'Erro ao processar pedido. A saga falhou durante a execução.',
            status: 400,
            isBusinessError: true,
          },
          validationErrors: null,
        });
      }
      
      return response;
    } catch (error) {
      const apiError = error as ApiError;
      const validationErrors = apiError.details || null;
      
      set({
        loading: 'error',
        error: apiError,
        validationErrors,
      });
      throw error;
    }
  },

  refreshPaymentStatus: async (orderId: string) => {
    set({ error: null, validationErrors: null });
    try {
      const updatedOrder = await orderService.refreshPaymentStatus(orderId);
      
      const currentOrder = get().currentOrder;
      if (currentOrder && currentOrder.id === orderId) {
        set({ currentOrder: updatedOrder });
      }
      
      const currentOrders = get().orders;
      const updatedOrders = currentOrders.map((order) =>
        order.id === orderId ? updatedOrder : order
      );
      
      set({
        orders: updatedOrders,
        error: null,
        validationErrors: null,
      });
    } catch (error) {
      const apiError = error as ApiError;
      set({
        error: apiError,
        validationErrors: apiError.details || null,
      });
      throw error;
    }
  },

  clearError: () => {
    set({ error: null, validationErrors: null });
  },

  clearValidationErrors: () => {
    set({ validationErrors: null });
  },

  clearCurrentOrder: () => {
    set({ currentOrder: null });
  },
}));
