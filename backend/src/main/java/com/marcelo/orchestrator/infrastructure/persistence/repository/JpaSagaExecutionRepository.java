package com.marcelo.orchestrator.infrastructure.persistence.repository;

import com.marcelo.orchestrator.infrastructure.persistence.entity.SagaExecutionEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface JpaSagaExecutionRepository extends JpaRepository<SagaExecutionEntity, UUID> {
    
    
    List<SagaExecutionEntity> findByOrderId(UUID orderId);
    
    
    Optional<SagaExecutionEntity> findFirstByOrderIdOrderByStartedAtDesc(UUID orderId);
    
    
    List<SagaExecutionEntity> findByStatus(SagaExecutionEntity.SagaStatus status);
    
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SagaExecutionEntity s WHERE s.idempotencyKey = :idempotencyKey")
    Optional<SagaExecutionEntity> findByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
    
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SagaExecutionEntity s WHERE s.id = :id")
    Optional<SagaExecutionEntity> findByIdWithLock(@Param("id") UUID id);
}

