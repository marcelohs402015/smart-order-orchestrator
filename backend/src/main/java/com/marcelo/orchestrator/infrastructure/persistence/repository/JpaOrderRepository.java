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
     * Busca pedido pelo número do pedido com itens carregados (eager loading).
     * Resolve problema de LazyInitializationException ao acessar items fora da sessão.
     * 
     * @param orderNumber Número do pedido
     * @return Optional contendo OrderEntity com itens carregados
     */
    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.orderNumber = :orderNumber")
    Optional<OrderEntity> findByOrderNumber(@Param("orderNumber") String orderNumber);
    
    /**
     * Busca pedidos por status com itens carregados (eager loading).
     * Resolve problema de LazyInitializationException ao acessar items fora da sessão.
     * 
     * @param status Status desejado
     * @return Lista de pedidos com o status especificado e itens carregados
     */
    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.status = :status ORDER BY o.createdAt DESC")
    List<OrderEntity> findByStatus(@Param("status") com.marcelo.orchestrator.domain.model.OrderStatus status);
    
    /**
     * Busca pedidos de um cliente com itens carregados (eager loading).
     * Resolve problema de LazyInitializationException ao acessar items fora da sessão.
     * 
     * @param customerId ID do cliente
     * @return Lista de pedidos do cliente com itens carregados
     */
    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.customerId = :customerId ORDER BY o.createdAt DESC")
    List<OrderEntity> findByCustomerId(@Param("customerId") UUID customerId);
    
    /**
     * Busca pedido por ID com itens carregados (eager loading).
     * Resolve problema de LazyInitializationException ao acessar items fora da sessão.
     * 
     * @param orderId ID do pedido
     * @return Optional contendo OrderEntity com itens carregados
     */
    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    Optional<OrderEntity> findByIdWithItems(@Param("orderId") UUID orderId);
    
    /**
     * Busca pedido por ID (método padrão do JpaRepository).
     * Usa findByIdWithItems quando precisar carregar items.
     */
    Optional<OrderEntity> findById(UUID orderId);
    
    /**
     * Query customizada para buscar todos os pedidos com itens (eager loading).
     * Resolve problema de LazyInitializationException ao acessar items fora da sessão.
     * 
     * @return Lista de OrderEntity com itens carregados
     */
    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items ORDER BY o.createdAt DESC")
    List<OrderEntity> findAllWithItems();
}

