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
    
    
    @Column(name = "idempotency_key", length = 255, unique = true)
    private String idempotencyKey;
    
    
    @Column(name = "order_id", columnDefinition = "UUID")
    private UUID orderId;
    
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private SagaStatus status;
    
    
    @Column(name = "current_step", length = 50)
    private String currentStep;
    
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    
    @Column(name = "duration_ms")
    private Long durationMs;
    
    
    @Column(name = "timeout_at")
    private LocalDateTime timeoutAt;
    
    
    @OneToMany(mappedBy = "sagaExecution", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SagaStepEntity> steps = new ArrayList<>();
    
    
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

