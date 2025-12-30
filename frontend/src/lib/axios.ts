import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { ApiError } from '../types';
import { logger } from '../utils/logger';

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
    const logData: Record<string, unknown> = {
      method: config.method?.toUpperCase(),
      url: config.url,
      baseURL: config.baseURL,
    };

    if (config.data) {
      logData.data = config.data;
    }

    if (config.params) {
      logData.params = config.params;
    }

    logger.log('API Request', logData);

    return config;
  },
  (error) => {
    logger.error('API Request Error', error);
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error: AxiosError) => {
    if (error.response?.status === 400) {
      const data = error.response.data as Record<string, unknown>;
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
      const data = error.response.data as Record<string, unknown>;
      apiError.status = error.response.status;
      apiError.error = (typeof data.error === 'string' ? data.error : error.response.statusText) || error.response.statusText;
      
      const errorLog: Record<string, unknown> = {
        status: error.response.status,
        statusText: error.response.statusText,
        url: error.config?.url,
        method: error.config?.method?.toUpperCase(),
        timestamp: new Date().toISOString(),
      };

      if (data) {
        errorLog.responseData = data;
      }

      if (error.config?.data) {
        try {
          errorLog.requestData =
            typeof error.config.data === 'string'
              ? JSON.parse(error.config.data)
              : error.config.data;
        } catch {
          errorLog.requestData = error.config.data;
        }
      }

      logger.error('API Error', error, errorLog);
      
      const errorMessage = typeof data.message === 'string' ? data.message : undefined;
      
      if (error.response.status === 500) {
        apiError.message = errorMessage || 'Erro interno do servidor. O backend está respondendo, mas ocorreu um erro ao processar a requisição. Verifique os logs do backend e a configuração do banco de dados.';
      } else if (error.response.status === 404) {
        apiError.message = errorMessage || 'Recurso não encontrado.';
      } else if (error.response.status === 400) {
        apiError.message = errorMessage || 'Dados inválidos. Verifique os campos do formulário.';
      } else {
        apiError.message = errorMessage || `Erro ${error.response.status}: ${error.response.statusText}`;
      }

      if (data.errors && typeof data.errors === 'object') {
        apiError.errors = data.errors as Record<string, string[]>;
      }
      
      if (data.details && typeof data.details === 'object') {
        apiError.details = data.details as Record<string, string>;
        
        if (error.response.status === 400 && Object.keys(apiError.details).length > 0) {
          const fieldCount = Object.keys(apiError.details).length;
          apiError.message = `Erro de validação em ${fieldCount} campo(s). Verifique os detalhes abaixo.`;
        }
      }
      
      if (!apiError.details && data.errors && typeof data.errors === 'object') {
        const errorsObj = data.errors as Record<string, unknown>;
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
