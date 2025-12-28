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
import { logger } from '../utils/logger';

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

const resetErrorState = () => ({
  error: null,
  validationErrors: null,
});

const resetLoadingState = () => ({
  loading: 'loading' as LoadingState,
  ...resetErrorState(),
});

const setSuccessState = () => ({
  loading: 'success' as LoadingState,
  ...resetErrorState(),
});

const setErrorState = (apiError: ApiError) => ({
  loading: 'error' as LoadingState,
  error: apiError,
  validationErrors: apiError.details || null,
});

export const useOrderStore = create<OrderState>((set, get) => ({
  orders: [],
  currentOrder: null,
  failedPaymentOrders: [],
  loading: 'idle',
  error: null,
  validationErrors: null,

  fetchOrders: async (status?: OrderStatus) => {
    set(resetLoadingState());
    try {
      const orders = await orderService.getAllOrders(status);
      set({ orders, ...setSuccessState() });
    } catch (error) {
      const apiError = error as ApiError;
      set(setErrorState(apiError));
    }
  },

  fetchOrderById: async (id: string) => {
    set({ ...resetLoadingState(), currentOrder: null });
    try {
      const order = await orderService.getOrderById(id);
      set({ currentOrder: order, ...setSuccessState() });
    } catch (error) {
      const apiError = error as ApiError;
      set({
        currentOrder: null,
        ...setErrorState(apiError),
      });
    }
  },

  fetchFailedPaymentOrders: async () => {
    try {
      const failedOrders = await orderService.getAllOrders(OrderStatus.PAYMENT_FAILED);
      set({ failedPaymentOrders: failedOrders });
    } catch (error) {
      logger.warn('Erro ao buscar pedidos com falha de pagamento', { error });
    }
  },

  createOrder: async (request: CreateOrderRequest) => {
    set(resetLoadingState());
    try {
      const response = await orderService.createOrder(request);
      
      if (response.success && response.order) {
        const currentOrders = get().orders;
        set({
          orders: [response.order, ...currentOrders],
          currentOrder: response.order,
          ...setSuccessState(),
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
      set(setErrorState(apiError));
      throw error;
    }
  },

  refreshPaymentStatus: async (orderId: string) => {
    set(resetErrorState());
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
        ...resetErrorState(),
      });
    } catch (error) {
      const apiError = error as ApiError;
      set(setErrorState(apiError));
      throw error;
    }
  },

  clearError: () => {
    set(resetErrorState());
  },

  clearValidationErrors: () => {
    set({ validationErrors: null });
  },

  clearCurrentOrder: () => {
    set({ currentOrder: null });
  },
}));
