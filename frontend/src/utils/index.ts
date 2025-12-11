/**
 * Funções utilitárias compartilhadas.
 * 
 * <h3>Exemplos de Utilitários:</h3>
 * <ul>
 *   <li>Formatação de datas, moedas</li>
 *   <li>Validações auxiliares</li>
 *   <li>Helpers para arrays, objetos</li>
 *   <li>Constantes compartilhadas</li>
 * </ul>
 */

import { OrderStatus, RiskLevel } from '../types';

/**
 * Formata valor monetário para exibição.
 * 
 * @param value Valor numérico
 * @param currency Moeda (padrão: BRL)
 * @returns String formatada (ex: "R$ 100,50")
 */
export const formatCurrency = (value: number, currency: string = 'BRL'): string => {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: currency,
  }).format(value);
};

/**
 * Formata data para exibição.
 * 
 * @param dateString String de data ISO
 * @returns String formatada (ex: "01/01/2024 10:30")
 */
export const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date);
};

/**
 * Retorna cor e label para status do pedido.
 * 
 * @param status Status do pedido
 * @returns Objeto com cor e label
 */
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

/**
 * Retorna cor e label para nível de risco.
 * 
 * @param riskLevel Nível de risco
 * @returns Objeto com cor e label
 */
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

/**
 * Gera ID único (UUID v4 simplificado).
 * 
 * @returns String UUID
 */
export const generateId = (): string => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
};

/**
 * Valida formato de email.
 * 
 * @param email Email a ser validado
 * @returns true se válido, false caso contrário
 */
export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

/**
 * Valida formato de UUID (v4).
 * 
 * @param value String a ser validada
 * @returns true se válido, false caso contrário
 */
export const isValidUUID = (value: string): boolean => {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
  return uuidRegex.test(value);
};

/**
 * Classe CSS utilitária para combinar classes.
 * 
 * @param classes Classes CSS a serem combinadas
 * @returns String com classes combinadas
 */
export const cn = (...classes: (string | undefined | null | false)[]): string => {
  return classes.filter(Boolean).join(' ');
};
