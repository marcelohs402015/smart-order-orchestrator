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
  OrderStatus,
} from '../types';

/**
 * Cria um novo pedido executando a saga completa.
 * 
 * <p>Orquestra os 3 passos da saga:
 * 1. Criar pedido
 * 2. Processar pagamento
 * 3. Analisar risco</p>
 * 
 * <p>Retorna resultado com pedido criado e ID da execução da saga
 * para rastreamento e observabilidade.</p>
 * 
 * <p>Suporta idempotência através do campo opcional idempotencyKey.
 * Se não fornecido, o backend gera automaticamente.</p>
 * 
 * @param request Dados do pedido a ser criado (incluindo idempotencyKey opcional)
 * @returns Promise com resultado da criação (sucesso, em progresso ou falha)
 * @throws ApiError se houver erro na requisição (validação, servidor, etc)
 */
export const createOrder = async (
  request: CreateOrderRequest
): Promise<CreateOrderResponse> => {
  try {
    // URL final será: baseURL + '/orders' = '/api/v1' + '/orders' = '/api/v1/orders'
    // Com proxy do Vite: '/api/v1/orders' → 'http://localhost:8080/api/v1/orders'
    const response = await apiClient.post<CreateOrderResponse>('/orders', request);
    return response.data;
  } catch (error: any) {
    // Se for um CreateOrderResponse com falha (saga falhou), retornar diretamente
    // O interceptor do Axios marca isso com isCreateOrderResponse: true
    if (error?.isCreateOrderResponse && error.data) {
      return error.data as CreateOrderResponse;
    }
    
    // Caso contrário, re-throw para que o store possa tratar adequadamente
    throw error;
  }
};

/**
 * Busca um pedido pelo ID.
 * 
 * @param id ID do pedido (UUID)
 * @returns Promise com o pedido encontrado
 * @throws ApiError se pedido não for encontrado (404) ou outro erro ocorrer
 */
export const getOrderById = async (id: string): Promise<OrderResponse> => {
  try {
    const response = await apiClient.get<OrderResponse>(`/orders/${id}`);
    return response.data;
  } catch (error) {
    // Re-throw para que o store possa tratar adequadamente
    throw error;
  }
};

/**
 * Busca um pedido pelo número do pedido.
 * 
 * @param orderNumber Número do pedido (ex: ORD-1234567890)
 * @returns Promise com o pedido encontrado
 * @throws ApiError se pedido não for encontrado (404) ou outro erro ocorrer
 */
export const getOrderByNumber = async (
  orderNumber: string
): Promise<OrderResponse> => {
  try {
    const response = await apiClient.get<OrderResponse>(
      `/orders/number/${orderNumber}`
    );
    return response.data;
  } catch (error) {
    // Re-throw para que o store possa tratar adequadamente
    throw error;
  }
};

/**
 * Lista todos os pedidos do sistema ou filtra por status.
 * 
 * @param status Status opcional para filtrar pedidos (PENDING, PAID, PAYMENT_FAILED, CANCELED)
 * @returns Promise com lista de pedidos (pode ser vazia se não houver pedidos)
 * @throws ApiError se houver erro na requisição
 */
export const getAllOrders = async (status?: OrderStatus): Promise<OrderResponse[]> => {
  try {
    const params = status ? { status } : {};
    const response = await apiClient.get<OrderResponse[]>('/orders', { params });
    return response.data;
  } catch (error) {
    // Re-throw para que o store possa tratar adequadamente
    throw error;
  }
};

/**
 * Atualiza o status do pagamento de um pedido consultando o gateway externo.
 * 
 * <p>Este endpoint consulta o status atual do pagamento no gateway (AbacatePay)
 * e atualiza o pedido no banco de dados se o status tiver mudado.</p>
 * 
 * @param orderId ID do pedido (UUID)
 * @returns Promise com o pedido atualizado
 * @throws ApiError se pedido não for encontrado (404) ou outro erro ocorrer
 */
export const refreshPaymentStatus = async (orderId: string): Promise<OrderResponse> => {
  try {
    const response = await apiClient.post<OrderResponse>(
      `/payments/orders/${orderId}/refresh-status`
    );
    return response.data;
  } catch (error) {
    // Re-throw para que o store possa tratar adequadamente
    throw error;
  }
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

