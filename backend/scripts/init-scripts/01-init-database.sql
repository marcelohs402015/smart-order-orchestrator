-- ============================================================================
-- Script de Inicialização do Banco de Dados
-- ============================================================================
-- Este script é executado automaticamente na primeira inicialização do PostgreSQL
-- através do Docker Compose (pasta /docker-entrypoint-initdb.d)
-- ============================================================================

-- Criar extensões úteis (se necessário)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Criar schema customizado (opcional - usar 'public' por padrão)
-- CREATE SCHEMA IF NOT EXISTS smartorder;

-- Comentários e documentação
COMMENT ON DATABASE smartorder IS 'Database for Smart Order Orchestrator - Hexagonal Architecture PoC';

-- ============================================================================
-- Notas:
-- ============================================================================
-- 1. Tabelas serão criadas automaticamente pelo Hibernate (ddl-auto: update)
--    ou através de migrations (Flyway/Liquibase) em fases futuras
-- 
-- 2. Este script é útil para:
--    - Criar extensões PostgreSQL
--    - Criar schemas customizados
--    - Inserir dados iniciais (se necessário)
--    - Configurar permissões
-- 
-- 3. Para adicionar mais scripts, crie arquivos numerados:
--    02-custom-script.sql
--    03-another-script.sql
-- ============================================================================

