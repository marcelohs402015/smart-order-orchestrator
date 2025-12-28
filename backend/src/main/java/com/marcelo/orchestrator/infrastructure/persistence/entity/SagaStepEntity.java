package com.marcelo.orchestrator.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


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
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saga_execution_id", nullable = false)
    private SagaExecutionEntity sagaExecution;
    
    
    @Column(name = "step_name", nullable = false, length = 50)
    private String stepName;
    
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StepStatus status;
    
    
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    
    @Column(name = "duration_ms")
    private Long durationMs;
    
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    
    public enum StepStatus {
        STARTED,
        SUCCESS,
        FAILED
    }
}

