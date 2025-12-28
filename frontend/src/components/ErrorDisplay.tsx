import { ApiError } from '../types';
import { Alert } from './ui/Alert';
import { Button } from './ui/Button';

interface ErrorDisplayProps {
  error: ApiError | null;
  onRetry?: () => void;
  onClose?: () => void;
  className?: string;
  showDetails?: boolean;
  retryLabel?: string;
}

export const ErrorDisplay = ({
  error,
  onRetry,
  onClose,
  className,
  showDetails = true,
  retryLabel = 'Tentar Novamente',
}: ErrorDisplayProps) => {
  if (!error) {
    return null;
  }

  const isServerError = error.status === 500;
  const isNotFound = error.status === 404;
  const isValidationError = error.status === 400 && error.details;

  const getErrorTitle = (): string => {
    if (isNotFound) return 'Recurso não encontrado';
    if (isServerError) return 'Erro interno do servidor';
    if (isValidationError) return 'Erro de validação';
    return 'Erro ao processar requisição';
  };

  const handleRetry = () => {
    if (onRetry) {
      onRetry();
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' || e.key === ' ') {
      handleRetry();
    }
  };

  return (
    <Alert variant="error" onClose={onClose} className={className}>
      <div>
        <p className="font-semibold mb-1">{getErrorTitle()}</p>
        <p className="mb-2">{error.message}</p>

        {isServerError && showDetails && (
          <div className="mt-3 p-3 bg-red-50 border border-red-200 rounded text-sm">
            <p className="font-medium mb-2">
              O backend está respondendo, mas ocorreu um erro interno.
            </p>
            <p className="mb-2 text-gray-700">Possíveis causas:</p>
            <ul className="list-disc list-inside space-y-1 text-gray-700">
              <li>Banco de dados não está conectado ou configurado corretamente</li>
              <li>Erro na consulta ao banco de dados (verifique se as tabelas existem)</li>
              <li>Erro na conversão de dados (verifique os logs do backend)</li>
              <li>Problema na configuração do Spring Boot</li>
            </ul>
            <p className="mt-2 text-gray-600">
              <strong>Status:</strong> {error.status} | <strong>Path:</strong>{' '}
              {error.path || 'N/A'}
            </p>
            <p className="mt-2 text-xs text-gray-500">
              💡 Dica: Verifique os logs do backend em http://localhost:8080 para mais detalhes
              sobre o erro.
            </p>
            {onRetry && (
              <div className="mt-3">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={handleRetry}
                  onKeyDown={handleKeyDown}
                  tabIndex={0}
                >
                  {retryLabel}
                </Button>
              </div>
            )}
          </div>
        )}

        {isValidationError && error.details && showDetails && (
          <div className="mt-2 text-sm">
            <p className="font-medium mb-1">Detalhes do erro:</p>
            <ul className="list-disc list-inside space-y-1">
              {Object.entries(error.details).map(([field, message]) => (
                <li key={field}>
                  <strong>{field}:</strong> {String(message)}
                </li>
              ))}
            </ul>
          </div>
        )}

        {!isServerError && onRetry && (
          <div className="mt-3">
            <Button
              size="sm"
              variant="outline"
              onClick={handleRetry}
              onKeyDown={handleKeyDown}
              tabIndex={0}
            >
              {retryLabel}
            </Button>
          </div>
        )}

        {error.path && showDetails && (
          <p className="mt-2 text-xs text-gray-500">
            <strong>Endpoint:</strong> {error.path}
          </p>
        )}
      </div>
    </Alert>
  );
};

