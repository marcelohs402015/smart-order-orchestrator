package com.marcelo.orchestrator.infrastructure.persistence.repository;

import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório JPA para OrderEntity.
 * 
 * <p>Interface Spring Data JPA que fornece operações CRUD automáticas.
 * Métodos customizados podem ser adicionados aqui usando convenções de nomenclatura
 * ou queries JPQL/SQL nativas.</p>
 * 
 * <h3>Spring Data JPA:</h3>
 * <ul>
 *   <li><strong>JpaRepository:</strong> Fornece métodos CRUD básicos</li>
 *   <li><strong>Query Methods:</strong> Métodos nomeados geram queries automaticamente</li>
 *   <li><strong>@Query:</strong> Queries customizadas quando necessário</li>
 * </ul>
 * 
 * <h3>Por que Spring Data JPA?</h3>
 * <ul>
 *   <li><strong>Produtividade:</strong> Menos código boilerplate</li>
 *   <li><strong>Type Safety:</strong> Queries baseadas em métodos</li>
 *   <li><strong>Flexibilidade:</strong> Pode usar JPQL, SQL nativo, ou convenções</li>
 * </ul>
 * 
 * @author Marcelo
 */
@Repository
public interface JpaOrderRepository extends JpaRepository<OrderEntity, UUID> {
    
    /**
     * Busca pedido pelo número do pedido.
     * Spring Data JPA gera query automaticamente baseado no nome do método.
     * 
     * @param orderNumber Número do pedido
     * @return Optional contendo OrderEntity se encontrado
     */
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
    
    /**
     * Busca pedidos por status.
     * 
     * @param status Status desejado
     * @return Lista de pedidos com o status especificado
     */
    List<OrderEntity> findByStatus(com.marcelo.orchestrator.domain.model.OrderStatus status);
    
    /**
     * Busca pedidos de um cliente.
     * 
     * @param customerId ID do cliente
     * @return Lista de pedidos do cliente
     */
    List<OrderEntity> findByCustomerId(UUID customerId);
    
    /**
     * Query customizada para buscar pedidos com itens (eager loading).
     * Útil quando precisa carregar itens junto com pedido.
     * 
     * @param orderId ID do pedido
     * @return Optional contendo OrderEntity com itens carregados
     */
    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    Optional<OrderEntity> findByIdWithItems(@Param("orderId") UUID orderId);
}

