import { OrderStatus, RiskLevel } from '../types';

export const formatCurrency = (value: number, currency: string = 'BRL'): string => {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: currency,
  }).format(value);
};

export const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date);
};

export const getOrderStatusInfo = (status: OrderStatus) => {
  const statusMap = {
    [OrderStatus.PENDING]: {
      label: 'Pendente',
      color: 'bg-yellow-100 text-yellow-800',
      icon: '⏳',
    },
    [OrderStatus.PAID]: {
      label: 'Pago',
      color: 'bg-green-100 text-green-800',
      icon: '✅',
    },
    [OrderStatus.PAYMENT_FAILED]: {
      label: 'Pagamento Falhou',
      color: 'bg-red-100 text-red-800',
      icon: '❌',
    },
    [OrderStatus.CANCELED]: {
      label: 'Cancelado',
      color: 'bg-gray-100 text-gray-800',
      icon: '🚫',
    },
  };

  return statusMap[status] || {
    label: status,
    color: 'bg-gray-100 text-gray-800',
    icon: '❓',
  };
};

export const getRiskLevelInfo = (riskLevel?: RiskLevel) => {
  if (!riskLevel) {
    return {
      label: 'Não analisado',
      color: 'bg-gray-100 text-gray-800',
      icon: '⏸️',
    };
  }

  const riskMap = {
    [RiskLevel.LOW]: {
      label: 'Baixo Risco',
      color: 'bg-green-100 text-green-800',
      icon: '🟢',
    },
    [RiskLevel.HIGH]: {
      label: 'Alto Risco',
      color: 'bg-red-100 text-red-800',
      icon: '🔴',
    },
    [RiskLevel.PENDING]: {
      label: 'Análise Pendente',
      color: 'bg-yellow-100 text-yellow-800',
      icon: '⏳',
    },
  };

  return riskMap[riskLevel] || {
    label: riskLevel,
    color: 'bg-gray-100 text-gray-800',
    icon: '❓',
  };
};

export const generateId = (): string => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
};

export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

export const isValidUUID = (value: string): boolean => {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
  return uuidRegex.test(value);
};

export const cn = (...classes: (string | undefined | null | false)[]): string => {
  return classes.filter(Boolean).join(' ');
};
