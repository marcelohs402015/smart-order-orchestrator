package com.marcelo.orchestrator.infrastructure.persistence.repository;

import com.marcelo.orchestrator.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface JpaOrderRepository extends JpaRepository<OrderEntity, UUID> {
    
    
    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.orderNumber = :orderNumber")
    Optional<OrderEntity> findByOrderNumber(@Param("orderNumber") String orderNumber);
    
    
    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.status = :status ORDER BY o.createdAt DESC")
    List<OrderEntity> findByStatus(@Param("status") com.marcelo.orchestrator.domain.model.OrderStatus status);
    
    
    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.customerId = :customerId ORDER BY o.createdAt DESC")
    List<OrderEntity> findByCustomerId(@Param("customerId") UUID customerId);
    
    
    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    Optional<OrderEntity> findByIdWithItems(@Param("orderId") UUID orderId);
    
    
    Optional<OrderEntity> findById(UUID orderId);
    
    
    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.paymentId = :paymentId")
    Optional<OrderEntity> findByPaymentId(@Param("paymentId") String paymentId);
    
    
    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items ORDER BY o.createdAt DESC")
    List<OrderEntity> findAllWithItems();
}

