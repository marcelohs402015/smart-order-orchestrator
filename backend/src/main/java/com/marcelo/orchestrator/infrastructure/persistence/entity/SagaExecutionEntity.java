package com.marcelo.orchestrator.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade JPA para rastreamento de execução de saga.
 * 
 * <p>Persiste o estado completo da execução da saga, permitindo:
 * - Observabilidade: rastrear cada passo executado
 * - Histórico: consultar execuções passadas
 * - Debugging: identificar onde falhou
 * - Métricas: calcular taxa de sucesso, tempo médio, etc.</p>
 * 
 * <h3>Por que persistir estado da saga?</h3>
 * <ul>
 *   <li><strong>Observabilidade:</strong> Rastreamento completo de execuções</li>
 *   <li><strong>Auditoria:</strong> Histórico de todas as operações</li>
 *   <li><strong>Debugging:</strong> Identificar exatamente onde falhou</li>
 *   <li><strong>Métricas:</strong> Taxa de sucesso, tempo médio, etc.</li>
 *   <li><strong>Recuperação:</strong> Possibilidade de retry manual se necessário</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Entity
@Table(name = "saga_executions", indexes = {
    @Index(name = "idx_saga_order_id", columnList = "order_id"),
    @Index(name = "idx_saga_status", columnList = "status"),
    @Index(name = "idx_saga_started_at", columnList = "started_at"),
    @Index(name = "idx_saga_idempotency_key", columnList = "idempotency_key", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaExecutionEntity {
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    /**
     * Chave de idempotência para prevenir execuções duplicadas.
     * 
     * <p>Padrão: Idempotency Key - garante que requisições duplicadas
     * (por timeout, retry, ou usuário clicando várias vezes) não criem
     * pedidos duplicados.</p>
     * 
     * <p>Deve ser único por requisição. Índice único garante que não
     * haverá duplicatas no banco.</p>
     */
    @Column(name = "idempotency_key", length = 255, unique = true)
    private String idempotencyKey;
    
    /**
     * ID do pedido associado a esta saga.
     */
    @Column(name = "order_id", columnDefinition = "UUID")
    private UUID orderId;
    
    /**
     * Status atual da saga.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private SagaStatus status;
    
    /**
     * Passo atual sendo executado.
     */
    @Column(name = "current_step", length = 50)
    private String currentStep;
    
    /**
     * Mensagem de erro (se houver).
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * Timestamp de início da execução.
     */
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    /**
     * Timestamp de conclusão (sucesso ou falha).
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    /**
     * Duração total em milissegundos.
     */
    @Column(name = "duration_ms")
    private Long durationMs;
    
    /**
     * Passos executados (histórico completo).
     */
    @OneToMany(mappedBy = "sagaExecution", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SagaStepEntity> steps = new ArrayList<>();
    
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

