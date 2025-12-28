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

export interface CreateOrderRequest {
  customerId: string;
  customerName: string;
  customerEmail: string;
  items: OrderItemRequest[];
  paymentMethod: string;
  currency?: string;
  idempotencyKey?: string;
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

export interface ApiError {
  message: string;
  status?: number;
  error?: string;
  errors?: Record<string, string[]>;
  details?: Record<string, string>;
  timestamp?: string;
  path?: string;
  isBusinessError?: boolean;
}

export type LoadingState = 'idle' | 'loading' | 'success' | 'error';

export interface ApiResponse<T> {
  data?: T;
  error?: ApiError;
  loading: boolean;
}
