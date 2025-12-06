/**
 * Serviço de API para operações de pedidos.
 * 
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Comunicação com endpoints de pedidos</li>
 *   <li>Conversão de tipos (TypeScript ↔ JSON)</li>
 *   <li>Tratamento de erros</li>
 * </ul>
 */

import apiClient from '../lib/axios';
import {
  CreateOrderRequest,
  CreateOrderResponse,
  OrderResponse,
  ApiError,
} from '../types';

/**
 * Cria um novo pedido executando a saga completa.
 * 
 * @param request Dados do pedido a ser criado
 * @returns Promise com resultado da criação
 */
export const createOrder = async (
  request: CreateOrderRequest
): Promise<CreateOrderResponse> => {
  const response = await apiClient.post<CreateOrderResponse>('/orders', request);
  return response.data;
};

/**
 * Busca um pedido pelo ID.
 * 
 * @param id ID do pedido
 * @returns Promise com o pedido encontrado
 * @throws ApiError se pedido não for encontrado
 */
export const getOrderById = async (id: string): Promise<OrderResponse> => {
  const response = await apiClient.get<OrderResponse>(`/orders/${id}`);
  return response.data;
};

/**
 * Busca um pedido pelo número do pedido.
 * 
 * @param orderNumber Número do pedido (ex: ORD-1234567890)
 * @returns Promise com o pedido encontrado
 * @throws ApiError se pedido não for encontrado
 */
export const getOrderByNumber = async (
  orderNumber: string
): Promise<OrderResponse> => {
  const response = await apiClient.get<OrderResponse>(
    `/orders/number/${orderNumber}`
  );
  return response.data;
};

/**
 * Lista todos os pedidos.
 * 
 * @returns Promise com lista de pedidos
 */
export const getAllOrders = async (): Promise<OrderResponse[]> => {
  const response = await apiClient.get<OrderResponse[]>('/orders');
  return response.data;
};

/**
 * Tipos de erro específicos do serviço.
 */
export class OrderServiceError extends Error {
  constructor(
    message: string,
    public apiError?: ApiError
  ) {
    super(message);
    this.name = 'OrderServiceError';
  }
}

