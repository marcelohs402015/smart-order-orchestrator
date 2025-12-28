import { ReactNode } from 'react';
import { cn } from '../../utils';

type AlertVariant = 'success' | 'error' | 'warning' | 'info';

interface AlertProps {
  variant: AlertVariant;
  title?: string;
  children: ReactNode;
  className?: string;
  onClose?: () => void;
}

const variantStyles = {
  success: {
    container: 'bg-green-50 border-green-200 text-green-800',
    icon: '✅',
  },
  error: {
    container: 'bg-red-50 border-red-200 text-red-800',
    icon: '❌',
  },
  warning: {
    container: 'bg-yellow-50 border-yellow-200 text-yellow-800',
    icon: '⚠️',
  },
  info: {
    container: 'bg-blue-50 border-blue-200 text-blue-800',
    icon: 'ℹ️',
  },
};

export const Alert = ({
  variant,
  title,
  children,
  className,
  onClose,
}: AlertProps) => {
  const styles = variantStyles[variant];

  return (
    <div
      className={cn(
        'border rounded-lg p-4',
        styles.container,
        className
      )}
      role="alert"
    >
      <div className="flex items-start">
        <span className="mr-2 text-xl">{styles.icon}</span>
        <div className="flex-1">
          {title && (
            <h4 className="font-semibold mb-1">{title}</h4>
          )}
          <div>{children}</div>
        </div>
        {onClose && (
          <button
            onClick={onClose}
            className="ml-4 text-current opacity-70 hover:opacity-100"
            aria-label="Fechar"
          >
            ✕
          </button>
        )}
      </div>
    </div>
  );
};
