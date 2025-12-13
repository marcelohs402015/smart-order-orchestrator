-- ============================================================================
-- Script de Limpeza do Banco de Dados
-- ============================================================================
-- ATENÇÃO: Este script remove TODAS as tabelas e dados do banco!
-- Execute este script ANTES de rodar a aplicação para recriar tudo do zero.
-- ============================================================================
-- 
-- Como usar:
-- 1. Conecte-se ao banco PostgreSQL
-- 2. Execute este script: psql -U postgres -d smartorder -f clean_database.sql
--    ou copie e cole no pgAdmin / DBeaver
-- 3. Depois, inicie a aplicação - o Flyway vai recriar todas as tabelas
-- ============================================================================

-- Remover tabelas na ordem correta (respeitando foreign keys)
DROP TABLE IF EXISTS saga_steps CASCADE;
DROP TABLE IF EXISTS saga_executions CASCADE;
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;

-- Remover índices se existirem (alguns podem ter sido criados separadamente)
DROP INDEX IF EXISTS idx_order_number CASCADE;
DROP INDEX IF EXISTS idx_order_status CASCADE;
DROP INDEX IF EXISTS idx_order_customer CASCADE;
DROP INDEX IF EXISTS idx_order_created_at CASCADE;
DROP INDEX IF EXISTS idx_order_item_order_id CASCADE;
DROP INDEX IF EXISTS idx_order_item_product_id CASCADE;
DROP INDEX IF EXISTS idx_saga_order_id CASCADE;
DROP INDEX IF EXISTS idx_saga_status CASCADE;
DROP INDEX IF EXISTS idx_saga_started_at CASCADE;
DROP INDEX IF EXISTS idx_saga_idempotency_key CASCADE;
DROP INDEX IF EXISTS idx_step_saga_id CASCADE;
DROP INDEX IF EXISTS idx_step_name CASCADE;

-- Limpar histórico do Flyway (se necessário)
-- Descomente a linha abaixo se quiser resetar o histórico do Flyway também:
-- DELETE FROM flyway_schema_history;

-- ============================================================================
-- FIM DA LIMPEZA
-- ============================================================================
-- Após executar este script, inicie a aplicação.
-- O Flyway vai executar V1__create_orders_table.sql e recriar tudo.
-- ============================================================================

