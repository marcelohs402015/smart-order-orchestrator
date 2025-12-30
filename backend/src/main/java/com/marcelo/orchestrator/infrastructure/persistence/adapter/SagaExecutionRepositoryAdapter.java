package com.marcelo.orchestrator.infrastructure.persistence.adapter;

import com.marcelo.orchestrator.domain.port.SagaExecutionRepositoryPort;
import com.marcelo.orchestrator.infrastructure.persistence.entity.SagaExecutionEntity;
import com.marcelo.orchestrator.infrastructure.persistence.entity.SagaStepEntity;
import com.marcelo.orchestrator.infrastructure.persistence.repository.JpaSagaExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class SagaExecutionRepositoryAdapter implements SagaExecutionRepositoryPort {
    
    private final JpaSagaExecutionRepository jpaSagaExecutionRepository;
    
    @Override
    public SagaExecutionRepositoryPort.SagaExecution save(
            SagaExecutionRepositoryPort.SagaExecution sagaExecution) {
        log.debug("Saving saga execution: {}", sagaExecution.id());
        
        
        SagaExecutionEntity entity = toEntity(sagaExecution);
        
        
        SagaExecutionEntity savedEntity = jpaSagaExecutionRepository.save(entity);
        
        
        return toDomain(savedEntity);
    }
    
    @Override
    public Optional<SagaExecutionRepositoryPort.SagaExecution> findById(UUID id) {
        log.debug("Finding saga execution by ID: {}", id);
        
        return jpaSagaExecutionRepository.findById(id)
            .map(this::toDomain);
    }
    
    @Override
    public List<SagaExecutionRepositoryPort.SagaExecution> findByOrderId(UUID orderId) {
        log.debug("Finding saga executions by order ID: {}", orderId);
        
        return jpaSagaExecutionRepository.findByOrderId(orderId).stream()
            .map(this::toDomain)
            .toList();
    }
    
    @Override
    public Optional<SagaExecutionRepositoryPort.SagaExecution> findFirstByOrderIdOrderByStartedAtDesc(UUID orderId) {
        log.debug("Finding latest saga execution by order ID: {}", orderId);
        
        return jpaSagaExecutionRepository.findFirstByOrderIdOrderByStartedAtDesc(orderId)
            .map(this::toDomain);
    }
    
    @Override
    public List<SagaExecutionRepositoryPort.SagaExecution> findByStatus(
            SagaExecutionRepositoryPort.SagaExecution.SagaStatus status) {
        log.debug("Finding saga executions by status: {}", status);
        
        SagaExecutionEntity.SagaStatus jpaStatus = mapToJpaStatus(status);
        return jpaSagaExecutionRepository.findByStatus(jpaStatus).stream()
            .map(this::toDomain)
            .toList();
    }
    
    @Override
    public Optional<SagaExecutionRepositoryPort.SagaExecution> findByIdempotencyKey(String idempotencyKey) {
        log.debug("Finding saga execution by idempotency key: {}", idempotencyKey);
        
        return jpaSagaExecutionRepository.findByIdempotencyKey(idempotencyKey)
            .map(this::toDomain);
    }
    
    @Override
    public Optional<SagaExecutionRepositoryPort.SagaExecution> findByIdWithLock(UUID id) {
        log.debug("Finding saga execution by ID with lock: {}", id);
        
        return jpaSagaExecutionRepository.findByIdWithLock(id)
            .map(this::toDomain);
    }
    
    
    private SagaExecutionEntity toEntity(SagaExecutionRepositoryPort.SagaExecution sagaExecution) {
        SagaExecutionEntity.SagaStatus jpaStatus = mapToJpaStatus(sagaExecution.status());
        
        SagaExecutionEntity entity = SagaExecutionEntity.builder()
            .id(sagaExecution.id())
            .idempotencyKey(sagaExecution.idempotencyKey())
            .orderId(sagaExecution.orderId())
            .status(jpaStatus)
            .currentStep(sagaExecution.currentStep())
            .errorMessage(sagaExecution.errorMessage())
            .startedAt(sagaExecution.startedAt())
            .completedAt(sagaExecution.completedAt())
            .timeoutAt(sagaExecution.timeoutAt())
            .durationMs(sagaExecution.durationMs())
            .build();
        
        
        if (sagaExecution.steps() != null && !sagaExecution.steps().isEmpty()) {
            List<SagaStepEntity> stepEntities = sagaExecution.steps().stream()
                .map(step -> toStepEntity(step, entity))
                .toList();
            entity.setSteps(stepEntities);
        }
        
        return entity;
    }
    
    
    private SagaExecutionRepositoryPort.SagaExecution toDomain(SagaExecutionEntity entity) {
        SagaExecutionRepositoryPort.SagaExecution.SagaStatus domainStatus = mapToDomainStatus(entity.getStatus());
        
        List<SagaExecutionRepositoryPort.SagaStep> steps = entity.getSteps() != null
            ? entity.getSteps().stream()
                .map(this::toStepDomain)
                .toList()
            : List.of();
        
        return new SagaExecutionRepositoryPort.SagaExecution(
            entity.getId(),
            entity.getIdempotencyKey(),
            entity.getOrderId(),
            domainStatus,
            entity.getCurrentStep(),
            entity.getErrorMessage(),
            entity.getStartedAt(),
            entity.getCompletedAt(),
            entity.getTimeoutAt(),
            entity.getDurationMs(),
            steps
        );
    }
    
    
    private SagaStepEntity toStepEntity(
            SagaExecutionRepositoryPort.SagaStep step, SagaExecutionEntity sagaExecution) {
        SagaStepEntity.StepStatus jpaStepStatus = mapToJpaStepStatus(step.status());
        
        return SagaStepEntity.builder()
            .id(step.id())
            .sagaExecution(sagaExecution)
            .stepName(step.stepName())
            .status(jpaStepStatus)
            .errorMessage(step.errorMessage())
            .startedAt(step.startedAt())
            .completedAt(step.completedAt())
            .durationMs(step.durationMs())
            .build();
    }
    
    
    private SagaExecutionRepositoryPort.SagaStep toStepDomain(SagaStepEntity entity) {
        SagaExecutionRepositoryPort.SagaStep.StepStatus domainStepStatus = mapToDomainStepStatus(entity.getStatus());
        
        return new SagaExecutionRepositoryPort.SagaStep(
            entity.getId(),
            entity.getSagaExecution().getId(),
            entity.getStepName(),
            domainStepStatus,
            entity.getErrorMessage(),
            entity.getStartedAt(),
            entity.getCompletedAt(),
            entity.getDurationMs()
        );
    }
    
    
    private SagaExecutionEntity.SagaStatus mapToJpaStatus(
            SagaExecutionRepositoryPort.SagaExecution.SagaStatus domainStatus) {
        return switch (domainStatus) {
            case STARTED -> SagaExecutionEntity.SagaStatus.STARTED;
            case ORDER_CREATED -> SagaExecutionEntity.SagaStatus.ORDER_CREATED;
            case PAYMENT_PROCESSED -> SagaExecutionEntity.SagaStatus.PAYMENT_PROCESSED;
            case RISK_ANALYZED -> SagaExecutionEntity.SagaStatus.RISK_ANALYZED;
            case COMPLETED -> SagaExecutionEntity.SagaStatus.COMPLETED;
            case FAILED -> SagaExecutionEntity.SagaStatus.FAILED;
            case COMPENSATED -> SagaExecutionEntity.SagaStatus.COMPENSATED;
        };
    }
    
    
    private SagaExecutionRepositoryPort.SagaExecution.SagaStatus mapToDomainStatus(
            SagaExecutionEntity.SagaStatus jpaStatus) {
        return switch (jpaStatus) {
            case STARTED -> SagaExecutionRepositoryPort.SagaExecution.SagaStatus.STARTED;
            case ORDER_CREATED -> SagaExecutionRepositoryPort.SagaExecution.SagaStatus.ORDER_CREATED;
            case PAYMENT_PROCESSED -> SagaExecutionRepositoryPort.SagaExecution.SagaStatus.PAYMENT_PROCESSED;
            case RISK_ANALYZED -> SagaExecutionRepositoryPort.SagaExecution.SagaStatus.RISK_ANALYZED;
            case COMPLETED -> SagaExecutionRepositoryPort.SagaExecution.SagaStatus.COMPLETED;
            case FAILED -> SagaExecutionRepositoryPort.SagaExecution.SagaStatus.FAILED;
            case COMPENSATED -> SagaExecutionRepositoryPort.SagaExecution.SagaStatus.COMPENSATED;
        };
    }
    
    
    private SagaStepEntity.StepStatus mapToJpaStepStatus(
            SagaExecutionRepositoryPort.SagaStep.StepStatus domainStatus) {
        return switch (domainStatus) {
            case STARTED -> SagaStepEntity.StepStatus.STARTED;
            case SUCCESS -> SagaStepEntity.StepStatus.SUCCESS;
            case FAILED -> SagaStepEntity.StepStatus.FAILED;
        };
    }
    
    
    private SagaExecutionRepositoryPort.SagaStep.StepStatus mapToDomainStepStatus(
            SagaStepEntity.StepStatus jpaStatus) {
        return switch (jpaStatus) {
            case STARTED -> SagaExecutionRepositoryPort.SagaStep.StepStatus.STARTED;
            case SUCCESS -> SagaExecutionRepositoryPort.SagaStep.StepStatus.SUCCESS;
            case FAILED -> SagaExecutionRepositoryPort.SagaStep.StepStatus.FAILED;
        };
    }
}

