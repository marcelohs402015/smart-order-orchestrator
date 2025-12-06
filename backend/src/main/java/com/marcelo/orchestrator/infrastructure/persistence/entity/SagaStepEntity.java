package com.marcelo.orchestrator.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA para rastreamento de cada passo da saga.
 * 
 * <p>Persiste informações detalhadas sobre cada passo executado,
 * permitindo rastreamento granular e observabilidade completa.</p>
 * 
 * <h3>Informações Rastreadas:</h3>
 * <ul>
 *   <li>Nome do passo (ORDER_CREATED, PAYMENT_PROCESSED, etc.)</li>
 *   <li>Status (SUCCESS, FAILED)</li>
 *   <li>Timestamps (início e fim)</li>
 *   <li>Duração em milissegundos</li>
 *   <li>Mensagem de erro (se falhou)</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Entity
@Table(name = "saga_steps", indexes = {
    @Index(name = "idx_step_saga_id", columnList = "saga_execution_id"),
    @Index(name = "idx_step_name", columnList = "step_name")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStepEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    /**
     * Saga execution pai.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saga_execution_id", nullable = false)
    private SagaExecutionEntity sagaExecution;
    
    /**
     * Nome do passo (ex: "ORDER_CREATED", "PAYMENT_PROCESSED").
     */
    @Column(name = "step_name", nullable = false, length = 50)
    private String stepName;
    
    /**
     * Status do passo.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StepStatus status;
    
    /**
     * Timestamp de início do passo.
     */
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    /**
     * Timestamp de conclusão do passo.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    /**
     * Duração em milissegundos.
     */
    @Column(name = "duration_ms")
    private Long durationMs;
    
    /**
     * Mensagem de erro (se falhou).
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * Dados adicionais em JSON (opcional, para contexto).
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    /**
     * Enum de status do passo.
     */
    public enum StepStatus {
        STARTED,
        SUCCESS,
        FAILED
    }
}

