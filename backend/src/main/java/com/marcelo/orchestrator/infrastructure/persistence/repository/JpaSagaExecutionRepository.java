package com.marcelo.orchestrator.infrastructure.persistence.repository;

import com.marcelo.orchestrator.infrastructure.persistence.entity.SagaExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository JPA para SagaExecutionEntity.
 * 
 * <p>Fornece métodos para consultar execuções de saga,
 * permitindo observabilidade e rastreamento.</p>
 * 
 * @author Marcelo
 */
@Repository
public interface JpaSagaExecutionRepository extends JpaRepository<SagaExecutionEntity, UUID> {
    
    /**
     * Busca execuções por ID do pedido.
     */
    List<SagaExecutionEntity> findByOrderId(UUID orderId);
    
    /**
     * Busca execução mais recente por ID do pedido.
     */
    Optional<SagaExecutionEntity> findFirstByOrderIdOrderByStartedAtDesc(UUID orderId);
    
    /**
     * Busca execuções por status.
     */
    List<SagaExecutionEntity> findByStatus(SagaExecutionEntity.SagaStatus status);
}

