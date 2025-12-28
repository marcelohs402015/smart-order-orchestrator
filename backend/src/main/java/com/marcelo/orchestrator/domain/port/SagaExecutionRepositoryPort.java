package com.marcelo.orchestrator.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SagaExecutionRepositoryPort {
    
    SagaExecution save(SagaExecution sagaExecution);
    
    Optional<SagaExecution> findById(UUID id);
    
    List<SagaExecution> findByOrderId(UUID orderId);
    
    Optional<SagaExecution> findFirstByOrderIdOrderByStartedAtDesc(UUID orderId);

    List<SagaExecution> findByStatus(SagaExecution.SagaStatus status);

    Optional<SagaExecution> findByIdempotencyKey(String idempotencyKey);

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
        public enum StepStatus {
            STARTED,
            SUCCESS,
            FAILED
        }
    }
}

