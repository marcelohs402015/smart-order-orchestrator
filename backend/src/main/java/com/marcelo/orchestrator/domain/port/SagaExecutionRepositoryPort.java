package com.marcelo.orchestrator.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta (Port) para persistência de execuções de saga.
 * 
 * <p>Esta interface define o <strong>contrato de saída</strong> (Outbound Port) na Arquitetura Hexagonal.
 * O domínio define o que precisa (contrato), e a camada Infrastructure implementa como fazer (adaptador).</p>
 * 
 * <h3>Padrão Ports and Adapters (Hexagonal Architecture):</h3>
 * <ul>
 *   <li><strong>Port:</strong> Esta interface - define o contrato que o domínio precisa</li>
 *   <li><strong>Adapter:</strong> Implementação na camada Infrastructure (ex: JpaRepository)</li>
 *   <li><strong>Inversão de Dependência:</strong> Application não depende de JPA, JPA depende do domínio</li>
 * </ul>
 * 
 * <h3>Benefícios desta Abordagem:</h3>
 * <ul>
 *   <li><strong>Testabilidade:</strong> Fácil criar mocks para testes unitários</li>
 *   <li><strong>Flexibilidade:</strong> Trocar JPA por MongoDB, Cassandra, etc. sem alterar Application</li>
 *   <li><strong>Isolamento:</strong> Application não conhece detalhes de implementação (JPA, SQL, etc.)</li>
 *   <li><strong>Clean Architecture:</strong> Dependências apontam para dentro (domínio no centro)</li>
 * </ul>
 * 
 * <h3>Responsabilidades:</h3>
 * <p>Esta porta é responsável apenas por <strong>persistência de execuções de saga</strong>.
 * A lógica de negócio da saga fica no OrderSagaOrchestrator.</p>
 * 
 * @author Marcelo
 */
public interface SagaExecutionRepositoryPort {
    
    /**
     * Salva uma execução de saga (cria nova ou atualiza existente).
     * 
     * @param sagaExecution Execução de saga a ser salva
     * @return Execução salva (pode conter dados adicionais gerados pela persistência)
     */
    SagaExecution save(SagaExecution sagaExecution);
    
    /**
     * Busca uma execução de saga pelo ID.
     * 
     * @param id Identificador único da execução
     * @return Optional contendo a execução se encontrada, vazio caso contrário
     */
    Optional<SagaExecution> findById(UUID id);
    
    /**
     * Busca execuções por ID do pedido.
     * 
     * @param orderId ID do pedido
     * @return Lista de execuções associadas ao pedido (pode ser vazia)
     */
    List<SagaExecution> findByOrderId(UUID orderId);
    
    /**
     * Busca execução mais recente por ID do pedido.
     * 
     * @param orderId ID do pedido
     * @return Execução mais recente ou Optional.empty() se não encontrada
     */
    Optional<SagaExecution> findFirstByOrderIdOrderByStartedAtDesc(UUID orderId);
    
    /**
     * Busca execuções por status.
     * 
     * @param status Status desejado
     * @return Lista de execuções com o status especificado (pode ser vazia)
     */
    List<SagaExecution> findByStatus(SagaExecution.SagaStatus status);
    
    /**
     * Busca execução por chave de idempotência.
     * 
     * <p>Padrão: Idempotência - usado para verificar se uma saga com
     * a mesma chave já foi executada ou está em progresso.</p>
     * 
     * @param idempotencyKey Chave de idempotência
     * @return Execução encontrada ou Optional.empty() se não existir
     */
    Optional<SagaExecution> findByIdempotencyKey(String idempotencyKey);
    
    /**
     * Value Object que representa uma execução de saga no domínio.
     * 
     * <p>Este é um objeto de domínio puro, sem dependências de infraestrutura.
     * A camada Infrastructure criará uma entidade JPA separada que será
     * mapeada para este objeto de domínio.</p>
     */
    record SagaExecution(
        UUID id,
        String idempotencyKey,
        UUID orderId,
        SagaStatus status,
        String currentStep,
        String errorMessage,
        java.time.LocalDateTime startedAt,
        java.time.LocalDateTime completedAt,
        Long durationMs,
        List<SagaStep> steps
    ) {
        /**
         * Enum de status da saga.
         */
        public enum SagaStatus {
            STARTED,
            ORDER_CREATED,
            PAYMENT_PROCESSED,
            RISK_ANALYZED,
            COMPLETED,
            FAILED,
            COMPENSATED
        }
    }
    
    /**
     * Value Object que representa um passo da saga no domínio.
     */
    record SagaStep(
        UUID id,
        UUID sagaExecutionId,
        String stepName,
        StepStatus status,
        String errorMessage,
        java.time.LocalDateTime startedAt,
        java.time.LocalDateTime completedAt,
        Long durationMs
    ) {
        /**
         * Enum de status do passo.
         */
        public enum StepStatus {
            STARTED,
            SUCCESS,
            FAILED
        }
    }
}

