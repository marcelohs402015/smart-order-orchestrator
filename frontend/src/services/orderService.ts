import apiClient from '../lib/axios';
import {
  CreateOrderRequest,
  CreateOrderResponse,
  OrderResponse,
  ApiError,
  OrderStatus,
} from '../types';

interface CreateOrderErrorResponse {
  isCreateOrderResponse?: boolean;
  data?: CreateOrderResponse;
  status?: number;
}

export const createOrder = async (
  request: CreateOrderRequest
): Promise<CreateOrderResponse> => {
  try {
    const response = await apiClient.post<CreateOrderResponse>('/orders', request);
    return response.data;
  } catch (error) {
    const createOrderError = error as CreateOrderErrorResponse;
    if (createOrderError?.isCreateOrderResponse && createOrderError.data) {
      return createOrderError.data as CreateOrderResponse;
    }
    
    throw error;
  }
};

export const getOrderById = async (id: string): Promise<OrderResponse> => {
  const response = await apiClient.get<OrderResponse>(`/orders/${id}`);
  return response.data;
};

export const getOrderByNumber = async (
  orderNumber: string
): Promise<OrderResponse> => {
  const response = await apiClient.get<OrderResponse>(
    `/orders/number/${orderNumber}`
  );
  return response.data;
};

export const getAllOrders = async (status?: OrderStatus): Promise<OrderResponse[]> => {
  const params = status ? { status } : {};
  const response = await apiClient.get<OrderResponse[]>('/orders', { params });
  return response.data;
};

export const refreshPaymentStatus = async (orderId: string): Promise<OrderResponse> => {
  const response = await apiClient.post<OrderResponse>(
    `/payments/orders/${orderId}/refresh-status`
  );
  return response.data;
};

export class OrderServiceError extends Error {
  constructor(
    message: string,
    public apiError?: ApiError
  ) {
    super(message);
    this.name = 'OrderServiceError';
  }
}
