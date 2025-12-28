import { Component, ReactNode, ErrorInfo } from 'react';
import { Button } from './ui/Button';
import { Card } from './ui/Card';

interface ErrorBoundaryProps {
  children: ReactNode;
  fallback?: ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
}

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  static getDerivedStateFromError(error: Error): Partial<ErrorBoundaryState> {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('ErrorBoundary capturou um erro:', error, errorInfo);
    this.setState({
      error,
      errorInfo,
    });
  }

  handleReset = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
    });
  };

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }

      return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
          <Card className="max-w-2xl w-full">
            <div className="text-center">
              <div className="text-6xl mb-4">⚠️</div>
              <h1 className="text-2xl font-bold text-gray-900 mb-2">
                Ops! Algo deu errado
              </h1>
              <p className="text-gray-600 mb-6">
                Ocorreu um erro inesperado. Por favor, tente novamente.
              </p>

              {import.meta.env.DEV && this.state.error && (
                <div className="mt-6 p-4 bg-red-50 border border-red-200 rounded-lg text-left">
                  <p className="font-semibold text-red-900 mb-2">Detalhes do erro (apenas em desenvolvimento):</p>
                  <p className="text-sm text-red-800 font-mono mb-2">
                    {this.state.error.toString()}
                  </p>
                  {this.state.errorInfo && (
                    <details className="text-xs text-red-700">
                      <summary className="cursor-pointer font-medium mb-2">
                        Stack trace
                      </summary>
                      <pre className="whitespace-pre-wrap overflow-auto max-h-64">
                        {this.state.errorInfo.componentStack}
                      </pre>
                    </details>
                  )}
                </div>
              )}

              <div className="mt-6 flex gap-4 justify-center">
                <Button onClick={this.handleReset}>
                  Tentar Novamente
                </Button>
                <Button
                  variant="secondary"
                  onClick={() => window.location.href = '/'}
                >
                  Ir para Home
                </Button>
              </div>
            </div>
          </Card>
        </div>
      );
    }

    return this.props.children;
  }
}
