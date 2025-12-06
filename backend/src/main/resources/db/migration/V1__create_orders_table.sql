-- ============================================================================
-- Migration V1: Create Orders and Order Items Tables
-- ============================================================================
-- Flyway Migration: Criação inicial das tabelas de pedidos
-- ============================================================================

-- Tabela de pedidos
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    customer_id UUID NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    payment_id VARCHAR(100),
    risk_level VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_order_number ON orders(order_number);
CREATE INDEX IF NOT EXISTS idx_order_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_customer ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_order_created_at ON orders(created_at);

-- Tabela de itens de pedido
CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_order_item_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_item_product_id ON order_items(product_id);

-- Comentários nas tabelas (documentação)
COMMENT ON TABLE orders IS 'Tabela de pedidos do sistema Smart Order Orchestrator';
COMMENT ON TABLE order_items IS 'Itens de pedido - relacionamento One-to-Many com orders';

COMMENT ON COLUMN orders.id IS 'ID único do pedido (UUID)';
COMMENT ON COLUMN orders.order_number IS 'Número único do pedido (ex: ORD-1234567890)';
COMMENT ON COLUMN orders.status IS 'Status do pedido (PENDING, PAID, PAYMENT_FAILED, CANCELED)';
COMMENT ON COLUMN orders.version IS 'Versão para controle de concorrência otimista (Optimistic Locking)';

