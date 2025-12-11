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
    const response = await apiClient.post<CreateOrderResponse>('/orders', request);
    return response.data;
  } catch (error) {
    // Re-throw para que o store possa tratar adequadamente
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
 * Lista todos os pedidos do sistema.
 * 
 * @returns Promise com lista de pedidos (pode ser vazia se não houver pedidos)
 * @throws ApiError se houver erro na requisição
 */
export const getAllOrders = async (): Promise<OrderResponse[]> => {
  try {
    const response = await apiClient.get<OrderResponse[]>('/orders');
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

