/**
 * Configuração do Axios para comunicação com a API REST.
 * 
 * <h3>Por que Axios?</h3>
 * <ul>
 *   <li><strong>Interceptors:</strong> Tratamento centralizado de erros e autenticação</li>
 *   <li><strong>Type Safety:</strong> Suporte a TypeScript</li>
 *   <li><strong>Cancelamento:</strong> Cancelar requisições em andamento</li>
 *   <li><strong>Transformação:</strong> Transformar dados automaticamente</li>
 * </ul>
 */

import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { ApiError } from '../types';

/**
 * Base URL da API.
 * Em desenvolvimento, usa proxy do Vite.
 * Em produção, usa variável de ambiente.
 */
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

/**
 * Instância do Axios configurada para a API.
 */
export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000, // 30 segundos
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Interceptor de requisição.
 * Adiciona headers comuns ou tokens de autenticação.
 */
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Adicionar token de autenticação se existir (futuro)
    // const token = localStorage.getItem('token');
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
    
    // Log para debug (apenas em desenvolvimento)
    if (import.meta.env.DEV && config.data) {
      console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, config.data);
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Interceptor de resposta.
 * Trata erros de forma centralizada.
 */
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error: AxiosError) => {
    // Verificar se é um CreateOrderResponse com falha (saga falhou)
    // Nesse caso, não é erro de validação, é erro de negócio que deve ser tratado pelo service
    if (error.response?.status === 400) {
      const data = error.response.data as any;
      // Se tiver success, sagaExecutionId, errorMessage, é um CreateOrderResponse
      if (data && typeof data === 'object' && 'success' in data && 'sagaExecutionId' in data) {
        // É um CreateOrderResponse, não um ErrorResponse
        // Deixar passar para o service tratar (não lançar erro)
        // O service vai tratar no store
        return Promise.reject({
          isCreateOrderResponse: true,
          data: data,
          status: 400,
        });
      }
    }

    // Tratamento centralizado de erros
    const apiError: ApiError = {
      message: 'Erro ao processar requisição',
      timestamp: new Date().toISOString(),
    };

    if (error.response) {
      // Erro com resposta do servidor
      const data = error.response.data as any;
      apiError.status = error.response.status;
      apiError.error = data.error || error.response.statusText;
      
      // Log completo do erro para debug (apenas em desenvolvimento)
      if (import.meta.env.DEV) {
        console.error('[API Error]', {
          status: error.response.status,
          statusText: error.response.statusText,
          url: error.config?.url,
          method: error.config?.method,
          data: data,
          requestData: error.config?.data,
        });
      }
      
      // Mensagens mais específicas baseadas no status
      if (error.response.status === 500) {
        apiError.message = data.message || 'Erro interno do servidor. O backend está respondendo, mas ocorreu um erro ao processar a requisição. Verifique os logs do backend e a configuração do banco de dados.';
      } else if (error.response.status === 404) {
        apiError.message = data.message || 'Recurso não encontrado.';
      } else if (error.response.status === 400) {
        // Para erros 400, usar mensagem do backend ou mensagem genérica
        // Se houver details, a mensagem será complementada na UI
        apiError.message = data.message || 'Dados inválidos. Verifique os campos do formulário.';
      } else {
        apiError.message = data.message || `Erro ${error.response.status}: ${error.response.statusText}`;
      }

      apiError.errors = data.errors;
      
      // Mapear details do backend (Map<String, String>) para Record<string, string>
      // O backend retorna details com erros de validação por campo no formato:
      // { "customerId": "Customer ID is required", "items[0].productId": "Product ID is required", ... }
      if (data.details && typeof data.details === 'object') {
        apiError.details = data.details as Record<string, string>;
        
        // Se houver details, melhorar a mensagem principal
        if (error.response.status === 400 && Object.keys(apiError.details).length > 0) {
          const fieldCount = Object.keys(apiError.details).length;
          apiError.message = `Erro de validação em ${fieldCount} campo(s). Verifique os detalhes abaixo.`;
        }
      }
      
      // Se não tiver details mas tiver errors (formato alternativo)
      if (!apiError.details && data.errors && typeof data.errors === 'object') {
        // Converter errors para details se necessário
        const errorsObj = data.errors as any;
        if (Object.keys(errorsObj).length > 0) {
          apiError.details = {};
          Object.entries(errorsObj).forEach(([key, value]) => {
            if (Array.isArray(value) && value.length > 0) {
              apiError.details![key] = value[0] as string;
            } else if (typeof value === 'string') {
              apiError.details![key] = value;
            }
          });
        }
      }
      
      apiError.path = error.config?.url;
    } else if (error.request) {
      // Requisição feita mas sem resposta
      apiError.message = 'Servidor não respondeu. Verifique se o backend está rodando em http://localhost:8080 e se o proxy do Vite está configurado corretamente.';
    } else {
      // Erro ao configurar requisição
      apiError.message = error.message || 'Erro desconhecido';
    }

    return Promise.reject(apiError);
  }
);

export default apiClient;

