package com.marcelo.orchestrator.infrastructure.persistence.repository;

import com.marcelo.orchestrator.infrastructure.persistence.entity.SagaExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface JpaSagaExecutionRepository extends JpaRepository<SagaExecutionEntity, UUID> {
    
    
    List<SagaExecutionEntity> findByOrderId(UUID orderId);
    
    
    Optional<SagaExecutionEntity> findFirstByOrderIdOrderByStartedAtDesc(UUID orderId);
    
    
    List<SagaExecutionEntity> findByStatus(SagaExecutionEntity.SagaStatus status);
    
    
    Optional<SagaExecutionEntity> findByIdempotencyKey(String idempotencyKey);
}

