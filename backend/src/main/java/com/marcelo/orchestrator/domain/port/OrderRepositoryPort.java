package com.marcelo.orchestrator.domain.port;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepositoryPort {
    
    Order save(Order order);

    Optional<Order> findById(UUID id);

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByPaymentId(String paymentId);
    
    List<Order> findAll();
    
    List<Order> findByStatus(OrderStatus status);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}

