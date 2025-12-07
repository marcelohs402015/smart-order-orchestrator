-- ============================================================================
-- Adiciona suporte a Idempotência na Saga
-- ============================================================================
-- Adiciona campo idempotency_key para prevenir execuções duplicadas.
-- Padrão: Idempotency Key - garante que requisições duplicadas não criem
-- pedidos duplicados.
-- ============================================================================

-- Adicionar coluna idempotency_key
ALTER TABLE saga_executions 
ADD COLUMN idempotency_key VARCHAR(255);

-- Criar índice único para garantir unicidade
CREATE UNIQUE INDEX idx_saga_idempotency_key ON saga_executions(idempotency_key);

-- Comentário para documentação
COMMENT ON COLUMN saga_executions.idempotency_key IS 'Chave de idempotência para prevenir execuções duplicadas. Deve ser único por requisição.';

