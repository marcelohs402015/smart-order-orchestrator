-- ============================================================================
-- Saga Pattern - Tabelas de Rastreamento e Observabilidade
-- ============================================================================
-- Tabelas para persistir estado e histórico de execuções de saga,
-- permitindo observabilidade completa e rastreamento de transações distribuídas.
-- ============================================================================

-- Tabela principal de execuções de saga
CREATE TABLE saga_executions (
    id UUID PRIMARY KEY,
    order_id UUID,
    status VARCHAR(50) NOT NULL,
    current_step VARCHAR(50),
    error_message TEXT,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    duration_ms BIGINT,
    
    CONSTRAINT fk_saga_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Índices para consultas rápidas
CREATE INDEX idx_saga_order_id ON saga_executions(order_id);
CREATE INDEX idx_saga_status ON saga_executions(status);
CREATE INDEX idx_saga_started_at ON saga_executions(started_at);

-- Tabela de passos da saga (histórico detalhado)
CREATE TABLE saga_steps (
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

-- Índices para consultas rápidas
CREATE INDEX idx_step_saga_id ON saga_steps(saga_execution_id);
CREATE INDEX idx_step_name ON saga_steps(step_name);

-- Comentários para documentação
COMMENT ON TABLE saga_executions IS 'Rastreamento de execuções de saga para observabilidade e auditoria';
COMMENT ON TABLE saga_steps IS 'Histórico detalhado de cada passo executado na saga';
COMMENT ON COLUMN saga_executions.duration_ms IS 'Duração total da saga em milissegundos';
COMMENT ON COLUMN saga_steps.duration_ms IS 'Duração do passo em milissegundos';

