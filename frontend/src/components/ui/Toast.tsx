import { useEffect } from 'react';
import { useNotificationStore, Notification } from '../../store/notificationStore';
import { cn } from '../../utils';

const notificationStyles = {
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

interface ToastItemProps {
  notification: Notification;
  onClose: () => void;
}

const ToastItem = ({ notification, onClose }: ToastItemProps) => {
  const styles = notificationStyles[notification.type];

  useEffect(() => {
    if (notification.duration && notification.duration > 0) {
      const timer = setTimeout(() => {
        onClose();
      }, notification.duration);

      return () => clearTimeout(timer);
    }
  }, [notification.duration, onClose]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' || e.key === ' ') {
      onClose();
    }
  };

  return (
    <div
      className={cn(
        'border rounded-lg p-4 shadow-lg min-w-[300px] max-w-[500px] transition-all duration-300',
        styles.container
      )}
      role="alert"
      aria-live="polite"
    >
      <div className="flex items-start gap-3">
        <span className="text-xl flex-shrink-0">{styles.icon}</span>
        <div className="flex-1 min-w-0">
          {notification.title && (
            <h4 className="font-semibold mb-1 text-sm">{notification.title}</h4>
          )}
          <p className="text-sm">{notification.message}</p>
        </div>
        <button
          onClick={onClose}
          onKeyDown={handleKeyDown}
          className="ml-2 text-current opacity-70 hover:opacity-100 flex-shrink-0"
          aria-label="Fechar notificação"
          tabIndex={0}
        >
          ✕
        </button>
      </div>
    </div>
  );
};

export const ToastContainer = () => {
  const notifications = useNotificationStore((state) => state.notifications);
  const removeNotification = useNotificationStore((state) => state.removeNotification);

  if (notifications.length === 0) {
    return null;
  }

  return (
    <div
      className="fixed top-4 right-4 z-50 flex flex-col gap-3 pointer-events-none"
      aria-live="polite"
      aria-label="Notificações"
    >
      {notifications.map((notification) => (
        <div key={notification.id} className="pointer-events-auto">
          <ToastItem
            notification={notification}
            onClose={() => removeNotification(notification.id)}
          />
        </div>
      ))}
    </div>
  );
};

