import { useCallback } from 'react';
import { ApiError } from '../types';
import { useNotificationStore } from '../store/notificationStore';
import { logger } from '../utils/logger';

interface ErrorHandlerOptions {
  showNotification?: boolean;
  notificationTitle?: string;
  logError?: boolean;
}

export const useErrorHandler = () => {
  const addNotification = useNotificationStore((state) => state.addNotification);

  const handleError = useCallback(
    (
      error: unknown,
      options: ErrorHandlerOptions = {
        showNotification: true,
        logError: true,
      }
    ): ApiError => {
      const apiError = error as ApiError;

      if (options.logError !== false) {
        logger.error('ErrorHandler', apiError, {
          message: apiError.message,
          status: apiError.status,
          path: apiError.path,
          details: apiError.details,
          timestamp: apiError.timestamp,
        });
      }

      if (options.showNotification !== false) {
        const getNotificationTitle = (): string => {
          if (apiError.status === 404) return 'Recurso não encontrado';
          if (apiError.status === 500) return 'Erro interno do servidor';
          if (apiError.status === 400) return 'Erro de validação';
          return options.notificationTitle || 'Erro ao processar requisição';
        };

        const getNotificationMessage = (): string => {
          if (apiError.details && Object.keys(apiError.details).length > 0) {
            const fieldCount = Object.keys(apiError.details).length;
            return `Erro de validação em ${fieldCount} campo(s)`;
          }
          return apiError.message;
        };

        addNotification({
          type: 'error',
          title: getNotificationTitle(),
          message: getNotificationMessage(),
          duration: 7000,
        });
      }

      return apiError;
    },
    [addNotification]
  );

  const handleSuccess = useCallback(
    (message: string, title?: string) => {
      addNotification({
        type: 'success',
        title: title || 'Sucesso',
        message,
        duration: 3000,
      });
    },
    [addNotification]
  );

  const handleWarning = useCallback(
    (message: string, title?: string) => {
      addNotification({
        type: 'warning',
        title: title || 'Atenção',
        message,
        duration: 5000,
      });
    },
    [addNotification]
  );

  const handleInfo = useCallback(
    (message: string, title?: string) => {
      addNotification({
        type: 'info',
        title: title || 'Informação',
        message,
        duration: 4000,
      });
    },
    [addNotification]
  );

  return {
    handleError,
    handleSuccess,
    handleWarning,
    handleInfo,
  };
};

