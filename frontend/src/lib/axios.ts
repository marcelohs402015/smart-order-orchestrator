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
      
      // Mensagens mais específicas baseadas no status
      if (error.response.status === 500) {
        apiError.message = data.message || 'Erro interno do servidor. O backend está respondendo, mas ocorreu um erro ao processar a requisição. Verifique os logs do backend e a configuração do banco de dados.';
      } else if (error.response.status === 404) {
        apiError.message = data.message || 'Recurso não encontrado.';
      } else if (error.response.status === 400) {
        apiError.message = data.message || 'Dados inválidos. Verifique os campos do formulário.';
      } else {
        apiError.message = data.message || `Erro ${error.response.status}: ${error.response.statusText}`;
      }
      
      apiError.errors = data.errors;
      // Mapear details do backend (Map<String, String>) para Record<string, string>
      if (data.details && typeof data.details === 'object') {
        apiError.details = data.details as Record<string, string>;
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

