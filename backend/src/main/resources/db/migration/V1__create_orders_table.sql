-- ============================================================================
-- Migration V1: Create All Tables - Complete Database Schema
-- ============================================================================
-- Flyway Migration: Criação completa do schema do banco de dados
-- Recriado para garantir alinhamento 100% com entidades JPA
-- ============================================================================

-- ============================================================================
-- TABELA: orders (Pedidos)
-- ============================================================================
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

-- Índices para tabela orders
CREATE INDEX IF NOT EXISTS idx_order_number ON orders(order_number);
CREATE INDEX IF NOT EXISTS idx_order_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_customer ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_order_created_at ON orders(created_at);

-- Comentários para tabela orders
COMMENT ON TABLE orders IS 'Tabela de pedidos do sistema Smart Order Orchestrator';
COMMENT ON COLUMN orders.id IS 'ID único do pedido (UUID)';
COMMENT ON COLUMN orders.order_number IS 'Número único do pedido (ex: ORD-1234567890)';
COMMENT ON COLUMN orders.status IS 'Status do pedido (PENDING, PAID, PAYMENT_FAILED, CANCELED)';
COMMENT ON COLUMN orders.version IS 'Versão para controle de concorrência otimista (Optimistic Locking)';

-- ============================================================================
-- TABELA: order_items (Itens de Pedido)
-- ============================================================================
CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Índices para tabela order_items
CREATE INDEX IF NOT EXISTS idx_order_item_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_item_product_id ON order_items(product_id);

-- Comentários para tabela order_items
COMMENT ON TABLE order_items IS 'Itens de pedido - relacionamento One-to-Many com orders';
COMMENT ON COLUMN order_items.id IS 'ID único do item (UUID gerado automaticamente)';
COMMENT ON COLUMN order_items.order_id IS 'Referência ao pedido pai (FK para orders.id)';

-- ============================================================================
-- TABELA: saga_executions (Execuções de Saga)
-- ============================================================================
CREATE TABLE IF NOT EXISTS saga_executions (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(255) UNIQUE,
    order_id UUID,
    status VARCHAR(50) NOT NULL,
    current_step VARCHAR(50),
    error_message TEXT,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    timeout_at TIMESTAMP,
    duration_ms BIGINT,
    
    CONSTRAINT fk_saga_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Índices para tabela saga_executions
CREATE INDEX IF NOT EXISTS idx_saga_order_id ON saga_executions(order_id);
CREATE INDEX IF NOT EXISTS idx_saga_status ON saga_executions(status);
CREATE INDEX IF NOT EXISTS idx_saga_started_at ON saga_executions(started_at);
CREATE UNIQUE INDEX IF NOT EXISTS idx_saga_idempotency_key ON saga_executions(idempotency_key);

-- Comentários para tabela saga_executions
COMMENT ON TABLE saga_executions IS 'Rastreamento de execuções de saga para observabilidade e auditoria';
COMMENT ON COLUMN saga_executions.id IS 'ID único da execução de saga (UUID)';
COMMENT ON COLUMN saga_executions.idempotency_key IS 'Chave de idempotência para prevenir execuções duplicadas. Deve ser único por requisição.';
COMMENT ON COLUMN saga_executions.order_id IS 'ID do pedido associado a esta saga (FK para orders.id)';
COMMENT ON COLUMN saga_executions.status IS 'Status atual da saga (STARTED, ORDER_CREATED, PAYMENT_PROCESSED, RISK_ANALYZED, COMPLETED, FAILED, COMPENSATED)';
COMMENT ON COLUMN saga_executions.duration_ms IS 'Duração total da saga em milissegundos';
COMMENT ON COLUMN saga_executions.timeout_at IS 'Data/hora de expiração da saga. Sagas expiradas devem ser compensadas';

-- ============================================================================
-- TABELA: saga_steps (Passos da Saga)
-- ============================================================================
CREATE TABLE IF NOT EXISTS saga_steps (
    id UUID PRIMARY KEY,
    saga_execution_id UUID NOT NULL,
    step_name VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    duration_ms BIGINT,
    error_message TEXT,
    metadata TEXT,
    
    CONSTRAINT fk_step_saga FOREIGN KEY (saga_execution_id) REFERENCES saga_executions(id) ON DELETE CASCADE
);

-- Índices para tabela saga_steps
CREATE INDEX IF NOT EXISTS idx_step_saga_id ON saga_steps(saga_execution_id);
CREATE INDEX IF NOT EXISTS idx_step_name ON saga_steps(step_name);

-- Comentários para tabela saga_steps
COMMENT ON TABLE saga_steps IS 'Histórico detalhado de cada passo executado na saga';
COMMENT ON COLUMN saga_steps.id IS 'ID único do passo (UUID)';
COMMENT ON COLUMN saga_steps.saga_execution_id IS 'Referência à execução de saga pai (FK para saga_executions.id)';
COMMENT ON COLUMN saga_steps.step_name IS 'Nome do passo (ex: ORDER_CREATED, PAYMENT_PROCESSED)';
COMMENT ON COLUMN saga_steps.status IS 'Status do passo (STARTED, SUCCESS, FAILED)';
COMMENT ON COLUMN saga_steps.duration_ms IS 'Duração do passo em milissegundos';

-- ============================================================================
-- FIM DA MIGRAÇÃO
-- ============================================================================
