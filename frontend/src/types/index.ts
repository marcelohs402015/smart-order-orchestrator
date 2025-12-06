/**
 * Tipos TypeScript compartilhados da aplicação.
 * 
 * <h3>Por que Centralizar Types?</h3>
 * <ul>
 *   <li><strong>Consistência:</strong> Tipos compartilhados entre componentes</li>
 *   <li><strong>Reutilização:</strong> Evita duplicação de definições</li>
 *   <li><strong>Manutenibilidade:</strong> Um lugar para atualizar tipos</li>
 * </ul>
 * 
 * <h3>Estrutura:</h3>
 * <p>Tipos baseados nos DTOs do backend para garantir compatibilidade.</p>
 */

// ============================================================================
// Enums
// ============================================================================

export enum OrderStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  PAYMENT_FAILED = 'PAYMENT_FAILED',
  CANCELED = 'CANCELED',
}

export enum RiskLevel {
  LOW = 'LOW',
  HIGH = 'HIGH',
  PENDING = 'PENDING',
}

// ============================================================================
// Order Item Types
// ============================================================================

export interface OrderItemRequest {
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
}

export interface OrderItemResponse {
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

// ============================================================================
// Order Types
// ============================================================================

export interface CreateOrderRequest {
  customerId: string;
  customerName: string;
  customerEmail: string;
  items: OrderItemRequest[];
  paymentMethod: string;
  currency?: string;
}

export interface OrderResponse {
  id: string;
  orderNumber: string;
  status: OrderStatus;
  customerId: string;
  customerName: string;
  customerEmail: string;
  items: OrderItemResponse[];
  totalAmount: number;
  paymentId?: string;
  riskLevel?: RiskLevel;
  createdAt: string;
  updatedAt: string;
}

export interface CreateOrderResponse {
  success: boolean;
  order?: OrderResponse;
  sagaExecutionId?: string;
  errorMessage?: string;
}

// ============================================================================
// API Error Types
// ============================================================================

export interface ApiError {
  message: string;
  errors?: Record<string, string[]>;
  timestamp?: string;
  path?: string;
}

// ============================================================================
// Utility Types
// ============================================================================

export type LoadingState = 'idle' | 'loading' | 'success' | 'error';

export interface ApiResponse<T> {
  data?: T;
  error?: ApiError;
  loading: boolean;
}
