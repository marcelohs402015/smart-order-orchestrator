/**
 * Componente OrderCard para exibir informações de um pedido.
 */

import { OrderResponse } from '../types';
import { formatCurrency, formatDate, getOrderStatusInfo, getRiskLevelInfo } from '../utils';
import { Card } from './ui/Card';

interface OrderCardProps {
  order: OrderResponse;
  onClick?: () => void;
}

export const OrderCard = ({ order, onClick }: OrderCardProps) => {
  const statusInfo = getOrderStatusInfo(order.status);
  const riskInfo = getRiskLevelInfo(order.riskLevel);

  return (
    <Card
      className={onClick ? 'cursor-pointer hover:shadow-lg transition-shadow' : ''}
      onClick={onClick}
    >
      <div className="space-y-4">
        {/* Header */}
        <div className="flex items-start justify-between">
          <div>
            <h3 className="text-lg font-semibold text-gray-900">
              {order.orderNumber}
            </h3>
            <p className="text-sm text-gray-500">
              {order.customerName}
            </p>
          </div>
          <span
            className={`px-3 py-1 rounded-full text-sm font-medium ${statusInfo.color}`}
          >
            {statusInfo.icon} {statusInfo.label}
          </span>
        </div>

        {/* Info */}
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-gray-500">Total</p>
            <p className="font-semibold text-gray-900">
              {formatCurrency(order.totalAmount)}
            </p>
          </div>
          <div>
            <p className="text-gray-500">Itens</p>
            <p className="font-semibold text-gray-900">
              {order.items.length} {order.items.length === 1 ? 'item' : 'itens'}
            </p>
          </div>
        </div>

        {/* Risk Level */}
        {order.riskLevel && (
          <div>
            <p className="text-xs text-gray-500 mb-1">Nível de Risco</p>
            <span
              className={`inline-flex items-center px-2 py-1 rounded text-xs font-medium ${riskInfo.color}`}
            >
              {riskInfo.icon} {riskInfo.label}
            </span>
          </div>
        )}

        {/* Footer */}
        <div className="pt-4 border-t border-gray-200">
          <p className="text-xs text-gray-500">
            Criado em {formatDate(order.createdAt)}
          </p>
        </div>
      </div>
    </Card>
  );
};

