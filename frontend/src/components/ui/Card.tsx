import { ReactNode } from 'react';
import { cn } from '../../utils';

interface CardProps {
  children: ReactNode;
  className?: string;
  header?: ReactNode;
  footer?: ReactNode;
  padding?: 'none' | 'sm' | 'md' | 'lg';
}

const paddingStyles = {
  none: '',
  sm: 'p-4',
  md: 'p-6',
  lg: 'p-8',
};

export const Card = ({
  children,
  className,
  header,
  footer,
  padding = 'md',
}: CardProps) => {
  return (
    <div
      className={cn(
        'bg-white rounded-lg shadow-md',
        'border border-gray-200',
        className
      )}
    >
      {header && (
        <div className="border-b border-gray-200 px-6 py-4">
          {header}
        </div>
      )}
      <div className={cn(paddingStyles[padding])}>{children}</div>
      {footer && (
        <div className="border-t border-gray-200 px-6 py-4">
          {footer}
        </div>
      )}
    </div>
  );
};
