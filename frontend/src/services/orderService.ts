import apiClient from '../lib/axios';
import {
  CreateOrderRequest,
  CreateOrderResponse,
  OrderResponse,
  ApiError,
  OrderStatus,
} from '../types';

export const createOrder = async (
  request: CreateOrderRequest
): Promise<CreateOrderResponse> => {
  try {
    const response = await apiClient.post<CreateOrderResponse>('/orders', request);
    return response.data;
  } catch (error: any) {
    if (error?.isCreateOrderResponse && error.data) {
      return error.data as CreateOrderResponse;
    }
    
    throw error;
  }
};

export const getOrderById = async (id: string): Promise<OrderResponse> => {
  try {
    const response = await apiClient.get<OrderResponse>(`/orders/${id}`);
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const getOrderByNumber = async (
  orderNumber: string
): Promise<OrderResponse> => {
  try {
    const response = await apiClient.get<OrderResponse>(
      `/orders/number/${orderNumber}`
    );
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const getAllOrders = async (status?: OrderStatus): Promise<OrderResponse[]> => {
  try {
    const params = status ? { status } : {};
    const response = await apiClient.get<OrderResponse[]>('/orders', { params });
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const refreshPaymentStatus = async (orderId: string): Promise<OrderResponse> => {
  try {
    const response = await apiClient.post<OrderResponse>(
      `/payments/orders/${orderId}/refresh-status`
    );
    return response.data;
  } catch (error) {
    throw error;
  }
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
