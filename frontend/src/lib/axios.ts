import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { ApiError } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (import.meta.env.DEV && config.data) {
      console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, config.data);
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error: AxiosError) => {
    if (error.response?.status === 400) {
      const data = error.response.data as any;
      if (data && typeof data === 'object' && 'success' in data && 'sagaExecutionId' in data) {
        return Promise.reject({
          isCreateOrderResponse: true,
          data: data,
          status: 400,
        });
      }
    }

    const apiError: ApiError = {
      message: 'Erro ao processar requisição',
      timestamp: new Date().toISOString(),
    };

    if (error.response) {
      const data = error.response.data as any;
      apiError.status = error.response.status;
      apiError.error = data.error || error.response.statusText;
      
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
      
      if (data.details && typeof data.details === 'object') {
        apiError.details = data.details as Record<string, string>;
        
        if (error.response.status === 400 && Object.keys(apiError.details).length > 0) {
          const fieldCount = Object.keys(apiError.details).length;
          apiError.message = `Erro de validação em ${fieldCount} campo(s). Verifique os detalhes abaixo.`;
        }
      }
      
      if (!apiError.details && data.errors && typeof data.errors === 'object') {
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
      apiError.message = 'Servidor não respondeu. Verifique se o backend está rodando em http://localhost:8080 e se o proxy do Vite está configurado corretamente.';
    } else {
      apiError.message = error.message || 'Erro desconhecido';
    }

    return Promise.reject(apiError);
  }
);

export default apiClient;
